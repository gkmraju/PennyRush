package dev.pennyrush.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The tokens every screen is built from.
 *
 * Before this existed the UI hardcoded nine different corner radii and eight
 * padding values, so nothing lined up and no change was safe. A scale is worth
 * more than any individual value in it: the point is that there are only a few.
 */
object Space {
    /** Between a label and the thing it labels. */
    val xs: Dp = 4.dp
    /** Inside a chip, between an icon and its text. */
    val sm: Dp = 8.dp
    /** Between rows in a list. */
    val md: Dp = 12.dp
    /** Card padding, and the gutter between cards. */
    val lg: Dp = 16.dp
    /** Screen gutter. */
    val xl: Dp = 20.dp
    /** Between sections. */
    val xxl: Dp = 28.dp
}

object Radius {
    val chip: Dp = 999.dp
    val field: Dp = 18.dp
    val tile: Dp = 20.dp
    val card: Dp = 28.dp
    val sheet: Dp = 32.dp
}

object Sizing {
    val button: Dp = 54.dp
    val icon: Dp = 20.dp
    val avatar: Dp = 40.dp
    val touchTarget: Dp = 48.dp
}

/** True when the app is in dark mode, taken from the theme rather than guessed. */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

/**
 * The only colours in the app that carry meaning rather than decoration.
 *
 * Everything else is neutral. Money moved up, money moved down, and a warning:
 * three signals, so all three stay instantly legible. The old palette had seven
 * hues doing decorative work, which meant the two that mattered had to compete.
 */
@Immutable
data class MoneyColors(
    val income: Color,
    val incomeWash: Color,
    val expense: Color,
    val expenseWash: Color,
    val neutral: Color,
    val neutralWash: Color,
    val warning: Color,
    val warningWash: Color,
    /** A small ramp for charts and category dots, tonal rather than rainbow. */
    val ramp: List<Color>,
)

internal val LightMoneyColors = MoneyColors(
    income = Color(0xFF0F7A5A),
    incomeWash = Color(0xFFDDF2E9),
    expense = Color(0xFFC0364B),
    expenseWash = Color(0xFFFBE3E7),
    neutral = Color(0xFF6B6660),
    neutralWash = Color(0xFFEFECE7),
    warning = Color(0xFF9A6300),
    warningWash = Color(0xFFFAEFD8),
    ramp = listOf(
        Color(0xFFB4531B),
        Color(0xFF0F7A5A),
        Color(0xFF3F5C93),
        Color(0xFF9A6300),
        Color(0xFF7A4B86),
        Color(0xFF167C86),
    ),
)

internal val DarkMoneyColors = MoneyColors(
    income = Color(0xFF63D6A8),
    incomeWash = Color(0xFF10352A),
    expense = Color(0xFFFF8A9E),
    expenseWash = Color(0xFF3D1620),
    neutral = Color(0xFFA9A29A),
    neutralWash = Color(0xFF262220),
    warning = Color(0xFFF2C06B),
    warningWash = Color(0xFF3A2B10),
    ramp = listOf(
        Color(0xFFF0A16A),
        Color(0xFF63D6A8),
        Color(0xFF93AEE0),
        Color(0xFFF2C06B),
        Color(0xFFC9A3D4),
        Color(0xFF66C7D1),
    ),
)

val LocalMoneyColors = compositionLocalOf { LightMoneyColors }
