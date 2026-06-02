package dev.pennyrush.feature.home

import java.util.Locale
import kotlin.math.abs

object ImportIdentity {
    fun dedupeKey(transaction: Transaction): String {
        val type = if (transaction.amount < 0) "expense" else "income"
        val amount = String.format(Locale.US, "%.2f", abs(transaction.amount))
        val merchant = normalize(transaction.description.ifBlank { transaction.merchant }.ifBlank { "transaction" })
        return "${transaction.date}|$amount|$type|$merchant"
    }

    fun hash(transaction: Transaction): String = "web-${fnv1a(dedupeKey(transaction))}"

    private fun normalize(value: String): String =
        value.lowercase()
            .replace("[^a-z0-9]+".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()

    private fun fnv1a(value: String): String {
        var hash = 0x811c9dc5L
        for (ch in value) {
            hash = hash xor ch.code.toLong()
            hash = (hash * 0x01000193L) and 0xffffffffL
        }
        return hash.toString(16).padStart(8, '0')
    }
}
