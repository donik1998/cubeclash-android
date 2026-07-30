package com.donik1998.cubeclash.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Sizes
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.designsystem.theme.tabular
import com.donik1998.cubeclash.core.model.CountryNames
import com.donik1998.cubeclash.core.model.LeaderboardEntry
import com.donik1998.cubeclash.core.model.ResultFormatter
import com.donik1998.cubeclash.core.model.WcaEvent
import java.util.Locale

/**
 * One ranked leaderboard card — Figma `row` (44:187), and its viewer variant (44:229).
 *
 * A card, not a bare list line: `surface` fill + `borderSubtle` hairline for a normal row;
 * `brandPrimarySoft` fill + `brandPrimary` border for [isViewer]. Layout is a fixed rank gutter,
 * a placeholder avatar circle, the name/country stack, and a right-aligned time. The rank and the
 * time both use tabular figures so a 1-digit and a 4-digit rank leave the name column in the same
 * place and the times align down the right edge.
 *
 * There is no avatar in the domain model; the circle renders [initialsOf] the display name.
 *
 * [onClick], when supplied, makes the whole card tappable — tapping a name opens that player's
 * public profile. It stays optional so the pinned viewer row (which is "you", already on your own
 * profile tab) can be left inert.
 */
@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    event: WcaEvent,
    modifier: Modifier = Modifier,
    isViewer: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = CubeClashTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (isViewer) colors.brandPrimarySoft else colors.surface,
                shape = Radius.button,
            )
            .border(
                width = 1.dp,
                color = if (isViewer) colors.brandPrimary else colors.borderSubtle,
                shape = Radius.button,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = formatRank(entry.rank),
            style = CubeClashTheme.typography.bodyStrong.tabular(),
            color = rankColor(entry.rank),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(Sizes.rankGutter),
        )
        Box(
            modifier = Modifier
                .size(Sizes.avatarSm)
                .background(colors.surfaceAlt, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initialsOf(entry.displayName),
                style = CubeClashTheme.typography.caption,
                color = colors.textSecondary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.displayName,
                style = CubeClashTheme.typography.bodyStrong,
                color = colors.textPrimary,
                maxLines = 1,
            )
            CountryNames.displayName(entry.country)?.let { country ->
                Text(
                    text = country,
                    style = CubeClashTheme.typography.caption,
                    color = colors.textSecondary,
                    maxLines = 1,
                )
            }
        }
        Text(
            text = ResultFormatter.formatAverage(entry.valueMs, event),
            style = CubeClashTheme.typography.title.tabular(),
            color = if (isViewer) colors.brandPrimary else colors.textPrimary,
        )
    }
}

/** Top gets the warning colour, third the energy accent; second is deliberately unaccented. */
@Composable
private fun rankColor(rank: Int): Color = when (rank) {
    1 -> CubeClashTheme.colors.warning
    3 -> CubeClashTheme.colors.accentEnergy
    else -> CubeClashTheme.colors.textSecondary
}

/** Grouped so a deep rank reads as `1,204`, not `1204`. */
internal fun formatRank(rank: Int): String = String.format(Locale.US, "%,d", rank)

/**
 * Up to two initials from a display name, upper-cased — a neutral placeholder in the avatar
 * circle since there is no avatar field on the wire. `"kian_r"` → `K`, `"m.dubois"` → `MD`,
 * blank → `?`.
 */
internal fun initialsOf(displayName: String): String {
    val words = displayName
        .split(' ', '_', '.', '-')
        .filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words.first().take(1).uppercase(Locale.ROOT)
        else -> (words.first().take(1) + words[1].take(1)).uppercase(Locale.ROOT)
    }
}
