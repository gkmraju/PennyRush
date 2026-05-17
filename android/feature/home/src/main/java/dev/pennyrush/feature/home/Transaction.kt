package dev.pennyrush.feature.home

import java.time.LocalDate
import java.util.UUID

enum class TransactionKind {
    UPI,
    Card,
    Transfer,
    ATM,
    Cash,
    Bill,
    Salary,
    Other,
}

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val description: String,
    val merchant: String,
    val amount: Double,
    val kind: TransactionKind = TransactionKind.Other,
)
