package dev.pennyrush.feature.home

object MerchantExtractor {

    data class Analysis(val merchant: String, val kind: TransactionKind)

    fun analyze(rawDescription: String): Analysis {
        val raw = rawDescription.trim()
        if (raw.isBlank()) return Analysis("Transaction", TransactionKind.Other)

        val upper = raw.uppercase()
        val kind = when {
            upper.contains("UPI") || upper.contains("BHIM") -> TransactionKind.UPI
            upper.startsWith("POS") || upper.contains("CARD") || upper.contains("VISA") || upper.contains("MASTERCARD") || upper.contains("RUPAY") -> TransactionKind.Card
            upper.startsWith("NEFT") || upper.startsWith("IMPS") || upper.startsWith("RTGS") -> TransactionKind.Transfer
            upper.startsWith("ATM") || upper.contains("CASH WDL") || upper.contains("CASH WITHDRAWAL") -> TransactionKind.ATM
            upper.contains("SALARY") || upper.contains("PAYROLL") -> TransactionKind.Salary
            upper.contains("BILL") || upper.contains("ELECTR") || upper.contains("BROADBAND") || upper.contains("RECHARGE") || upper.contains("MOBILE") -> TransactionKind.Bill
            else -> TransactionKind.Other
        }

        val merchant = when (kind) {
            TransactionKind.UPI -> extractUpi(raw)
            TransactionKind.Card -> extractCard(raw)
            TransactionKind.Transfer -> extractTransfer(raw)
            TransactionKind.ATM -> "ATM withdrawal"
            else -> tidy(raw)
        }
        return Analysis(merchant.ifBlank { tidy(raw) }, kind)
    }

    private val tokenSplit = "[/\\-_|]+".toRegex()
    private val digitsOnly = "^\\d+$".toRegex()
    private val refLike = ".*\\d{8,}.*".toRegex()
    private val noiseTokens = setOf(
        "UPI", "BHIM", "POS", "CARD", "NEFT", "IMPS", "RTGS", "ACH", "DR", "CR",
        "TXN", "REF", "REFNO", "TRF", "PAY", "PAYTM", "GPAY", "PHONEPE",
        "PAYMENT", "DEBIT", "CREDIT", "FROM", "TO", "VIA", "ECOM",
    )

    private fun extractUpi(raw: String): String {
        val parts = raw.split(tokenSplit).map { it.trim() }.filter { it.isNotEmpty() }
        // Skip tokens that look like refs / VPAs / pure digits / noise.
        val candidates = parts.filter { token ->
            val u = token.uppercase()
            !digitsOnly.matches(token) &&
                !refLike.matches(token) &&
                !token.contains("@") &&
                u !in noiseTokens &&
                token.any { it.isLetter() } &&
                token.length >= 2
        }
        return candidates.maxByOrNull { scoreMerchant(it) }
            ?.let { titleCase(it) }
            ?: tidy(raw)
    }

    private fun extractCard(raw: String): String {
        var s = raw
        // Strip "POS" prefix and any masked card number like XXXXXXXXXXXX1234.
        s = s.replace("(?i)^POS\\s+".toRegex(), "")
        s = s.replace("[Xx*]{4,}\\d{2,}".toRegex(), " ")
        s = s.replace("\\b\\d{12,}\\b".toRegex(), " ")
        return titleCase(s.split(tokenSplit).joinToString(" ").trim())
    }

    private fun extractTransfer(raw: String): String {
        val parts = raw.split(tokenSplit).map { it.trim() }.filter { it.isNotEmpty() }
        val candidates = parts.filter { token ->
            val u = token.uppercase()
            !digitsOnly.matches(token) &&
                !refLike.matches(token) &&
                u !in noiseTokens &&
                token.any { it.isLetter() } &&
                token.length >= 2
        }
        return candidates.maxByOrNull { scoreMerchant(it) }
            ?.let { titleCase(it) }
            ?: tidy(raw)
    }

    private fun scoreMerchant(token: String): Int {
        val letters = token.count { it.isLetter() }
        val digits = token.count { it.isDigit() }
        return letters * 3 - digits * 4 + (if (token.length in 4..30) 5 else 0)
    }

    private fun tidy(raw: String): String {
        val cleaned = raw
            .replace("\\s+".toRegex(), " ")
            .trim()
            .removeSuffix("-")
            .removeSuffix("/")
        return titleCase(cleaned.take(60))
    }

    private fun titleCase(input: String): String {
        if (input.isBlank()) return input
        if (input.any { it.isLowerCase() }) return input.trim()
        return input.split(" ").joinToString(" ") { word ->
            if (word.length <= 3) word.uppercase()
            else word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
