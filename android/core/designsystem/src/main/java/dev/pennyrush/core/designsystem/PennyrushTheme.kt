package dev.pennyrush.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Emerald = Color(0xFF10B981)
private val EmeraldDeep = Color(0xFF059669)
private val Ink = Color(0xFF0F172A)
private val InkSoft = Color(0xFF1E293B)
private val Cloud = Color(0xFFF8FAFC)
private val Mist = Color(0xFFF1F5F9)

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    background = Cloud,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0),
)

private val DarkColors = darkColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = EmeraldDeep,
    onPrimaryContainer = Color(0xFFD1FAE5),
    background = Ink,
    onBackground = Cloud,
    surface = InkSoft,
    onSurface = Cloud,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
)

@Composable
fun PennyrushTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PennyrushTypography,
        content = content,
    )
}
