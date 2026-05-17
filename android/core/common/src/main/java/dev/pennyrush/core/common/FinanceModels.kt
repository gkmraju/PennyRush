package dev.pennyrush.core.common

import kotlinx.serialization.Serializable

@Serializable
data class AccountSummary(
    val id: String,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val currency: String,
)

@Serializable
data class TransactionSummary(
    val id: String,
    val merchant: String,
    val category: String,
    val amount: Double,
    val dateLabel: String,
)

@Serializable
data class ImportCandidate(
    val id: String,
    val date: String,
    val merchant: String,
    val amount: Double,
    val type: TransactionType,
    val note: String? = null,
)

@Serializable
enum class AccountType {
    BANK,
    WALLET,
    CASH,
    CREDIT_CARD,
}

@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
}
