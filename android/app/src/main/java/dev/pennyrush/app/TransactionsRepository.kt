package dev.pennyrush.app

import dev.pennyrush.feature.home.MerchantExtractor
import dev.pennyrush.feature.home.Transaction
import dev.pennyrush.feature.home.TransactionKind
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.security.MessageDigest
import java.time.LocalDate
import kotlin.math.abs
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class AccountRow(
    val id: String,
)

@Serializable
private data class AccountInsert(
    @SerialName("user_id") val userId: String,
    val name: String,
    val type: String,
    val currency: String,
    val color: String,
    val icon: String,
)

@Serializable
private data class TransactionRow(
    val id: String,
    val amount: Double,
    val type: String,
    val date: String,
    val merchant: String? = null,
    val note: String? = null,
    @SerialName("imported_hash") val importedHash: String? = null,
)

@Serializable
private data class TransactionInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("account_id") val accountId: String,
    val amount: Double,
    val type: String,
    val date: String,
    val merchant: String? = null,
    val note: String? = null,
    val source: String = "manual",
    @SerialName("imported_hash") val importedHash: String? = null,
)

class TransactionsRepository(private val supabase: SupabaseClient) {

    suspend fun ensureAccount(userId: String): String {
        val existing = supabase.from("accounts")
            .select(columns = Columns.list("id")) {
                filter { eq("user_id", userId) }
                limit(1)
            }
            .decodeList<AccountRow>()
        existing.firstOrNull()?.let { return it.id }

        val inserted = supabase.from("accounts")
            .insert(
                AccountInsert(
                    userId = userId,
                    name = "Primary",
                    type = "bank",
                    currency = "INR",
                    color = "#10B981",
                    icon = "bank",
                ),
            ) { select(columns = Columns.list("id")) }
            .decodeSingle<AccountRow>()
        return inserted.id
    }

    suspend fun listForUser(userId: String): List<Transaction> {
        val rows = supabase.from("transactions")
            .select(columns = Columns.list("id, amount, type, date, merchant, note, imported_hash")) {
                filter { eq("user_id", userId) }
                order("date", Order.DESCENDING)
                limit(500)
            }
            .decodeList<TransactionRow>()
        return rows.map { it.toAppModel() }
    }

    suspend fun insertOne(
        userId: String,
        accountId: String,
        transaction: Transaction,
    ): Transaction {
        val payload = transaction.toInsert(userId, accountId, source = "manual")
        val inserted = supabase.from("transactions")
            .insert(payload) { select(columns = Columns.list("id, amount, type, date, merchant, note, imported_hash")) }
            .decodeSingle<TransactionRow>()
        return inserted.toAppModel()
    }

    suspend fun insertBatch(
        userId: String,
        accountId: String,
        transactions: List<Transaction>,
    ): List<Transaction> {
        if (transactions.isEmpty()) return emptyList()
        val payload = transactions.map { it.toInsert(userId, accountId, source = "import") }
        val inserted = supabase.from("transactions")
            .insert(payload) { select(columns = Columns.list("id, amount, type, date, merchant, note, imported_hash")) }
            .decodeList<TransactionRow>()
        return inserted.map { it.toAppModel() }
    }

    suspend fun existingImportedHashesFor(userId: String, hashes: List<String>): Set<String> {
        if (hashes.isEmpty()) return emptySet()
        val rows = supabase.from("transactions")
            .select(columns = Columns.list("imported_hash")) {
                filter {
                    eq("user_id", userId)
                    isIn("imported_hash", hashes)
                }
            }
            .decodeList<TransactionRow>()
        return rows.mapNotNull { it.importedHash }.toSet()
    }
}

private fun TransactionRow.toAppModel(): Transaction {
    val raw = (merchant ?: note ?: "Transaction").trim()
    val analysis = MerchantExtractor.analyze(raw)
    val signed = if (type == "expense") -amount else amount
    return Transaction(
        id = id,
        date = LocalDate.parse(date),
        description = raw,
        merchant = analysis.merchant,
        amount = signed,
        kind = analysis.kind,
    )
}

private fun Transaction.toInsert(
    userId: String,
    accountId: String,
    source: String,
): TransactionInsert = TransactionInsert(
    userId = userId,
    accountId = accountId,
    amount = abs(amount),
    type = if (amount < 0) "expense" else "income",
    date = date.toString(),
    merchant = description.takeIf { it.isNotBlank() },
    note = description.takeIf { it.isNotBlank() },
    source = source,
    importedHash = importHash(this),
)

private fun importHash(t: Transaction): String {
    val md = MessageDigest.getInstance("MD5")
    val raw = "${t.date}|${t.amount}|${t.description.lowercase().trim()}"
    val bytes = md.digest(raw.toByteArray())
    return "app-" + bytes.joinToString("") { "%02x".format(it) }
}
