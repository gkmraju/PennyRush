package dev.pennyrush.feature.home

import java.time.LocalDate
import java.util.UUID

data class BudgetLimit(
    val category: String,
    val limit: Double,
)

data class SavingsGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: LocalDate? = null,
)

class PlanningSync(
    val enabled: Boolean = false,
    val loadBudgets: suspend () -> List<BudgetLimit> = { emptyList() },
    val saveBudget: suspend (BudgetLimit) -> BudgetLimit = { it },
    val loadGoals: suspend () -> List<SavingsGoal> = { emptyList() },
    val saveGoal: suspend (SavingsGoal) -> SavingsGoal = { it },
    val deleteGoal: suspend (String) -> Unit = {},
)
