package dev.pennyrush.feature.home

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class AddResult(val added: Int, val duplicates: Int)

object TransactionsStore {
    private val items: SnapshotStateList<Transaction> = mutableStateListOf()

    val transactions: List<Transaction> get() = items

    fun add(transaction: Transaction) {
        items.add(0, transaction)
    }

    fun replaceAll(transactions: List<Transaction>) {
        items.clear()
        items.addAll(transactions)
    }

    fun update(transaction: Transaction) {
        val index = items.indexOfFirst { it.id == transaction.id }
        if (index >= 0) {
            items[index] = transaction
        }
    }

    fun delete(id: String) {
        items.removeAll { it.id == id }
    }

    fun addAll(transactions: List<Transaction>): AddResult {
        val existing = items.mapTo(HashSet()) { it.dedupKey() }
        var added = 0
        var duplicates = 0
        val accepted = mutableListOf<Transaction>()
        for (t in transactions) {
            val key = t.dedupKey()
            if (existing.add(key)) {
                accepted += t
                added++
            } else {
                duplicates++
            }
        }
        items.addAll(0, accepted)
        return AddResult(added, duplicates)
    }

    fun clear() {
        items.clear()
    }

    private fun Transaction.dedupKey(): String =
        "$date|$amount|${description.lowercase().trim()}"
}
