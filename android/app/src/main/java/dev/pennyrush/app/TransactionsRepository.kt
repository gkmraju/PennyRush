package dev.pennyrush.app

import dev.pennyrush.feature.home.CategorizationRules
import dev.pennyrush.feature.home.ImportIdentity
import dev.pennyrush.feature.home.MerchantExtractor
import dev.pennyrush.feature.home.BudgetLimit
import dev.pennyrush.feature.home.SavingsGoal
import dev.pennyrush.feature.home.Transaction
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class AccountRow(
    val id: String,
)

@Serializable
private data class ProfileRow(
    val currency: String? = null,
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
private data class CategoryRow(
    val id: String,
    val name: String,
)

@Serializable
private data class CategoryInsert(
    @SerialName("user_id") val userId: String,
    val name: String,
    val icon: String = "circle",
    val color: String = "#E5E7EB",
)

@Serializable
private data class BudgetRow(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    val amount: Double,
    @SerialName("start_date") val startDate: String,
)

@Serializable
private data class BudgetInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("category_id") val categoryId: String,
    val amount: Double,
    val period: String = "monthly",
    @SerialName("start_date") val startDate: String,
    val rollover: Boolean = false,
)

@Serializable
private data class BudgetUpdate(
    val amount: Double,
)

@Serializable
private data class GoalRow(
    val id: String,
    val name: String,
    @SerialName("target_amount") val targetAmount: Double,
    @SerialName("current_amount") val currentAmount: Double,
    @SerialName("target_date") val targetDate: String? = null,
)

@Serializable
private data class GoalInsert(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("target_amount") val targetAmount: Double,
    @SerialName("current_amount") val currentAmount: Double,
    @SerialName("target_date") val targetDate: String? = null,
)

@Serializable
private data class GoalUpdate(
    val name: String,
    @SerialName("target_amount") val targetAmount: Double,
    @SerialName("current_amount") val currentAmount: Double,
    @SerialName("target_date") val targetDate: String? = null,
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
    @SerialName("category_id") val categoryId: String? = null,
    val amount: Double,
    val type: String,
    val date: String,
    val merchant: String? = null,
    val note: String? = null,
    val source: String = "manual",
    @SerialName("ai_confidence") val aiConfidence: Double? = null,
    @SerialName("imported_hash") val importedHash: String? = null,
)

@Serializable
private data class TransactionUpdate(
    @SerialName("category_id") val categoryId: String? = null,
    val amount: Double,
    val type: String,
    val date: String,
    val merchant: String? = null,
    val note: String? = null,
    @SerialName("ai_confidence") val aiConfidence: Double? = null,
)

class TransactionsRepository(private val supabase: SupabaseClient) {
    private val categoryCache = mutableMapOf<String, List<CategoryRow>>()

    suspend fun ensureAccount(userId: String): String {
        val existing = supabase.from("accounts")
            .select(columns = Columns.list("id")) {
                filter { eq("user_id", userId) }
                limit(1)
            }
            .decodeList<AccountRow>()
        existing.firstOrNull()?.let { return it.id }

        val currency = profileCurrency(userId)
        val inserted = supabase.from("accounts")
            .insert(
                AccountInsert(
                    userId = userId,
                    name = "Primary",
                    type = "bank",
                    currency = currency,
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
        val categories = categoriesForUser(userId)
        val payload = transaction.toInsert(userId, accountId, source = "manual", categories = categories)
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
        val categories = categoriesForUser(userId)
        val payload = transactions.map { it.toInsert(userId, accountId, source = "import", categories = categories) }
        val existingHashes = existingImportedHashesFor(userId, payload.mapNotNull { it.importedHash })
        val filteredPayload = payload.filter { it.importedHash == null || it.importedHash !in existingHashes }
        if (filteredPayload.isEmpty()) return emptyList()

        val inserted = supabase.from("transactions")
            .insert(filteredPayload) { select(columns = Columns.list("id, amount, type, date, merchant, note, imported_hash")) }
            .decodeList<TransactionRow>()
        return inserted.map { it.toAppModel() }
    }

    suspend fun updateOne(
        userId: String,
        transaction: Transaction,
    ): Transaction {
        val categories = categoriesForUser(userId)
        val payload = transaction.toUpdate(categories)
        val updated = supabase.from("transactions")
            .update(payload) {
                filter {
                    eq("id", transaction.id)
                    eq("user_id", userId)
                }
                select(columns = Columns.list("id, amount, type, date, merchant, note, imported_hash"))
            }
            .decodeSingle<TransactionRow>()
        return updated.toAppModel()
    }

    suspend fun deleteOne(userId: String, transactionId: String) {
        supabase.from("transactions").delete {
            filter {
                eq("id", transactionId)
                eq("user_id", userId)
            }
        }
    }

    suspend fun listBudgets(userId: String): List<BudgetLimit> {
        val categories = categoriesForUser(userId)
        val categoryById = categories.associateBy { it.id }
        val rows = supabase.from("budgets")
            .select(columns = Columns.list("id, category_id, amount, start_date")) {
                filter {
                    eq("user_id", userId)
                    eq("period", "monthly")
                    eq("start_date", YearMonth.now().atDay(1).toString())
                }
            }
            .decodeList<BudgetRow>()
        return rows.mapNotNull { row ->
            categoryById[row.categoryId]?.name?.let { BudgetLimit(it, row.amount) }
        }
    }

    suspend fun saveBudget(userId: String, budget: BudgetLimit): BudgetLimit {
        val category = ensureCategory(userId, budget.category)
        val startDate = YearMonth.now().atDay(1).toString()
        val existing = supabase.from("budgets")
            .select(columns = Columns.list("id")) {
                filter {
                    eq("user_id", userId)
                    eq("category_id", category.id)
                    eq("period", "monthly")
                    eq("start_date", startDate)
                }
                limit(1)
            }
            .decodeList<AccountRow>()
        if (existing.firstOrNull() != null) {
            supabase.from("budgets").update(BudgetUpdate(budget.limit)) {
                filter {
                    eq("id", existing.first().id)
                    eq("user_id", userId)
                }
            }
        } else {
            supabase.from("budgets").insert(
                BudgetInsert(
                    userId = userId,
                    categoryId = category.id,
                    amount = budget.limit,
                    startDate = startDate,
                ),
            )
        }
        return budget
    }

    suspend fun listGoals(userId: String): List<SavingsGoal> {
        val rows = supabase.from("goals")
            .select(columns = Columns.list("id, name, target_amount, current_amount, target_date")) {
                filter { eq("user_id", userId) }
                order("target_date", Order.ASCENDING)
            }
            .decodeList<GoalRow>()
        return rows.map { it.toAppModel() }
    }

    suspend fun saveGoal(userId: String, goal: SavingsGoal): SavingsGoal {
        val existing = supabase.from("goals")
            .select(columns = Columns.list("id")) {
                filter {
                    eq("id", goal.id)
                    eq("user_id", userId)
                }
                limit(1)
            }
            .decodeList<AccountRow>()
        val row = if (existing.firstOrNull() != null) {
            supabase.from("goals")
                .update(goal.toUpdate()) {
                    filter {
                        eq("id", goal.id)
                        eq("user_id", userId)
                    }
                    select(columns = Columns.list("id, name, target_amount, current_amount, target_date"))
                }
                .decodeSingle<GoalRow>()
        } else {
            supabase.from("goals")
                .insert(goal.toInsert(userId)) {
                    select(columns = Columns.list("id, name, target_amount, current_amount, target_date"))
                }
                .decodeSingle<GoalRow>()
        }
        return row.toAppModel()
    }

    suspend fun deleteGoal(userId: String, goalId: String) {
        supabase.from("goals").delete {
            filter {
                eq("id", goalId)
                eq("user_id", userId)
            }
        }
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

    private suspend fun profileCurrency(userId: String): String {
        val rows = supabase.from("profiles")
            .select(columns = Columns.list("currency")) {
                filter { eq("id", userId) }
                limit(1)
            }
            .decodeList<ProfileRow>()
        return rows.firstOrNull()?.currency?.takeIf { it.isNotBlank() } ?: "USD"
    }

    private suspend fun categoriesForUser(userId: String): List<CategoryRow> {
        categoryCache[userId]?.let { return it }
        val rows = supabase.from("categories")
            .select(columns = Columns.list("id, name")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<CategoryRow>()
        categoryCache[userId] = rows
        return rows
    }

    private suspend fun ensureCategory(userId: String, name: String): CategoryRow {
        categoriesForUser(userId)
            .firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?.let { return it }
        val inserted = supabase.from("categories")
            .insert(
                CategoryInsert(
                    userId = userId,
                    name = name,
                    color = categoryColor(name),
                ),
            ) { select(columns = Columns.list("id, name")) }
            .decodeSingle<CategoryRow>()
        categoryCache.remove(userId)
        return inserted
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
    categories: List<CategoryRow>,
): TransactionInsert = TransactionInsert(
    userId = userId,
    accountId = accountId,
    categoryId = categoryIdFor(categories),
    amount = abs(amount),
    type = if (amount < 0) "expense" else "income",
    date = date.toString(),
    merchant = description.takeIf { it.isNotBlank() },
    note = description.takeIf { it.isNotBlank() },
    source = source,
    aiConfidence = categoryIdFor(categories)?.let { 0.86 },
    importedHash = if (source == "import") ImportIdentity.hash(this) else null,
)

private fun Transaction.toUpdate(categories: List<CategoryRow>): TransactionUpdate = TransactionUpdate(
    categoryId = categoryIdFor(categories),
    amount = abs(amount),
    type = if (amount < 0) "expense" else "income",
    date = date.toString(),
    merchant = description.takeIf { it.isNotBlank() },
    note = description.takeIf { it.isNotBlank() },
    aiConfidence = categoryIdFor(categories)?.let { 0.86 },
)

private fun Transaction.categoryIdFor(categories: List<CategoryRow>): String? {
    val target = CategorizationRules.categoryNameFor(this)
    return categories.firstOrNull { it.name.equals(target, ignoreCase = true) }?.id
        ?: categories.firstOrNull { it.name.equals("Other", ignoreCase = true) }?.id
}

private fun GoalRow.toAppModel(): SavingsGoal = SavingsGoal(
    id = id,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    targetDate = targetDate?.let { LocalDate.parse(it) },
)

private fun SavingsGoal.toInsert(userId: String): GoalInsert = GoalInsert(
    id = id,
    userId = userId,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    targetDate = targetDate?.toString(),
)

private fun SavingsGoal.toUpdate(): GoalUpdate = GoalUpdate(
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    targetDate = targetDate?.toString(),
)

private fun categoryColor(name: String): String = when (name) {
    "Food", "Groceries" -> "#0F766E"
    "Transport", "Fuel", "Travel" -> "#1D4ED8"
    "Bills", "Subscriptions" -> "#6D28D9"
    "Shopping", "Entertainment" -> "#B45309"
    "Health" -> "#BE123C"
    else -> "#64748B"
}
