package dev.pennyrush.feature.home

/**
 * Indirection so feature/home doesn't depend on Supabase. The app module wires real
 * implementations that talk to Postgrest; tests/previews can leave the defaults.
 */
class TransactionsSync(
    val enabled: Boolean = false,
    val loadAll: suspend () -> List<Transaction> = { emptyList() },
    val persistOne: suspend (Transaction) -> Transaction = { it },
    val persistBatch: suspend (List<Transaction>) -> List<Transaction> = { it },
    val updateOne: suspend (Transaction) -> Transaction = { it },
    val deleteOne: suspend (String) -> Unit = {},
)
