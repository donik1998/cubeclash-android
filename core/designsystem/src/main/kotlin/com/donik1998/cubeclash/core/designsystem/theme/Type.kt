package com.donik1998.cubeclash.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.donik1998.cubeclash.core.designsystem.R

/**
 * Noto Serif throughout — an editorial serif is rare in timer apps and is most of what makes
 * CubeClash look like itself rather than like every other stopwatch.
 */
val NotoSerif = FontFamily(
    Font(R.font.noto_serif_regular, FontWeight.Normal),
    Font(R.font.noto_serif_medium, FontWeight.Medium),
    Font(R.font.noto_serif_semibold, FontWeight.SemiBold),
    Font(R.font.noto_serif_bold, FontWeight.Bold),
    Font(R.font.noto_serif_extrabold, FontWeight.ExtraBold),
)

/**
 * Noto Serif's digits are proportional, so anything that counts up asks for `tnum`. Without it
 * a running timer visibly jitters as the glyph widths change under it.
 */
const val TABULAR_FIGURES = "tnum"

@Immutable
data class CubeClashTypography(
    val display: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val small: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val overline: TextStyle,
    // The scramble ramp: a 5×5 scramble is three times a 3×3, and a 7×7 runs to ten lines.
    val scramble: TextStyle,
    val scrambleCompact: TextStyle,
    val scrambleDense: TextStyle,
)

val cubeClashTypography = CubeClashTypography(
    display = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 60.sp,
        lineHeight = 64.sp,
        fontFeatureSettings = TABULAR_FIGURES,
        textAlign = TextAlign.Center,
    ),
    h1 = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    h2 = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    title = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    body = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyStrong = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    small = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    label = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    caption = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    overline = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em,
    ),
    scramble = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.Medium, fontSize = 19.sp, lineHeight = 28.sp),
    scrambleCompact = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    scrambleDense = TextStyle(fontFamily = NotoSerif, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 19.sp),
)

val LocalCubeClashTypography = staticCompositionLocalOf { cubeClashTypography }
