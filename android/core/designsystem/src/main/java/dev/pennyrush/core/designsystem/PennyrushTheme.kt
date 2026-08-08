package dev.pennyrush.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/*
 * PennyRush, 2026.
 *
 * The old scheme was emerald-on-white with seven accent hues competing across
 * every screen. In a money app that is backwards: the only things that should
 * carry colour are the amounts, because up and down is the one distinction the
 * eye must never have to work for.
 *
 * So the shell is neutral, warm rather than blue-grey, and the single brand
 * accent is copper. It is on the nose for an app called PennyRush and, more
 * usefully, it is nowhere near the green and red that mean money moved.
 */

// Brand. One hue, used sparingly: the primary action, the selected tab, focus.
private val Copper = Color(0xFFB4531B)
private val CopperBright = Color(0xFFF0A16A)
private val CopperWashLight = Color(0xFFFBEADF)
private val CopperWashDark = Color(0xFF43230F)

// Warm neutrals. A hint of yellow in the greys stops the near-white from
// reading as cold hospital blue, and stops the near-black from looking flat.
private val CanvasLight = Color(0xFFFBFAF8)
private val SurfaceLight = Color(0xFFFFFFFF)
private val ContainerLight = Color(0xFFF3F1ED)
private val ContainerHighLight = Color(0xFFEAE7E1)
private val InkLight = Color(0xFF1A1815)
private val MutedLight = Color(0xFF6B6660)
private val OutlineLight = Color(0xFFDCD8D1)

private val CanvasDark = Color(0xFF100F0E)
private val SurfaceDark = Color(0xFF181614)
private val ContainerDark = Color(0xFF201D1A)
private val ContainerHighDark = Color(0xFF2A2622)
private val InkDark = Color(0xFFF2EFEA)
private val MutedDark = Color(0xFFA9A29A)
private val OutlineDark = Color(0xFF3A342E)

private val LightColors = lightColorScheme(
    primary = Copper,
    onPrimary = Color.White,
    primaryContainer = CopperWashLight,
    onPrimaryContainer = Color(0xFF54240A),
    secondary = Color(0xFF4A5568),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4E7EC),
    onSecondaryContainer = Color(0xFF23293A),
    tertiary = Color(0xFF0F7A5A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5F0E4),
    onTertiaryContainer = Color(0xFF06301F),
    background = CanvasLight,
    onBackground = InkLight,
    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = ContainerLight,
    onSurfaceVariant = MutedLight,
    surfaceContainer = ContainerLight,
    surfaceContainerHigh = ContainerHighLight,
    surfaceContainerHighest = Color(0xFFE3DFD8),
    surfaceContainerLow = Color(0xFFF8F6F3),
    surfaceContainerLowest = Color.White,
    outline = OutlineLight,
    outlineVariant = Color(0xFFE7E3DC),
    error = Color(0xFFC0364B),
    onError = Color.White,
    errorContainer = Color(0xFFFBE0E4),
    onErrorContainer = Color(0xFF5C1220),
    scrim = Color(0xFF0C0B0A),
)

private val DarkColors = darkColorScheme(
    primary = CopperBright,
    onPrimary = Color(0xFF3A1C08),
    primaryContainer = CopperWashDark,
    onPrimaryContainer = Color(0xFFFFDCC6),
    secondary = Color(0xFFB6BFCE),
    onSecondary = Color(0xFF232936),
    secondaryContainer = Color(0xFF333B4B),
    onSecondaryContainer = Color(0xFFE0E5EC),
    tertiary = Color(0xFF63D6A8),
    onTertiary = Color(0xFF04301F),
    tertiaryContainer = Color(0xFF0C4A34),
    onTertiaryContainer = Color(0xFFCDF3E2),
    background = CanvasDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = ContainerDark,
    onSurfaceVariant = MutedDark,
    surfaceContainer = ContainerDark,
    surfaceContainerHigh = ContainerHighDark,
    surfaceContainerHighest = Color(0xFF332E29),
    surfaceContainerLow = Color(0xFF141210),
    surfaceContainerLowest = Color(0xFF0B0A09),
    outline = OutlineDark,
    outlineVariant = Color(0xFF2B2621),
    error = Color(0xFFFF8A9E),
    onError = Color(0xFF48000E),
    errorContainer = Color(0xFF6A1526),
    onErrorContainer = Color(0xFFFFD9DE),
    scrim = Color(0xFF000000),
)

/*
 * Larger than Material's defaults on purpose. The 2026 look is softer and more
 * generous, and a money app is mostly rectangles: the radius is doing most of
 * the work that a border used to do.
 */
private val PennyrushShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
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

    // Passed down explicitly rather than inferred from background luminance.
    // Guessing the mode from a colour breaks the moment dynamic colour is on.
    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalMoneyColors provides if (darkTheme) DarkMoneyColors else LightMoneyColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PennyrushTypography,
            shapes = PennyrushShapes,
            content = content,
        )
    }
}
