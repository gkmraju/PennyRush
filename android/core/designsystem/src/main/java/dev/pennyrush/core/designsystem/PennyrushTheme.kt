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

private val Gold = Color(0xFFF5B82E)
private val NearBlack = Color(0xFF0A0A0A)
private val SoftWhite = Color(0xFFF7F7F7)

private val LightColors = lightColorScheme(
    primary = Gold,
    onPrimary = NearBlack,
    background = Color.White,
    onBackground = NearBlack,
    surface = Color.White,
    onSurface = NearBlack,
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = Color(0xFF6B7280),
)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = NearBlack,
    background = NearBlack,
    onBackground = SoftWhite,
    surface = Color(0xFF111111),
    onSurface = SoftWhite,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFA3A3A3),
)

@Composable
fun PennyrushTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
