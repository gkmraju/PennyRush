package dev.pennyrush.core.common

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormatter {
    fun format(amount: Double, currencyCode: String = "USD", locale: Locale = Locale.US): String {
        return NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
            maximumFractionDigits = 0
        }.format(amount)
    }
}
