package dev.pennyrush.feature.home

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanningLogicTest {

    @Test
    fun buildsBudgetProgressForCurrentMonthCategories() {
        val month = YearMonth.of(2026, 5)
        val transactions = listOf(
            txn("2026-05-02", "Zomato dinner", "Zomato", -800.0),
            txn("2026-05-04", "Zomato lunch", "Zomato", -650.0),
            txn("2026-05-10", "Uber ride", "Uber", -450.0),
            txn("2026-04-30", "Zomato old", "Zomato", -999.0),
            txn("2026-05-01", "Salary", "Salary", 50000.0, TransactionKind.Salary),
        )

        val rows = buildBudgetProgress(
            transactions = transactions,
            limits = DefaultBudgetLimits,
            month = month,
        )

        val food = rows.first { it.category == "Food" }
        val transport = rows.first { it.category == "Transport" }
        assertEquals(1450.0, food.spent, 0.001)
        assertEquals(450.0, transport.spent, 0.001)
        assertTrue(rows.none { it.category == "Income" })
    }

    @Test
    fun usesCustomBudgetLimitWhenProvided() {
        val rows = buildBudgetProgress(
            transactions = listOf(txn("2026-05-02", "Zomato dinner", "Zomato", -800.0)),
            limits = DefaultBudgetLimits + ("Food" to 1000.0),
            month = YearMonth.of(2026, 5),
        )

        val food = rows.first { it.category == "Food" }
        assertEquals(1000.0, food.limit, 0.001)
    }

    @Test
    fun detectsRecurringMerchantsAcrossMonths() {
        val rows = detectRecurringMerchants(
            listOf(
                txn("2026-03-03", "Netflix", "Netflix", -649.0),
                txn("2026-04-03", "Netflix", "Netflix", -649.0),
                txn("2026-05-03", "Netflix", "Netflix", -649.0),
                txn("2026-05-12", "One off", "Camera Store", -42000.0),
            ),
        )

        assertEquals(1, rows.size)
        assertEquals("Netflix", rows.single().merchant)
        assertEquals(3, rows.single().count)
        assertEquals(649.0, rows.single().averageAmount, 0.001)
    }

    private fun txn(
        date: String,
        description: String,
        merchant: String,
        amount: Double,
        kind: TransactionKind = MerchantExtractor.analyze(description).kind,
    ) = Transaction(
        date = LocalDate.parse(date),
        description = description,
        merchant = merchant,
        amount = amount,
        kind = kind,
    )
}
