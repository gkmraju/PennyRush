package dev.pennyrush.feature.home

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.pennyrush.core.common.MoneyFormatter
import dev.pennyrush.core.designsystem.ThemeMode
import dev.pennyrush.core.designsystem.ThemePreferences
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─── Design tokens ─────────────────────────────────────────────────────────────

private val CardShape = RoundedCornerShape(24.dp)
private val ButtonShape = RoundedCornerShape(16.dp)
private val ChipShape = RoundedCornerShape(10.dp)
private val InputShape = RoundedCornerShape(14.dp)
private val ButtonHeight = 52.dp
private val Income = Color(0xFF10B981)
private val Expense = Color(0xFFEF4444)

// ─── Reusable primitives ───────────────────────────────────────────────────────

@Composable
private fun PrCard(
    modifier: Modifier = Modifier,
    padding: Int = 20,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = CardShape,
        color = color,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Column(modifier = Modifier.padding(padding.dp), content = content)
    }
}

@Composable
private fun PrButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(ButtonHeight),
        shape = ButtonShape,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun PrSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(ButtonHeight),
        shape = ButtonShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun KindChip(kind: TransactionKind) {
    val (label, tint) = kindStyle(kind)
    Surface(
        shape = ChipShape,
        color = tint.copy(alpha = 0.15f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = tint,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 0.6.sp,
            ),
        )
    }
}

private fun kindStyle(kind: TransactionKind): Pair<String, Color> = when (kind) {
    TransactionKind.UPI -> "UPI" to Color(0xFFA78BFA)
    TransactionKind.Card -> "CARD" to Color(0xFF60A5FA)
    TransactionKind.Transfer -> "TRANSFER" to Color(0xFF22D3EE)
    TransactionKind.ATM -> "ATM" to Color(0xFFFBBF24)
    TransactionKind.Salary -> "SALARY" to Color(0xFF10B981)
    TransactionKind.Bill -> "BILL" to Color(0xFFFB7185)
    TransactionKind.Cash -> "CASH" to Color(0xFF94A3B8)
    TransactionKind.Other -> "OTHER" to Color(0xFF94A3B8)
}

private fun accentForKind(kind: TransactionKind): Color = kindStyle(kind).second

// ─── Route ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    userEmail: String? = null,
    userName: String? = null,
    userAvatarUrl: String? = null,
    sync: TransactionsSync = TransactionsSync(),
    onSignOut: suspend () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDestination by rememberSaveable { mutableStateOf(HomeDestination.Home) }
    var showAddSheet by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<StatementPreviewState?>(null) }
    var isLoading by remember { mutableStateOf(sync.enabled) }

    LaunchedEffect(sync) {
        if (!sync.enabled) {
            isLoading = false
            return@LaunchedEffect
        }
        runCatching { sync.loadAll() }
            .onSuccess { TransactionsStore.replaceAll(it) }
            .onFailure {
                Toast.makeText(
                    context,
                    "Couldn't load transactions: ${it.message ?: "unknown error"}",
                    Toast.LENGTH_LONG,
                ).show()
            }
        isLoading = false
    }

    val statementPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val name = context.displayNameFor(uri)
        preview = StatementPreviewState.Loading(name)
        scope.launch {
            preview = parseStatement(context, uri, name)
        }
    }

    val openImport: () -> Unit = {
        statementPicker.launch(
            arrayOf(
                "text/csv",
                "text/comma-separated-values",
                "application/pdf",
                "text/*",
                "application/*",
            ),
        )
    }

    preview?.let { state ->
        StatementPreviewScreen(
            state = state,
            onCancel = { preview = null },
            onImport = { transactions ->
                scope.launch {
                    val existing = TransactionsStore.transactions
                    val seen = existing.mapTo(HashSet()) {
                        "${it.date}|${it.amount}|${it.description.lowercase().trim()}"
                    }
                    val (toInsert, duplicates) = transactions.partition {
                        "${it.date}|${it.amount}|${it.description.lowercase().trim()}" !in seen
                    }
                    val persisted = if (sync.enabled && toInsert.isNotEmpty()) {
                        runCatching { sync.persistBatch(toInsert) }
                            .getOrElse {
                                Toast.makeText(
                                    context,
                                    "Couldn't sync to server: ${it.message ?: "unknown error"}",
                                    Toast.LENGTH_LONG,
                                ).show()
                                return@launch
                            }
                    } else {
                        toInsert
                    }
                    TransactionsStore.addAll(persisted)
                    val msg = buildString {
                        append("Imported ${persisted.size} transaction")
                        if (persisted.size != 1) append("s")
                        if (duplicates.isNotEmpty()) {
                            append(" · ${duplicates.size} duplicate")
                            if (duplicates.size != 1) append("s")
                            append(" skipped")
                        }
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    preview = null
                }
            },
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (selectedDestination == HomeDestination.Home) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = {
            HomeBottomBar(
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it },
            )
        },
    ) { padding ->
        when (selectedDestination) {
            HomeDestination.Home -> HomeContent(
                userEmail = userEmail,
                userName = userName,
                userAvatarUrl = userAvatarUrl,
                onImportStatement = openImport,
                onAddManually = { showAddSheet = true },
                onSignOut = onSignOut,
                modifier = Modifier.padding(padding),
            )
            HomeDestination.Transactions -> TransactionsContent(modifier = Modifier.padding(padding))
            HomeDestination.Insights -> InsightsContent(modifier = Modifier.padding(padding))
            HomeDestination.More -> MoreContent(
                userEmail = userEmail,
                userName = userName,
                userAvatarUrl = userAvatarUrl,
                onSignOut = onSignOut,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (showAddSheet) {
        QuickAddSheet(
            onImportStatement = {
                showAddSheet = false
                openImport()
            },
            onSave = { transaction ->
                scope.launch {
                    val saved = if (sync.enabled) {
                        runCatching { sync.persistOne(transaction) }
                            .getOrElse {
                                Toast.makeText(
                                    context,
                                    "Couldn't save: ${it.message ?: "unknown error"}",
                                    Toast.LENGTH_LONG,
                                ).show()
                                return@launch
                            }
                    } else {
                        transaction
                    }
                    TransactionsStore.add(saved)
                    showAddSheet = false
                }
            },
            onDismiss = { showAddSheet = false },
        )
    }
}

// ─── Home tab ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeContent(
    userEmail: String?,
    userName: String?,
    userAvatarUrl: String?,
    onImportStatement: () -> Unit,
    onAddManually: () -> Unit,
    onSignOut: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transactions = TransactionsStore.transactions
    val isEmpty = transactions.isEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            Header(
                userEmail = userEmail,
                userName = userName,
                userAvatarUrl = userAvatarUrl,
                onSignOut = onSignOut,
            )
        }
        if (isEmpty) {
            item {
                EmptyHomeCard(
                    onImport = onImportStatement,
                    onAddManually = onAddManually,
                )
            }
        } else {
            item { WalletHero(transactions) }
            item {
                QuickActions(
                    onAdd = onAddManually,
                    onImport = onImportStatement,
                )
            }
            item { StatsStrip(transactions) }
            item { SpendingBreakdown(transactions) }

            val today = LocalDate.now()
            val sorted = transactions.sortedByDescending { it.date }
            val groups = sorted.groupBy { bucketLabel(it.date, today) }
            groups.forEach { (label, items) ->
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    SectionLabel(label)
                }
                items(items) { transaction ->
                    TransactionRow(transaction)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

private fun bucketLabel(date: LocalDate, today: LocalDate): String = when {
    date == today -> "Today"
    date == today.minusDays(1) -> "Yesterday"
    date.isAfter(today.minusDays(7)) -> "This week"
    date.isAfter(today.minusDays(30)) -> "This month"
    else -> date.format(DateTimeFormatter.ofPattern("MMM yyyy"))
}

@Composable
private fun Header(
    userEmail: String?,
    userName: String?,
    userAvatarUrl: String?,
    onSignOut: suspend () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greetingForNow() + " · " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = userName?.substringBefore(' ')?.let { "Hi, $it" } ?: "Welcome",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        ProfileAvatar(
            userEmail = userEmail,
            userName = userName,
            userAvatarUrl = userAvatarUrl,
            onSignOut = onSignOut,
        )
    }
}

@Composable
private fun ProfileAvatar(
    userEmail: String?,
    userName: String?,
    userAvatarUrl: String?,
    onSignOut: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var menuOpen by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier.size(44.dp).clip(CircleShape),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            onClick = { menuOpen = true },
        ) {
            if (!userAvatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = userAvatarUrl,
                    contentDescription = userName ?: userEmail,
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initialsFor(userName, userEmail),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                userName?.let {
                    Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                userEmail?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            DropdownMenuItem(
                text = { Text("Sign out") },
                onClick = {
                    menuOpen = false
                    scope.launch { onSignOut() }
                },
            )
        }
    }
}

@Composable
private fun EmptyHomeCard(
    onImport: () -> Unit,
    onAddManually: () -> Unit,
) {
    PrCard(padding = 24) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Start tracking your money",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            ),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Upload a bank statement CSV (PhonePe, Paytm, HDFC, ICICI, SBI all export this) or add transactions one at a time. PennyRush surfaces trends and recurring charges as soon as it has data.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(20.dp))
        PrButton("Import statement", onClick = onImport, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        PrSecondaryButton("Add manually", onClick = onAddManually, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun WalletHero(transactions: List<Transaction>, accountName: String = "Primary balance") {
    val net = transactions.sumOf { it.amount }
    val now = YearMonth.now()
    val thisMonth = transactions.filter { YearMonth.from(it.date) == now }
    val income = thisMonth.filter { it.amount > 0 }.sumOf { it.amount }
    val expenses = thisMonth.filter { it.amount < 0 }.sumOf { abs(it.amount) }
    val delta = income - expenses
    val positive = delta >= 0
    val savingsRate = if (income > 0) (delta / income).coerceIn(0.0, 1.0) else 0.0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF065F46),
                            Color(0xFF059669),
                            Color(0xFF10B981),
                        ),
                    ),
                    shape = RoundedCornerShape(28.dp),
                ),
        ) {
            // decorative orb
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .offset(x = 180.dp, y = (-80).dp)
                    .background(Color.White.copy(alpha = 0.07f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-30).dp, y = 120.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape),
            )

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = accountName.uppercase(),
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "PennyRush wallet",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            text = "INR",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp,
                            ),
                        )
                    }
                }

                Text(
                    text = MoneyFormatter.format(net),
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-2).sp,
                        fontSize = 46.sp,
                    ),
                )

                if (thisMonth.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.2f),
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            text = "${if (positive) "▲" else "▼"}  ${MoneyFormatter.format(delta, showSign = true)} this month",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }

                    if (income > 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Saved this month",
                                    color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                Text(
                                    text = "${(savingsRate * 100).toInt()}%",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.18f),
                                        RoundedCornerShape(999.dp),
                                    ),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(savingsRate.toFloat().coerceAtLeast(0.02f))
                                        .height(6.dp)
                                        .background(Color.White, RoundedCornerShape(999.dp)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActions(
    onAdd: () -> Unit,
    onImport: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickActionTile("Add", Icons.Rounded.Add, Modifier.weight(1f), onAdd)
        QuickActionTile("Import", Icons.Rounded.FileDownload, Modifier.weight(1f), onImport)
        QuickActionTile("Categories", Icons.Rounded.Category, Modifier.weight(1f)) {}
        QuickActionTile("Reports", Icons.Rounded.BarChart, Modifier.weight(1f)) {}
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun StatsStrip(transactions: List<Transaction>) {
    val now = YearMonth.now()
    val thisMonth = transactions.filter { YearMonth.from(it.date) == now }
    val income = thisMonth.filter { it.amount > 0 }.sumOf { it.amount }
    val expenses = thisMonth.filter { it.amount < 0 }.sumOf { abs(it.amount) }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            label = "Income",
            amount = income,
            accent = Income,
            icon = Icons.Rounded.ArrowDownward,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Spend",
            amount = expenses,
            accent = Expense,
            icon = Icons.Rounded.ArrowUpward,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    amount: Double,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.18f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = accent,
                        )
                    }
                }
                Text(
                    text = label.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
            Text(
                text = MoneyFormatter.compact(amount),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "this month",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun SpendingBreakdown(transactions: List<Transaction>) {
    val byKind = transactions
        .filter { it.amount < 0 }
        .groupBy { it.kind }
        .map { (kind, txns) -> kind to txns.sumOf { abs(it.amount) } }
        .sortedByDescending { it.second }
        .take(6)
    val total = byKind.sumOf { it.second }
    if (byKind.isEmpty() || total == 0.0) return

    PrCard(padding = 20) {
        Text(
            text = "Where your money went",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
            ),
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(132.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(132.dp)) {
                    val strokeWidth = 22.dp.toPx()
                    val gap = 2f
                    var startAngle = -90f
                    byKind.forEach { (kind, amount) ->
                        val sweep = ((amount / total) * 360.0).toFloat() - gap
                        drawArc(
                            color = accentForKind(kind),
                            startAngle = startAngle,
                            sweepAngle = sweep.coerceAtLeast(0f),
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                        startAngle += sweep + gap
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "SPENT",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    Text(
                        MoneyFormatter.compact(total),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                        ),
                    )
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                byKind.forEach { (kind, amount) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(accentForKind(kind), CircleShape),
                        )
                        Text(
                            text = kindStyle(kind).first
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = MoneyFormatter.compact(amount),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    val accent = accentForKind(transaction.kind)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = transaction.merchant.firstOrNull { it.isLetter() }?.uppercaseChar()?.toString() ?: "·",
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = transaction.merchant,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KindChip(transaction.kind)
                Text(
                    text = transaction.date.format(DateTimeFormatter.ofPattern("MMM d")),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Text(
            text = MoneyFormatter.format(transaction.amount, showSign = true),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp,
            ),
            color = if (transaction.amount > 0) Income else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold,
        ),
    )
}

// ─── Other tabs ────────────────────────────────────────────────────────────────

@Composable
private fun HomeBottomBar(
    selectedDestination: HomeDestination,
    onDestinationSelected: (HomeDestination) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        HomeDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == selectedDestination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = { Text(destination.label, fontWeight = FontWeight.Medium) },
            )
        }
    }
}

@Composable
private fun TransactionsContent(modifier: Modifier = Modifier) {
    val transactions = TransactionsStore.transactions
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
            )
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "No transactions yet. Import a statement or add one from Home.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            items(transactions) { TransactionRow(it) }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun InsightsContent(modifier: Modifier = Modifier) {
    val transactions = TransactionsStore.transactions
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
            )
        }
        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "Insights appear once PennyRush has at least a few transactions to analyse.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            val byKind = transactions
                .filter { it.amount < 0 }
                .groupBy { it.kind }
                .map { (kind, txns) -> kind to txns.sumOf { abs(it.amount) } }
                .sortedByDescending { it.second }

            item {
                PrCard {
                    Text(
                        text = "Spend by type",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Spacer(Modifier.height(12.dp))
                    byKind.forEach { (kind, total) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            KindChip(kind)
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = MoneyFormatter.format(total),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Category breakdowns, recurring detection, and AI-generated insights are next on the roadmap.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun MoreContent(
    userEmail: String?,
    userName: String?,
    userAvatarUrl: String?,
    onSignOut: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val themeMode = ThemePreferences.themeMode

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            Text(
                "More",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
            )
        }
        item {
            ProfileCard(
                userEmail = userEmail,
                userName = userName,
                userAvatarUrl = userAvatarUrl,
            )
        }
        item { SectionLabel("Appearance") }
        item {
            ThemeSelector(
                current = themeMode,
                onChange = { ThemePreferences.set(it) },
            )
        }
        item { SectionLabel("Account") }
        item {
            PrSecondaryButton(
                text = "Sign out",
                onClick = { scope.launch { onSignOut() } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Text(
                text = "Budgets, goals, subscriptions, debts, and investments will live here as the next feature modules come online.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun ProfileCard(
    userEmail: String?,
    userName: String?,
    userAvatarUrl: String?,
) {
    PrCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            ) {
                if (!userAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = userAvatarUrl,
                        contentDescription = userName ?: userEmail,
                        modifier = Modifier.size(72.dp).clip(CircleShape),
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initialsFor(userName, userEmail),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = userName ?: "Signed in",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
                userEmail?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSelector(
    current: ThemeMode,
    onChange: (ThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ThemeMode.entries.forEach { mode ->
            val selected = mode == current
            Surface(
                modifier = Modifier.weight(1f).height(52.dp),
                shape = ButtonShape,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (selected) Color.Transparent else MaterialTheme.colorScheme.outline,
                ),
                onClick = { onChange(mode) },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = mode.name,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ─── Quick add sheet ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddSheet(
    onImportStatement: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDismiss: () -> Unit,
) {
    var isExpense by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val amountValue = amount.toDoubleOrNull()?.let { if (isExpense) -abs(it) else abs(it) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Add transaction",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = ButtonShape,
                    color = if (isExpense) Expense.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isExpense) Expense.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                    ),
                    onClick = { isExpense = true },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "Expense",
                            color = if (isExpense) Expense else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = ButtonShape,
                    color = if (!isExpense) Income.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (!isExpense) Income.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                    ),
                    onClick = { isExpense = false },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "Income",
                            color = if (!isExpense) Income else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Amount (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = InputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = InputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )

            PrButton(
                text = "Save transaction",
                onClick = {
                    val value = amountValue ?: return@PrButton
                    onSave(
                        Transaction(
                            date = LocalDate.now(),
                            description = description.trim(),
                            merchant = MerchantExtractor.analyze(description).merchant,
                            amount = value,
                            kind = MerchantExtractor.analyze(description).kind,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amountValue != null && description.isNotBlank(),
            )

            PrSecondaryButton(
                text = "Import statement instead",
                onClick = onImportStatement,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ─── Statement preview ─────────────────────────────────────────────────────────

@Composable
private fun StatementPreviewScreen(
    state: StatementPreviewState,
    onCancel: () -> Unit,
    onImport: (List<Transaction>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    "Statement preview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    state.fileName(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        when (state) {
            is StatementPreviewState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Parsing…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            is StatementPreviewState.PdfNotSupported -> Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "PDF parsing isn't built yet",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    "Most banks let you export the same statement as CSV — try that and re-import. Native PDF parsing is on the roadmap.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                PrButton("Got it", onClick = onCancel, modifier = Modifier.fillMaxWidth())
            }
            is StatementPreviewState.Failed -> Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Couldn't read that file",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    state.reason,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (state.sample.isNotEmpty()) {
                    PrCard(padding = 14) {
                        Text(
                            "First lines we saw:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(6.dp))
                        state.sample.forEach {
                            Text(it, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                PrButton("Choose another file", onClick = onCancel, modifier = Modifier.fillMaxWidth())
            }
            is StatementPreviewState.Success -> {
                val income = state.transactions.filter { it.amount > 0 }.sumOf { it.amount }
                val expenses = state.transactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "${state.transactions.size} transactions",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("In", income, Income, Icons.Rounded.ArrowDownward, Modifier.weight(1f))
                        StatCard("Out", expenses, Expense, Icons.Rounded.ArrowUpward, Modifier.weight(1f))
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 20.dp).padding(top = 16.dp),
                ) {
                    items(state.transactions) { TransactionRow(it) }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PrSecondaryButton(
                        "Cancel",
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                    )
                    PrButton(
                        "Import ${state.transactions.size}",
                        onClick = { onImport(state.transactions) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private sealed interface StatementPreviewState {
    data class Loading(val name: String) : StatementPreviewState
    data class Success(val name: String, val transactions: List<Transaction>) : StatementPreviewState
    data class Failed(val name: String, val reason: String, val sample: List<String>) : StatementPreviewState
    data class PdfNotSupported(val name: String) : StatementPreviewState
}

private fun StatementPreviewState.fileName(): String = when (this) {
    is StatementPreviewState.Loading -> name
    is StatementPreviewState.Success -> name
    is StatementPreviewState.Failed -> name
    is StatementPreviewState.PdfNotSupported -> name
}

/** Decode bytes as text, honouring BOM, then trying strict UTF-8, then Windows-1252. */
private fun decodeBytes(buffer: ByteArray, length: Int): String {
    if (length >= 3 &&
        buffer[0] == 0xEF.toByte() && buffer[1] == 0xBB.toByte() && buffer[2] == 0xBF.toByte()
    ) {
        return String(buffer, 3, length - 3, StandardCharsets.UTF_8)
    }
    if (length >= 2 && buffer[0] == 0xFF.toByte() && buffer[1] == 0xFE.toByte()) {
        return String(buffer, 2, length - 2, StandardCharsets.UTF_16LE)
    }
    if (length >= 2 && buffer[0] == 0xFE.toByte() && buffer[1] == 0xFF.toByte()) {
        return String(buffer, 2, length - 2, StandardCharsets.UTF_16BE)
    }
    val strictUtf8 = runCatching {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(buffer, 0, length))
            .toString()
    }
    return strictUtf8.getOrElse {
        String(buffer, 0, length, Charset.forName("windows-1252"))
    }
}

private suspend fun parseStatement(
    context: Context,
    uri: Uri,
    name: String,
): StatementPreviewState {
    val isPdf = name.endsWith(".pdf", ignoreCase = true) ||
        context.contentResolver.getType(uri)?.contains("pdf", ignoreCase = true) == true
    if (isPdf) return StatementPreviewState.PdfNotSupported(name)

    return withContext(Dispatchers.IO) {
        runCatching {
            val cap = StatementParser.MAX_BYTES
            val buffer = ByteArray(cap + 1)
            val read = context.contentResolver.openInputStream(uri)?.use { stream ->
                var total = 0
                while (total < buffer.size) {
                    val n = stream.read(buffer, total, buffer.size - total)
                    if (n <= 0) break
                    total += n
                }
                total
            } ?: 0
            if (read == 0) {
                return@runCatching StatementPreviewState.Failed(name, "File appears to be empty.", emptyList())
            }
            if (read > cap) {
                return@runCatching StatementPreviewState.Failed(
                    name,
                    "File is larger than 5 MB. A real bank statement is usually a few hundred KB.",
                    emptyList(),
                )
            }
            val text = decodeBytes(buffer, read)
            when (val outcome = StatementParser.parseCsv(text)) {
                is ParseOutcome.Success -> StatementPreviewState.Success(name, outcome.transactions)
                is ParseOutcome.Failed -> StatementPreviewState.Failed(name, outcome.reason, outcome.previewLines)
            }
        }.getOrElse {
            StatementPreviewState.Failed(name, it.message ?: "Could not open the file.", emptyList())
        }
    }
}

private enum class HomeDestination(
    val label: String,
    val icon: ImageVector,
) {
    Home("Home", Icons.Rounded.Home),
    Transactions("Transactions", Icons.AutoMirrored.Rounded.ReceiptLong),
    Insights("Insights", Icons.Rounded.AutoAwesome),
    More("More", Icons.Rounded.MoreHoriz),
}

private fun Context.displayNameFor(uri: Uri): String {
    val fallback = uri.lastPathSegment ?: "Selected statement"
    return contentResolver
        .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else fallback
        } ?: fallback
}

private fun initialsFor(name: String?, email: String?): String {
    name?.takeIf { it.isNotBlank() }?.let { n ->
        val parts = n.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }
    email?.takeIf { it.isNotBlank() }?.let { return it.first().uppercase() }
    return "?"
}

private fun greetingForNow(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 5 -> "Late night"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }
}
