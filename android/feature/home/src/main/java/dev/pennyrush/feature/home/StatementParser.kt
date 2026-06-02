package dev.pennyrush.feature.home

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

sealed interface ParseOutcome {
    data class Success(val transactions: List<Transaction>) : ParseOutcome
    data class Failed(val reason: String, val previewLines: List<String>) : ParseOutcome
}

object StatementParser {

    const val MAX_BYTES: Int = 5 * 1024 * 1024
    private const val MAX_LINES = 20_000
    private const val MAX_AMOUNT = 1_000_000_000.0

    private val dateFormats = listOf(
        "yyyy-MM-dd", "yyyy/MM/dd",
        "dd-MM-yyyy", "dd/MM/yyyy",
        "MM-dd-yyyy", "MM/dd/yyyy",
        "dd-MMM-yyyy", "dd MMM yyyy",
    ).map { DateTimeFormatter.ofPattern(it) }

    fun parseCsv(text: String): ParseOutcome {
        if (text.isBlank()) return ParseOutcome.Failed("File is empty.", emptyList())

        if (looksBinary(text)) {
            return ParseOutcome.Failed(
                "This doesn't look like a readable statement file. Download a spreadsheet version from your bank and try again.",
                emptyList(),
            )
        }

        val raw = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (raw.isEmpty()) return ParseOutcome.Failed("File has no readable entries.", emptyList())
        if (raw.size > MAX_LINES) {
            return ParseOutcome.Failed(
                "This file is too large for a quick import. Try a shorter statement.",
                raw.take(6),
            )
        }

        val headerIdx = raw.indexOfFirst { line ->
            val cells = splitCsvLine(line).map { it.lowercase() }
            if (cells.size < 3) return@indexOfFirst false
            val hasDate = cells.any { it.contains("date") }
            val hasMoney = cells.any {
                it == "amount" || it.contains("amount") ||
                    it.contains("debit") || it.contains("credit") ||
                    it.contains("withdraw") || it.contains("deposit")
            }
            val hasDesc = cells.any {
                it.contains("desc") || it.contains("narration") ||
                    it.contains("particular") || it.contains("payee") ||
                    it.contains("details") || it == "memo" || it == "merchant"
            }
            val score = (if (hasDate) 1 else 0) + (if (hasMoney) 1 else 0) + (if (hasDesc) 1 else 0)
            hasDate && score >= 2
        }
        if (headerIdx < 0) {
            return ParseOutcome.Failed(
                "Couldn't find the statement columns. Look for Date, Description, Amount, Debit, or Credit.",
                raw.take(6),
            )
        }

        val header = splitCsvLine(raw[headerIdx]).map { it.lowercase() }
        val dateIdx = header.indexOfFirst { it.contains("date") }
        val descIdx = header.indexOfFirst {
            it.contains("descr") || it.contains("narration") ||
                it.contains("particular") || it.contains("payee") ||
                it.contains("details") || it == "memo" || it == "merchant"
        }
        val amountIdx = header.indexOfFirst {
            it == "amount" || it == "txn amount" || it == "transaction amount"
        }
        val debitIdx = header.indexOfFirst {
            it.contains("withdraw") || it == "debit" || it.contains("debit amount")
        }
        val creditIdx = header.indexOfFirst {
            it.contains("deposit") || it == "credit" || it.contains("credit amount")
        }

        if (amountIdx < 0 && (debitIdx < 0 || creditIdx < 0)) {
            return ParseOutcome.Failed(
                "Couldn't find an amount column. Look for Amount, Debit, Credit, Withdrawal, or Deposit.",
                raw.take(6),
            )
        }

        val today = LocalDate.now()
        val minDate = today.minusYears(50)
        val maxDate = today.plusYears(10)

        val parsed = mutableListOf<Transaction>()
        var skippedDates = 0
        var skippedAmounts = 0

        for (line in raw.drop(headerIdx + 1)) {
            val cells = splitCsvLine(line)
            if (cells.size <= dateIdx) continue
            val date = parseDate(cells.getOrNull(dateIdx)) ?: continue
            if (date.isBefore(minDate) || date.isAfter(maxDate)) {
                skippedDates++
                continue
            }
            val description = descIdx.takeIf { it >= 0 }
                ?.let { cells.getOrNull(it) }
                ?.takeIf { it.isNotBlank() }
                ?: "Imported entry"

            val amount = when {
                amountIdx >= 0 -> parseAmount(cells.getOrNull(amountIdx))
                else -> {
                    val debit = parseAmount(cells.getOrNull(debitIdx)) ?: 0.0
                    val credit = parseAmount(cells.getOrNull(creditIdx)) ?: 0.0
                    when {
                        debit > 0.0 -> -debit
                        credit > 0.0 -> credit
                        else -> null
                    }
                }
            } ?: continue
            if (amount == 0.0) continue
            if (abs(amount) > MAX_AMOUNT) {
                skippedAmounts++
                continue
            }
            val analysis = MerchantExtractor.analyze(description)
            parsed += Transaction(
                date = date,
                description = description,
                merchant = analysis.merchant,
                amount = amount,
                kind = analysis.kind,
            )
        }

        if (parsed.isEmpty()) {
            val hint = when {
                skippedDates > 0 -> "All dates fell outside the accepted range (within 50 years past, 10 years future)."
                skippedAmounts > 0 -> "All amounts were above the supported import limit."
                else -> "The columns looked right, but no entries could be turned into activity."
            }
            return ParseOutcome.Failed(hint, raw.take(6))
        }
        return ParseOutcome.Success(parsed.sortedByDescending { it.date })
    }

    /** Quick check: if more than 5% of the first 4KB are non-printable, treat as binary. */
    private fun looksBinary(text: String): Boolean {
        val sample = text.take(4096)
        if (sample.isEmpty()) return false
        var nonPrintable = 0
        for (ch in sample) {
            val code = ch.code
            if (code == 9 || code == 10 || code == 13) continue
            if (code < 32 || code == 0xFFFD) nonPrintable++
        }
        return nonPrintable.toDouble() / sample.length > 0.05
    }

    private fun splitCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < line.length) {
            val ch = line[index]
            when {
                ch == '"' && inQuotes && line.getOrNull(index + 1) == '"' -> {
                    current.append('"')
                    index++
                }
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    out += current.toString().trim()
                    current.clear()
                }
                else -> current.append(ch)
            }
            index++
        }
        out += current.toString().trim()
        return out
    }

    private fun parseDate(raw: String?): LocalDate? {
        val s = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        for (fmt in dateFormats) {
            runCatching { return LocalDate.parse(s, fmt) }
        }
        runCatching { return LocalDate.parse(s) }
        return null
    }

    private fun parseAmount(raw: String?): Double? {
        val s = raw?.trim() ?: return null
        if (s.isEmpty() || s == "-") return null
        val negative = s.startsWith("(") && s.endsWith(")")
        val cleaned = s
            .removePrefix("(").removeSuffix(")")
            .replace("[,\\s₹$€£]".toRegex(), "")
            .replace("Rs.", "", ignoreCase = true)
            .replace("INR", "", ignoreCase = true)
            .replace("USD", "", ignoreCase = true)
        val value = cleaned.toDoubleOrNull() ?: return null
        return if (negative) -value else value
    }
}
