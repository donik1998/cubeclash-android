package com.donik1998.cubeclash.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The semantic colour tokens, one-for-one with the `CubeClash/Colors` variable collection in
 * Figma (🎨 Design System, node 7:2). Screens bind to *these*, never to a hex value and never
 * to a raw Material role — which is what makes the Light/Dark flip a mode switch rather than
 * a second design.
 */
@Immutable
data class CubeClashColors(
    val canvas: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val brandPrimary: Color,
    val onBrandPrimary: Color,
    val brandPrimarySoft: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val accentEnergy: Color,
    val segTrack: Color,
    val segThumb: Color,
    val isDark: Boolean,
)

val LightCubeClashColors = CubeClashColors(
    canvas = Color(0xFFF5F6F8),
    surface = Color(0xFFFFFFFF),
    surfaceAlt = Color(0xFFEEF0F4),
    borderSubtle = Color(0xFFE4E7EC),
    borderStrong = Color(0xFFCBD1DC),
    textPrimary = Color(0xFF14171D),
    textSecondary = Color(0xFF5B6472),
    textMuted = Color(0xFF9AA3B2),
    brandPrimary = Color(0xFF2E6BFF),
    onBrandPrimary = Color(0xFFFFFFFF),
    brandPrimarySoft = Color(0xFFE7EEFF),
    success = Color(0xFF12B76A),
    danger = Color(0xFFE23744),
    warning = Color(0xFFF5B800),
    accentEnergy = Color(0xFFFF6A2C),
    segTrack = Color(0xFFEAEDF2),
    segThumb = Color(0xFFFFFFFF),
    isDark = false,
)

val DarkCubeClashColors = CubeClashColors(
    canvas = Color(0xFF0C0E12),
    surface = Color(0xFF16191F),
    surfaceAlt = Color(0xFF1E222A),
    borderSubtle = Color(0xFF262B33),
    borderStrong = Color(0xFF363C46),
    textPrimary = Color(0xFFF5F6F8),
    textSecondary = Color(0xFFA7B0BF),
    textMuted = Color(0xFF6B7480),
    brandPrimary = Color(0xFF4C82FF),
    onBrandPrimary = Color(0xFFFFFFFF),
    brandPrimarySoft = Color(0xFF1B2740),
    success = Color(0xFF2ECC87),
    danger = Color(0xFFFF5A6A),
    warning = Color(0xFFFFD84D),
    accentEnergy = Color(0xFFFF7E47),
    segTrack = Color(0xFF0F1217),
    segThumb = Color(0xFF2B303A),
    isDark = true,
)

/** The authentic WCA face colours. Identical in both themes — they are the puzzle, not the UI. */
object CubeColors {
    val White = Color(0xFFFFFFFF)
    val Yellow = Color(0xFFFFD500)
    val Green = Color(0xFF009E60)
    val Blue = Color(0xFF0051BA)
    val Red = Color(0xFFC41E3A)
    val Orange = Color(0xFFFF5800)

    val all = listOf(White, Yellow, Green, Blue, Red, Orange)
}

val LocalCubeClashColors = staticCompositionLocalOf { LightCubeClashColors }
