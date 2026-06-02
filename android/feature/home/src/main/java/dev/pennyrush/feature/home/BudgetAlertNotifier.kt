package dev.pennyrush.feature.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.pennyrush.core.designsystem.AppPreferences
import java.time.YearMonth
import kotlin.math.abs

internal object BudgetAlertNotifier {
    const val ChannelId = "budget_alerts"
    private const val NotificationPrefs = "pennyrush_notification_state"
    private const val LargeTransactionThreshold = 5000.0

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            ChannelId,
            "Budget alerts",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Budget progress, big-spend alerts, and planning nudges"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canPost(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                )

    @SuppressLint("MissingPermission")
    fun notifyBudgetThresholds(context: Context, rows: List<BudgetProgress>) {
        if (!AppPreferences.budgetAlerts || !canPost(context)) return
        ensureChannel(context)
        val prefs = context.getSharedPreferences(NotificationPrefs, Context.MODE_PRIVATE)
        val month = YearMonth.now().toString()
        rows
            .filter { it.spent > 0.0 && it.limit > 0.0 }
            .forEach { row ->
                val ratio = row.spent / row.limit
                val threshold = when {
                    ratio >= 1.0 -> 100
                    ratio >= 0.8 -> 80
                    ratio >= 0.5 -> 50
                    else -> null
                } ?: return@forEach
                val key = "budget_${month}_${row.category}_$threshold"
                if (prefs.getBoolean(key, false)) return@forEach
                val over = row.spent - row.limit
                val body = if (over > 0) {
                    "${row.category} is over by ${MoneyText.compact(abs(over))}."
                } else {
                    "${row.category} has reached $threshold% of ${MoneyText.compact(row.limit)}."
                }
                NotificationManagerCompat.from(context).notify(
                    key.hashCode(),
                    NotificationCompat.Builder(context, ChannelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Budget alert")
                        .setContentText(body)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build(),
                )
                prefs.edit().putBoolean(key, true).apply()
            }
    }

    @SuppressLint("MissingPermission")
    fun notifyLargeTransactions(context: Context, transactions: List<Transaction>) {
        if (!AppPreferences.largeTransactions || !canPost(context)) return
        ensureChannel(context)
        val prefs = context.getSharedPreferences(NotificationPrefs, Context.MODE_PRIVATE)
        transactions
            .filter { abs(it.amount) >= LargeTransactionThreshold }
            .forEach { transaction ->
                val key = "large_${transaction.id}"
                if (prefs.getBoolean(key, false)) return@forEach
                val direction = if (transaction.amount < 0) "spend" else "income"
                val body = "${transaction.merchant}: ${MoneyText.compact(abs(transaction.amount))} $direction recorded."
                NotificationManagerCompat.from(context).notify(
                    key.hashCode(),
                    NotificationCompat.Builder(context, ChannelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Big spend noticed")
                        .setContentText(body)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build(),
                )
                prefs.edit().putBoolean(key, true).apply()
            }
    }
}

private object MoneyText {
    fun compact(value: Double): String = when {
        value >= 10000000 -> "₹${trim(value / 10000000)}Cr"
        value >= 100000 -> "₹${trim(value / 100000)}L"
        value >= 1000 -> "₹${trim(value / 1000)}K"
        else -> "₹${trim(value)}"
    }

    private fun trim(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
}
