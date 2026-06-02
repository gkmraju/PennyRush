package dev.pennyrush.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val Leaf = Color(0xFF047857)
private val LeafDeep = Color(0xFF064E3B)
private val Ocean = Color(0xFF2563EB)
private val Coral = Color(0xFFE0523F)
private val Ink = Color(0xFF17211D)
private val InkSoft = Color(0xFF121A17)
private val Canvas = Color(0xFFF6FAF7)
private val Panel = Color(0xFFFFFFFF)
private val Field = Color(0xFFEAF2ED)
private val MutedText = Color(0xFF52645B)
private val MutedTextDark = Color(0xFFC0CDC6)

private val LightColors = lightColorScheme(
    primary = Leaf,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDFBEF),
    onPrimaryContainer = LeafDeep,
    secondary = Ocean,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE8FF),
    onSecondaryContainer = Color(0xFF163A8A),
    tertiary = Coral,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE1D8),
    onTertiaryContainer = Color(0xFF7A2519),
    background = Canvas,
    onBackground = Ink,
    surface = Panel,
    onSurface = Ink,
    surfaceVariant = Field,
    onSurfaceVariant = MutedText,
    outline = Color(0xFFCAD8D0),
    error = Color(0xFFBE123C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF74E3B5),
    onPrimary = Color(0xFF063323),
    primaryContainer = Color(0xFF0C4936),
    onPrimaryContainer = Color(0xFFDDFBEF),
    secondary = Color(0xFF9EC5FF),
    onSecondary = Color(0xFF0E285F),
    secondaryContainer = Color(0xFF173A80),
    onSecondaryContainer = Color(0xFFDDE8FF),
    tertiary = Color(0xFFFFB29F),
    onTertiary = Color(0xFF54160D),
    tertiaryContainer = Color(0xFF7A2519),
    onTertiaryContainer = Color(0xFFFFE1D8),
    background = Color(0xFF0B1110),
    onBackground = Color(0xFFEFF7F1),
    surface = InkSoft,
    onSurface = Color(0xFFEFF7F1),
    surfaceVariant = Color(0xFF1B2621),
    onSurfaceVariant = MutedTextDark,
    outline = Color(0xFF40524A),
    error = Color(0xFFFF8A9A),
)

private val PennyrushShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
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
        shapes = PennyrushShapes,
        content = content,
    )
}
