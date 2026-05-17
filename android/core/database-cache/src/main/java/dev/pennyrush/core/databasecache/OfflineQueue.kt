package dev.pennyrush.core.databasecache

data class OfflineWrite(
    val id: String,
    val table: String,
    val operation: Operation,
    val payloadJson: String,
    val createdAtMillis: Long,
)

enum class Operation {
    INSERT,
    UPDATE,
    DELETE,
}
