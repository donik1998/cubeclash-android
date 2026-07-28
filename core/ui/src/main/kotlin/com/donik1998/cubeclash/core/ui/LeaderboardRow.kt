package com.donik1998.cubeclash.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.LeaderboardEntry
import com.donik1998.cubeclash.core.model.ResultFormatter
import com.donik1998.cubeclash.core.model.WcaEvent

@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    event: WcaEvent,
    modifier: Modifier = Modifier,
) {
    val colors = CubeClashTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (entry.isCurrentUser) colors.brandPrimarySoft else Color.Transparent,
                Radius.md,
            )
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = entry.rank.toString(),
            style = CubeClashTheme.typography.bodyStrong,
            color = medalColor(entry.rank) ?: colors.textMuted,
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(colors.surfaceAlt, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = entry.user.displayName.take(1).uppercase(),
                style = CubeClashTheme.typography.caption,
                color = colors.textSecondary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.user.displayName, style = CubeClashTheme.typography.bodyStrong, color = colors.textPrimary)
            entry.user.country?.let {
                Text(it, style = CubeClashTheme.typography.caption, color = colors.textMuted)
            }
        }
        Text(
            text = ResultFormatter.formatAverage(entry.value, event),
            style = CubeClashTheme.typography.bodyStrong,
            color = colors.textPrimary,
        )
    }
}

/** Top three get the medal colours; everyone else gets muted text. */
@Composable
private fun medalColor(rank: Int): Color? = when (rank) {
    1 -> CubeClashTheme.colors.warning
    2 -> CubeClashTheme.colors.textSecondary
    3 -> CubeClashTheme.colors.accentEnergy
    else -> null
}
