package com.donik1998.cubeclash.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing

/**
 * The circular profile avatar — Figma `Ellipse` (47:166). There is no avatar field on the wire
 * (spec §11.1), so this is always an initials-on-neutral-fill placeholder: a [surfaceAlt] fill
 * with a [brandPrimary] ring, and up to two [initialsOf] the display name centered inside.
 *
 * Promoted from the per-screen avatars the clients each inlined into one shared component, sized
 * by [size] (74 on the hero, 34 on a leaderboard row). The initials reuse the same derivation the
 * leaderboard row uses so a cuber reads the same everywhere.
 */
@Composable
fun ProfileAvatar(
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 74.dp,
) {
    val colors = CubeClashTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .background(colors.surfaceAlt, CircleShape)
            .border(2.dp, colors.brandPrimary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(displayName),
            style = CubeClashTheme.typography.h2,
            color = colors.textSecondary,
        )
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Avatar · Light", showBackground = true)
@Preview(name = "Avatar · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileAvatarPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            ProfileAvatar(displayName = "cuber_98")
        }
    }
}
