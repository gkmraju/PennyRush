package dev.pennyrush.feature.home

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportIdentityTest {

    @Test
    fun createsWebCompatibleImportHash() {
        val transaction = Transaction(
            date = LocalDate.of(2026, 5, 12),
            description = "Coffee Shop",
            merchant = "Coffee Shop",
            amount = -120.0,
            kind = TransactionKind.Other,
        )

        assertEquals("2026-05-12|120.00|expense|coffee shop", ImportIdentity.dedupeKey(transaction))
        assertEquals("web-7319a2f8", ImportIdentity.hash(transaction))
    }
}
