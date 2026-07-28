package com.donik1998.cubeclash.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

/**
 * One theme, two modes.
 *
 * Material 3 is present because the component library is worth having, but the CubeClash
 * tokens are the source of truth: the Material scheme below is *derived* from them so that a
 * stray `MaterialTheme.colorScheme.primary` still lands on brand colour instead of purple.
 */
@Composable
fun CubeClashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkCubeClashColors else LightCubeClashColors

    val materialScheme = remember(colors) {
        if (darkTheme) darkScheme(colors) else lightScheme(colors)
    }

    CompositionLocalProvider(
        LocalCubeClashColors provides colors,
        LocalCubeClashTypography provides cubeClashTypography,
        LocalSpacing provides Spacing,
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = MaterialTheme.typography,
            content = content,
        )
    }
}

private fun lightScheme(colors: CubeClashColors): ColorScheme = lightColorScheme(
    primary = colors.brandPrimary,
    onPrimary = colors.onBrandPrimary,
    primaryContainer = colors.brandPrimarySoft,
    onPrimaryContainer = colors.brandPrimary,
    background = colors.canvas,
    onBackground = colors.textPrimary,
    surface = colors.surface,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.surfaceAlt,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.borderStrong,
    outlineVariant = colors.borderSubtle,
    error = colors.danger,
)

private fun darkScheme(colors: CubeClashColors): ColorScheme = darkColorScheme(
    primary = colors.brandPrimary,
    onPrimary = colors.onBrandPrimary,
    primaryContainer = colors.brandPrimarySoft,
    onPrimaryContainer = colors.brandPrimary,
    background = colors.canvas,
    onBackground = colors.textPrimary,
    surface = colors.surface,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.surfaceAlt,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.borderStrong,
    outlineVariant = colors.borderSubtle,
    error = colors.danger,
)

/** `CubeClashTheme.colors.brandPrimary` — the accessor every component uses. */
object CubeClashTheme {
    val colors: CubeClashColors
        @Composable @ReadOnlyComposable get() = LocalCubeClashColors.current

    val typography: CubeClashTypography
        @Composable @ReadOnlyComposable get() = LocalCubeClashTypography.current

    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
}
