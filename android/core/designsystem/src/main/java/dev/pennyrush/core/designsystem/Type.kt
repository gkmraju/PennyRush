package dev.pennyrush.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/*
 * The old scale set display, headline and title all to ExtraBold, which is the
 * same as having no hierarchy: when everything shouts, the balance does not
 * stand out from the section header above it.
 *
 * Here weight descends with size. Only the balance is heavy, headings are
 * semibold, and body text is regular, so the eye lands on the number first.
 */
private val base = Typography()

private val tightHeadings = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

val PennyrushTypography = base.copy(
    // Reserved for the one number that matters on a screen.
    displayLarge = base.displayLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 52.sp,
        lineHeight = 56.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.4).sp,
        lineHeightStyle = tightHeadings,
    ),
    displayMedium = base.displayMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.0).sp,
        lineHeightStyle = tightHeadings,
    ),
    displaySmall = base.displaySmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.6).sp,
        lineHeightStyle = tightHeadings,
    ),
    // Screen titles.
    headlineMedium = base.headlineMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.4).sp,
    ),
    headlineSmall = base.headlineSmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    // Card titles.
    titleLarge = base.titleLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.1).sp,
    ),
    titleMedium = base.titleMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    titleSmall = base.titleSmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    bodyLarge = base.bodyLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = base.bodyMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = base.bodySmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = base.labelLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    labelMedium = base.labelMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
    ),
    // Section eyebrows. The only place letter-spacing is used, and the only
    // place text is set in caps.
    labelSmall = base.labelSmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
    ),
)

/**
 * For anything that is a number in a column.
 *
 * Amounts that do not line up are the classic tell of an app that was never
 * looked at properly, so every figure in a list or table uses this.
 */
val MoneyTextStyle: TextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontFeatureSettings = "tnum",
    letterSpacing = (-0.2).sp,
)
