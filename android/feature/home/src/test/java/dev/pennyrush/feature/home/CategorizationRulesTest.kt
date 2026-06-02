package dev.pennyrush.feature.home

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class CategorizationRulesTest {

    @Test
    fun mapsKnownMerchantsToCategories() {
        assertEquals("Food", CategorizationRules.categoryNameForText("Swiggy Instamart Cafe"))
        assertEquals("Transport", CategorizationRules.categoryNameForText("UPI Uber Trip"))
        assertEquals("Bills", CategorizationRules.categoryNameForText("Airtel broadband bill"))
    }

    @Test
    fun mapsIncomeAndSpecialKinds() {
        assertEquals(
            "Income",
            CategorizationRules.categoryNameFor(
                Transaction(
                    date = LocalDate.of(2026, 5, 12),
                    description = "SALARY MAY 2026",
                    merchant = "Salary",
                    amount = 50000.0,
                    kind = TransactionKind.Salary,
                ),
            ),
        )
        assertEquals(
            "ATM",
            CategorizationRules.categoryNameFor(
                Transaction(
                    date = LocalDate.of(2026, 5, 12),
                    description = "ATM CASH WDL",
                    merchant = "ATM withdrawal",
                    amount = -1000.0,
                    kind = TransactionKind.ATM,
                ),
            ),
        )
    }
}
