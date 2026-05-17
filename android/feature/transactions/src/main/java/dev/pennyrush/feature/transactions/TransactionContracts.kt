package dev.pennyrush.feature.transactions

import dev.pennyrush.core.common.ImportCandidate

sealed interface TransactionCommand {
    data class SaveManualTransaction(val merchant: String, val amount: Double) : TransactionCommand
    data class SaveImportedTransactions(val candidates: List<ImportCandidate>) : TransactionCommand
}
