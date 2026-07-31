package com.donik1998.cubeclash.feature.race.tournament.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.TournamentMatch
import com.donik1998.cubeclash.core.model.TournamentRound

/**
 * The whole bracket: a "Bracket" section header, then each round (earliest first, in the order the
 * model gives) labelled with its name and stacked with its matches. An empty bracket — a tournament
 * whose draw has not been made — collapses to a quiet note rather than a bare header.
 */
@Composable
fun BracketSection(
    rounds: List<TournamentRound>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        SectionHeader("Bracket")

        if (rounds.isEmpty()) {
            Text(
                text = "The bracket hasn't been drawn yet.",
                style = CubeClashTheme.typography.small,
                color = CubeClashTheme.colors.textMuted,
            )
            return@Column
        }

        rounds.forEach { round ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = round.name,
                    style = CubeClashTheme.typography.label,
                    color = CubeClashTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
                round.matches.forEach { match -> MatchCard(match = match) }
            }
        }
    }
}

// --- Previews ---------------------------------------------------------------------------------

internal fun sampleRounds() = listOf(
    TournamentRound(
        name = "Semifinals",
        matches = listOf(
            TournamentMatch("kian_r", "mira_v", timeAMs = 7_180, timeBMs = 8_640, winner = "A"),
            TournamentMatch("sora_h", "owen_p", timeAMs = 9_020, timeBMs = 7_450, winner = "B"),
        ),
    ),
    TournamentRound(
        name = "Final",
        matches = listOf(TournamentMatch("kian_r", "owen_p")),
    ),
)

@Preview(name = "Bracket · Light", showBackground = true)
@Preview(name = "Bracket · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun BracketSectionPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            BracketSection(rounds = sampleRounds())
        }
    }
}

@Preview(name = "Empty bracket · Light", showBackground = true)
@Preview(name = "Empty bracket · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun BracketSectionEmptyPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            BracketSection(rounds = emptyList())
        }
    }
}
