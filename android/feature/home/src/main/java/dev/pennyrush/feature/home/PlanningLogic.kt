package dev.pennyrush.feature.home

import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

internal val DefaultBudgetLimits = linkedMapOf(
    "Food" to 12000.0,
    "Groceries" to 10000.0,
    "Transport" to 6000.0,
    "Fuel" to 8000.0,
    "Bills" to 15000.0,
    "Subscriptions" to 3000.0,
    "Shopping" to 10000.0,
    "Entertainment" to 5000.0,
    "Health" to 6000.0,
    "Travel" to 12000.0,
    "Other" to 6000.0,
)

internal data class BudgetProgress(
    val category: String,
    val spent: Double,
    val limit: Double,
)

internal data class RecurringMerchant(
    val merchant: String,
    val count: Int,
    val averageAmount: Double,
    val lastSeen: LocalDate,
)

internal fun buildBudgetProgress(
    transactions: List<Transaction>,
    limits: Map<String, Double>,
    month: YearMonth = YearMonth.now(),
): List<BudgetProgress> {
    val monthSpend = transactions.filter { YearMonth.from(it.date) == month && it.amount < 0 }
    val spendByCategory = monthSpend
        .groupBy { CategorizationRules.categoryNameFor(it) }
        .mapValues { (_, txns) -> txns.sumOf { abs(it.amount) } }
    val categories = (DefaultBudgetLimits.keys + spendByCategory.keys).distinct()
    return categories
        .map { category ->
            BudgetProgress(
                category = category,
                spent = spendByCategory[category] ?: 0.0,
                limit = limits[category] ?: DefaultBudgetLimits[category] ?: 6000.0,
            )
        }
        .sortedWith(
            compareByDescending<BudgetProgress> { it.spent / it.limit.coerceAtLeast(1.0) }
                .thenByDescending { it.spent },
        )
}

internal fun detectRecurringMerchants(transactions: List<Transaction>): List<RecurringMerchant> =
    transactions
        .filter { it.amount < 0 }
        .groupBy { it.merchant.trim().lowercase() }
        .values
        .mapNotNull { rows ->
            if (rows.size < 2) return@mapNotNull null
            val sorted = rows.sortedByDescending { it.date }
            val merchant = sorted.first().merchant
            val average = sorted.map { abs(it.amount) }.average()
            val distinctMonths = sorted.map { YearMonth.from(it.date) }.distinct().size
            val looksRecurring = distinctMonths >= 2 || rows.size >= 3 ||
                CategorizationRules.categoryNameFor(sorted.first()) in setOf("Bills", "Subscriptions")
            if (!looksRecurring) return@mapNotNull null
            RecurringMerchant(
                merchant = merchant,
                count = rows.size,
                averageAmount = average,
                lastSeen = sorted.first().date,
            )
        }
        .sortedByDescending { it.averageAmount * it.count }
        .take(6)
