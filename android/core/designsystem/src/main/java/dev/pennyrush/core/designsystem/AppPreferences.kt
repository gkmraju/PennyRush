package dev.pennyrush.core.designsystem

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Currency

object AppPreferences {
    private const val PREFS = "pennyrush_app"
    private const val KEY_BUDGET = "budget_alerts"
    private const val KEY_LARGE = "large_transactions"
    private const val KEY_BIOMETRIC = "biometric_lock"
    private const val KEY_CURRENCY = "currency_code"

    private var prefs: SharedPreferences? = null

    var budgetAlerts: Boolean by mutableStateOf(true)
        private set
    var largeTransactions: Boolean by mutableStateOf(true)
        private set
    var biometricLock: Boolean by mutableStateOf(false)
        private set
    var currencyCode: String by mutableStateOf("INR")
        private set

    fun init(context: Context) {
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        budgetAlerts = p.getBoolean(KEY_BUDGET, true)
        largeTransactions = p.getBoolean(KEY_LARGE, true)
        biometricLock = p.getBoolean(KEY_BIOMETRIC, false)
        currencyCode = p.getString(KEY_CURRENCY, "INR")
            ?.takeIf { runCatching { Currency.getInstance(it) }.isSuccess }
            ?: "INR"
    }

    fun updateBudgetAlerts(value: Boolean) {
        budgetAlerts = value
        prefs?.edit()?.putBoolean(KEY_BUDGET, value)?.apply()
    }

    fun updateLargeTransactions(value: Boolean) {
        largeTransactions = value
        prefs?.edit()?.putBoolean(KEY_LARGE, value)?.apply()
    }

    fun updateBiometricLock(value: Boolean) {
        biometricLock = value
        prefs?.edit()?.putBoolean(KEY_BIOMETRIC, value)?.apply()
    }

    fun updateCurrencyCode(value: String) {
        val normalized = value.trim().uppercase().take(3)
        if (normalized.length != 3 || runCatching { Currency.getInstance(normalized) }.isFailure) return
        currencyCode = normalized
        prefs?.edit()?.putString(KEY_CURRENCY, normalized)?.apply()
    }
}
