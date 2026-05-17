package dev.pennyrush.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.pennyrush.core.common.MoneyFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        },
        bottomBar = { HomeBottomBar() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { Header() }
            item { HeroBalance() }
            item { MetricRow() }
            item { SpendingCard() }
            item { InsightsStrip() }
            item {
                Text(
                    text = "Recent transactions",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            items(recentTransactions) { transaction ->
                TransactionRow(transaction)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun Header() {
    Column {
        Text(
            text = "Good afternoon · ${LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d"))}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = "Your money, live",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
private fun HeroBalance() {
    Column {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                text = "Net worth",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = MoneyFormatter.format(48210.0),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Up ${MoneyFormatter.format(1240.0)} this month after bills and goals.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun MetricRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile(label = "Income", amount = 6400.0, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
        MetricTile(label = "Expenses", amount = 4560.0, color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
        MetricTile(label = "Saved", amount = 1840.0, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MetricTile(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(96.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(text = MoneyFormatter.format(amount), color = color, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun SpendingCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = "Spending", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Text(text = "May categories", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(14.dp))
                spendingSlices.take(4).forEach { slice ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(slice.color, CircleShape),
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = slice.name,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            SpendingRing()
        }
    }
}

@Composable
private fun SpendingRing() {
    Canvas(modifier = Modifier.size(132.dp)) {
        val strokeWidth = 18.dp.toPx()
        var startAngle = -90f
        val total = spendingSlices.sumOf { it.amount }
        spendingSlices.forEach { slice ->
            val sweep = (slice.amount / total * 360f).toFloat()
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun InsightsStrip() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InsightTile("Dining is up", "Food spend is 28% above average.", Modifier.weight(1f))
        InsightTile("Bills ahead", "Two recurring payments land this week.", Modifier.weight(1f))
    }
}

@Composable
private fun InsightTile(title: String, body: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(132.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun TransactionRow(transaction: RecentTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(transaction.color, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = transaction.category.take(1), color = Color(0xFF0A0A0A), fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(text = transaction.merchant, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = transaction.category, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        }
        Text(
            text = MoneyFormatter.format(transaction.amount),
            style = MaterialTheme.typography.bodyLarge,
            color = if (transaction.amount > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HomeBottomBar() {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("Home", "Transactions", "Insights", "More").forEach { label ->
                Text(
                    text = label,
                    color = if (label == "Home") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

private data class SpendingSlice(val name: String, val amount: Double, val color: Color)

private val spendingSlices = listOf(
    SpendingSlice("Food", 920.0, Color(0xFFF5B82E)),
    SpendingSlice("Rent", 1700.0, Color(0xFFA3A3A3)),
    SpendingSlice("Transport", 380.0, Color(0xFF60A5FA)),
    SpendingSlice("Shopping", 640.0, Color(0xFFF9A8D4)),
    SpendingSlice("Bills", 520.0, Color(0xFFA78BFA)),
)

private data class RecentTransaction(
    val merchant: String,
    val category: String,
    val amount: Double,
    val color: Color,
)

private val recentTransactions = listOf(
    RecentTransaction("Whole Foods", "Groceries", -84.0, Color(0xFFD9F99D)),
    RecentTransaction("Payroll", "Income", 3200.0, Color(0xFFBBF7D0)),
    RecentTransaction("Uber", "Transport", -24.0, Color(0xFFBFDBFE)),
    RecentTransaction("Netflix", "Subscriptions", -18.0, Color(0xFFE9D5FF)),
)
