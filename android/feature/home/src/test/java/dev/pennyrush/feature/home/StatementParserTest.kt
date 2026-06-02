package dev.pennyrush.feature.home

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatementParserTest {

    @Test
    fun parsesDebitCreditBankCsv() {
        val csv = """
            Date,Narration,Withdrawal Amount,Deposit Amount
            15/05/2026,UPI/123456789012/SWIGGY/food@upi,249.50,
            14/05/2026,SALARY MAY 2026,,50000.00
        """.trimIndent()

        val outcome = StatementParser.parseCsv(csv)

        assertTrue(outcome is ParseOutcome.Success)
        val transactions = (outcome as ParseOutcome.Success).transactions
        assertEquals(2, transactions.size)
        assertEquals(LocalDate.of(2026, 5, 15), transactions[0].date)
        assertEquals(-249.50, transactions[0].amount, 0.001)
        assertEquals("Swiggy", transactions[0].merchant)
        assertEquals(TransactionKind.UPI, transactions[0].kind)
        assertEquals(50000.00, transactions[1].amount, 0.001)
        assertEquals(TransactionKind.Salary, transactions[1].kind)
    }

    @Test
    fun parsesSignedAmountCsvWithQuotedCommaAndEscapedQuote() {
        val csv = listOf(
            "Txn Date,Description,Amount",
            "2026-05-12,\"POS STARBUCKS, BANDRA \"\"WEST\"\"\",-612.75",
        ).joinToString("\n")

        val outcome = StatementParser.parseCsv(csv)

        assertTrue(outcome is ParseOutcome.Success)
        val transaction = (outcome as ParseOutcome.Success).transactions.single()
        assertEquals("POS STARBUCKS, BANDRA \"WEST\"", transaction.description)
        assertEquals("Starbucks, Bandra \"West\"", transaction.merchant)
        assertEquals(-612.75, transaction.amount, 0.001)
        assertEquals(TransactionKind.Card, transaction.kind)
    }

    @Test
    fun returnsHelpfulFailureForUnknownHeaders() {
        val outcome = StatementParser.parseCsv(
            """
                Foo,Bar,Baz
                one,two,three
            """.trimIndent(),
        )

        assertTrue(outcome is ParseOutcome.Failed)
        assertTrue((outcome as ParseOutcome.Failed).reason.contains("statement columns"))
    }
}
