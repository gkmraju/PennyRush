package dev.pennyrush.core.common

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

object MoneyFormatter {

    private val inLocale: Locale = Locale.Builder().setLanguage("en").setRegion("IN").build()

    fun format(
        amount: Double,
        currencyCode: String = "INR",
        locale: Locale = inLocale,
        showSign: Boolean = false,
    ): String {
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
            maximumFractionDigits = 0
        }
        val formatted = formatter.format(abs(amount))
        return when {
            showSign && amount > 0 -> "+$formatted"
            showSign && amount < 0 -> "-$formatted"
            amount < 0 -> "-$formatted"
            else -> formatted
        }
    }

    fun compact(amount: Double, currencyCode: String = "INR"): String {
        val abs = abs(amount)
        val sym = when (currencyCode) { "INR" -> "₹"; "USD" -> "$"; else -> currencyCode }
        return when {
            abs >= 10_000_000 -> "$sym${"%.1f".format(abs / 10_000_000)}Cr"
            abs >= 100_000 -> "$sym${"%.1f".format(abs / 100_000)}L"
            abs >= 1_000 -> "$sym${"%.1f".format(abs / 1_000)}K"
            else -> format(amount, currencyCode)
        }
    }
}
