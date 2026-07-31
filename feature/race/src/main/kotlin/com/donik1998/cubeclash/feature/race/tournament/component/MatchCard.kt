package com.donik1998.cubeclash.feature.race.tournament.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.component.CubeCard
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.designsystem.theme.tabular
import com.donik1998.cubeclash.core.model.ResultFormatter
import com.donik1998.cubeclash.core.model.TournamentMatch

private val TrophyGutter = 14.dp

/**
 * One head-to-head in the bracket. A played match ([TournamentMatch.isPlayed]) shows both formatted
 * times and marks the winning seat with a trophy and the brand colour; a pending match shows an em
 * dash for each time and a quiet "Not played yet" line, so pending never masquerades as a 0-0 draw.
 */
@Composable
fun MatchCard(
    match: TournamentMatch,
    modifier: Modifier = Modifier,
) {
    CubeCard(modifier = modifier.fillMaxWidth()) {
        Seat(
            name = match.playerA,
            timeMs = match.timeAMs,
            isWinner = match.winner == "A",
            played = match.isPlayed,
        )
        Spacer(Modifier.size(Spacing.xxs))
        Seat(
            name = match.playerB,
            timeMs = match.timeBMs,
            isWinner = match.winner == "B",
            played = match.isPlayed,
        )
        if (!match.isPlayed) {
            Text(
                text = "Not played yet",
                style = CubeClashTheme.typography.caption,
                color = CubeClashTheme.colors.textMuted,
                modifier = Modifier.padding(top = Spacing.xxs),
            )
        }
    }
}

@Composable
private fun Seat(
    name: String,
    timeMs: Long?,
    isWinner: Boolean,
    played: Boolean,
) {
    val colors = CubeClashTheme.colors
    val nameColor: Color = when {
        isWinner -> colors.brandPrimary
        played -> colors.textMuted
        else -> colors.textPrimary
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        if (isWinner) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = "Winner",
                tint = colors.brandPrimary,
                modifier = Modifier.size(TrophyGutter),
            )
        } else {
            Spacer(Modifier.width(TrophyGutter))
        }
        Text(
            text = name,
            style = if (isWinner) CubeClashTheme.typography.bodyStrong else CubeClashTheme.typography.body,
            color = nameColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = timeMs?.let(ResultFormatter::formatDuration) ?: "—",
            style = CubeClashTheme.typography.body.tabular(),
            color = nameColor,
        )
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Match · Light", showBackground = true)
@Preview(name = "Match · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun MatchCardPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                MatchCard(
                    TournamentMatch(
                        playerA = "kian_r",
                        playerB = "mira_v",
                        timeAMs = 7_180,
                        timeBMs = 8_640,
                        winner = "A",
                    ),
                )
                MatchCard(TournamentMatch(playerA = "kian_r", playerB = "owen_p"))
            }
        }
    }
}
