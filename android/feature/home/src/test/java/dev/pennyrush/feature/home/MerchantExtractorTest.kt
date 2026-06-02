package dev.pennyrush.feature.home

import org.junit.Assert.assertEquals
import org.junit.Test

class MerchantExtractorTest {

    @Test
    fun extractsMerchantFromUpiNarration() {
        val result = MerchantExtractor.analyze("UPI/508812345678/ZOMATO/zomato@upi")

        assertEquals("Zomato", result.merchant)
        assertEquals(TransactionKind.UPI, result.kind)
    }

    @Test
    fun extractsMerchantFromCardNarration() {
        val result = MerchantExtractor.analyze("POS XXXXXXXXXXXX1234 RELIANCE FRESH")

        assertEquals("Reliance Fresh", result.merchant)
        assertEquals(TransactionKind.Card, result.kind)
    }

    @Test
    fun classifiesAtmWithdrawals() {
        val result = MerchantExtractor.analyze("ATM CASH WDL 123456 HDFC")

        assertEquals("ATM withdrawal", result.merchant)
        assertEquals(TransactionKind.ATM, result.kind)
    }
}
