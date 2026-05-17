package dev.pennyrush.core.network

data class SupabaseConfig(
    val url: String,
    val anonKey: String,
)

interface PennyrushBackend {
    suspend fun syncNow()
    suspend fun categorizeUnknownTransactions()
}
