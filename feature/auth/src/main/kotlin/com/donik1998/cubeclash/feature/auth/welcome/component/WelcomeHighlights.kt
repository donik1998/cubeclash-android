package com.donik1998.cubeclash.feature.auth.welcome.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Spacing

/**
 * The three lines of what CubeClash is, each with a soft brand chip icon. Stateless: the whole
 * list is a constant, so this is a leaf a preview renders with no setup.
 */
@Composable
fun WelcomeHighlights(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        for ((icon, label) in Items) {
            HighlightRow(icon = icon, label = label)
        }
    }
}

@Composable
private fun HighlightRow(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(IconChip)
                .background(CubeClashTheme.colors.brandPrimarySoft, Radius.md),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CubeClashTheme.colors.brandPrimary,
                modifier = Modifier.size(Spacing.md),
            )
        }
        Text(
            text = label,
            style = CubeClashTheme.typography.small,
            color = CubeClashTheme.colors.textSecondary,
        )
    }
}

private val IconChip = 36.dp

private val Items: List<Pair<ImageVector, String>> = listOf(
    Icons.Outlined.Timer to "WCA inspection, +2 and DNF, done properly",
    Icons.Outlined.Bolt to "Live 1v1 races on a shared scramble",
    Icons.Outlined.BarChart to "Ao5, Ao12 and where you rank",
)
