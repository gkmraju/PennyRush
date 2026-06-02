package dev.pennyrush.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import dev.pennyrush.core.designsystem.PennyrushTheme
import dev.pennyrush.core.designsystem.ThemeMode
import dev.pennyrush.core.designsystem.ThemePreferences
import dev.pennyrush.feature.home.HomeRoute
import dev.pennyrush.feature.home.PlanningSync
import dev.pennyrush.feature.home.TransactionsSync
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAuthDeeplink(intent)
        enableEdgeToEdge()
        setContent {
            val mode = ThemePreferences.themeMode
            val systemDark = isSystemInDarkTheme()
            PennyrushTheme(
                darkTheme = when (mode) {
                    ThemeMode.System -> systemDark
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                },
            ) {
                PennyrushApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthDeeplink(intent)
    }

    private fun handleAuthDeeplink(intent: Intent) {
        if (PennyrushSupabase.isConfigured) {
            PennyrushSupabase.client.handleDeeplinks(intent)
        }
    }
}

@Composable
private fun PennyrushApp() {
    if (!PennyrushSupabase.isConfigured) {
        AuthSetupRequired()
        return
    }

    val supabase = PennyrushSupabase.client
    val repo = remember { TransactionsRepository(supabase) }
    val sessionStatus by supabase.auth.sessionStatus.collectAsState()

    when (val status = sessionStatus) {
        is SessionStatus.Authenticated -> {
            val userId = status.session.user?.id
            val sync = remember(userId) { syncFor(repo, userId) }
            val planningSync = remember(userId) { planningSyncFor(repo, userId) }
            HomeRoute(
                userEmail = status.session.user?.email,
                userName = status.session.user?.metaString("full_name", "name"),
                userAvatarUrl = status.session.user?.metaString("avatar_url", "picture"),
                sync = sync,
                planningSync = planningSync,
                onDeleteAccount = { deleteAccount(status.session.accessToken, supabase) },
                onSignOut = {
                    supabase.auth.signOut()
                },
            )
        }
        SessionStatus.Initializing -> LoadingAuth()
        is SessionStatus.NotAuthenticated,
        is SessionStatus.RefreshFailure -> GoogleSignInRoute()
    }
}

@Composable
private fun GoogleSignInRoute() {
    val scope = rememberCoroutineScope()
    val insets: PaddingValues = WindowInsets.systemBars.asPaddingValues()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SignInBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandMark()
            Spacer(Modifier.height(28.dp))
            Text(
                text = "PennyRush",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Every penny, in a rush to be tracked.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(insets)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    scope.launch {
                        PennyrushSupabase.client.auth.signInWith(Google)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = CircleShape,
                        clip = false,
                    ),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F1F1F),
                ),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google_g),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.1.sp,
                    ),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Your money data stays backed up across devices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun syncFor(repo: TransactionsRepository, userId: String?): TransactionsSync {
    if (userId.isNullOrBlank()) return TransactionsSync()
    var accountIdCache: String? = null
    suspend fun ensureAccount(): String =
        accountIdCache ?: repo.ensureAccount(userId).also { accountIdCache = it }
    return TransactionsSync(
        enabled = true,
        loadAll = { repo.listForUser(userId) },
        persistOne = { repo.insertOne(userId, ensureAccount(), it) },
        persistBatch = { repo.insertBatch(userId, ensureAccount(), it) },
        updateOne = { repo.updateOne(userId, it) },
        deleteOne = { repo.deleteOne(userId, it) },
    )
}

private fun planningSyncFor(repo: TransactionsRepository, userId: String?): PlanningSync {
    if (userId.isNullOrBlank()) return PlanningSync()
    return PlanningSync(
        enabled = true,
        loadBudgets = { repo.listBudgets(userId) },
        saveBudget = { repo.saveBudget(userId, it) },
        loadGoals = { repo.listGoals(userId) },
        saveGoal = { repo.saveGoal(userId, it) },
        deleteGoal = { repo.deleteGoal(userId, it) },
    )
}

private suspend fun deleteAccount(accessToken: String, supabase: io.github.jan.supabase.SupabaseClient) {
    val baseUrl = BuildConfig.WEB_BASE_URL.trim().trimEnd('/')
    require(baseUrl.startsWith("https://")) { "Account deletion requires the secure web endpoint." }

    val request = Request.Builder()
        .url("$baseUrl/api/account/delete")
        .header("Authorization", "Bearer $accessToken")
        .post(ByteArray(0).toRequestBody("application/json".toMediaType()))
        .build()

    withContext(Dispatchers.IO) {
        OkHttpClient().newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val body = response.body?.string()?.takeIf { it.isNotBlank() }
                error(body ?: "Account deletion failed with HTTP ${response.code}")
            }
        }
    }

    supabase.auth.signOut()
}

private fun UserInfo.metaString(vararg keys: String): String? {
    val meta = userMetadata ?: return null
    for (key in keys) {
        (meta[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }?.let { return it }
    }
    return null
}

@Composable
private fun SignInBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .size(104.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary,
            )
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_pennyrush_logo),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
        )
    }
}

@Composable
private fun AuthSetupRequired() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "PennyRush needs setup",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "This build is missing the secure connection settings. Add the Android configuration values and rebuild.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun LoadingAuth() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
