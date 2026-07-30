package com.donik1998.cubeclash.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Spacing

/**
 * A leading colored badge — Figma `it` (47:179): a 30×30, [Radius.sm] rounded square filled with
 * [backgroundColor], holding a 16pt glyph. The [tint] defaults to [onBrandPrimary] (white) so a
 * white glyph reads on the brand/success fills; the Settings tile overrides it (spec §11.6).
 */
@Composable
fun IconTile(
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    tint: Color = CubeClashTheme.colors.onBrandPrimary,
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .background(backgroundColor, Radius.sm),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
    }
}

/**
 * One row of the profile menu card — Figma `row` (47:206/211/216): a leading [IconTile], a label
 * that takes the remaining width, an optional [trailingValue], and a chevron. Tapping anywhere on
 * the 54-tall row fires [onTap].
 *
 * This is deliberately **not** the settings `_SettingSwitch` — that one trails a toggle; this one
 * trails a value + chevron and navigates (spec §8, fixed decision 8).
 */
@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    iconTileColor: Color,
    label: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = CubeClashTheme.colors.onBrandPrimary,
    trailingValue: String? = null,
    showChevron: Boolean = true,
) {
    val colors = CubeClashTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onTap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        IconTile(icon = icon, backgroundColor = iconTileColor, tint = iconTint)
        Text(
            text = label,
            style = CubeClashTheme.typography.bodyStrong,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        trailingValue?.let { value ->
            Text(
                text = value,
                style = CubeClashTheme.typography.body,
                color = colors.textSecondary,
            )
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(Spacing.lg),
            )
        }
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Menu row · Light", showBackground = true)
@Preview(name = "Menu row · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileMenuRowPreview() {
    CubeClashTheme {
        Column(Modifier.padding(Spacing.md)) {
            ProfileMenuRow(
                icon = Icons.Rounded.People,
                iconTileColor = CubeClashTheme.colors.brandPrimary,
                label = "Friends",
                trailingValue = "48",
                onTap = {},
            )
        }
    }
}
