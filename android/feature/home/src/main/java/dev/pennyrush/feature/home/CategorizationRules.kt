package dev.pennyrush.feature.home

object CategorizationRules {
    private data class Rule(val category: String, val terms: List<String>)

    private val rules = listOf(
        Rule("Food", listOf("swiggy", "zomato", "restaurant", "cafe", "coffee", "starbucks")),
        Rule("Groceries", listOf("grocery", "supermarket", "bigbasket", "blinkit", "instamart", "dmart")),
        Rule("Transport", listOf("uber", "ola", "metro", "rapido", "taxi")),
        Rule("Fuel", listOf("fuel", "petrol", "shell", "indianoil", "bharat petroleum")),
        Rule("Shopping", listOf("amazon", "flipkart", "myntra", "nykaa", "store")),
        Rule("Bills", listOf("electricity", "water bill", "broadband", "airtel", "jio", "postpaid")),
        Rule("Entertainment", listOf("netflix", "spotify", "prime video", "bookmyshow", "hotstar")),
        Rule("Subscriptions", listOf("subscription", "membership", "icloud", "google one", "notion")),
        Rule("Health", listOf("pharmacy", "apollo", "hospital", "clinic", "diagnostic")),
        Rule("Travel", listOf("makemytrip", "goibibo", "airline", "hotel", "airbnb")),
        Rule("Investment", listOf("zerodha", "groww", "coin", "mutual fund", "sip")),
        Rule("Rent", listOf("rent", "landlord")),
    )

    fun categoryNameFor(transaction: Transaction): String {
        if (transaction.amount > 0 || transaction.kind == TransactionKind.Salary) return "Income"
        return when (transaction.kind) {
            TransactionKind.ATM -> "ATM"
            TransactionKind.Bill -> "Bills"
            TransactionKind.Transfer -> "Transfer"
            TransactionKind.Salary -> "Income"
            else -> categoryNameForText(transaction.merchant.ifBlank { transaction.description })
        }
    }

    fun categoryNameForText(value: String): String {
        val normalized = value.lowercase()
        return rules.firstOrNull { rule ->
            rule.terms.any { term -> normalized.contains(term) }
        }?.category ?: "Other"
    }

    fun visibleCategories(): List<String> =
        (rules.map { it.category } + listOf("Income", "ATM", "Transfer", "Other")).distinct().sorted()
}
