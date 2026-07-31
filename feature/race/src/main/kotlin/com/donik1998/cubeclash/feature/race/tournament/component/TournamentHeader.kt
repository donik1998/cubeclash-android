package com.donik1998.cubeclash.feature.race.tournament.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.designsystem.theme.tabular
import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentStatus
import com.donik1998.cubeclash.core.ui.EventIcon
import com.donik1998.cubeclash.feature.race.tournament.chipTone
import com.donik1998.cubeclash.feature.race.tournament.formatEntrants
import com.donik1998.cubeclash.feature.race.tournament.formatStartTime
import com.donik1998.cubeclash.feature.race.tournament.label

/**
 * The detail header: the name and a status chip, the event chip, the start time (device zone) and
 * entrant count, then the description. The count uses tabular figures so it never jitters as a
 * register bumps it. [Tournament.isFull] is read off the model, never recomputed.
 */
@Composable
fun TournamentHeader(
    tournament: Tournament,
    modifier: Modifier = Modifier,
) {
    val colors = CubeClashTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = tournament.name,
                style = CubeClashTheme.typography.h2,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            CubeChip(
                label = tournament.status.label(),
                tone = tournament.status.chipTone(),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            CubeChip(
                label = tournament.event.displayName,
                leading = { EventIcon(event = tournament.event, size = Spacing.md) },
            )
            Text(
                text = formatEntrants(tournament.entrants, tournament.capacity),
                style = CubeClashTheme.typography.small.tabular(),
                color = colors.textSecondary,
            )
        }

        Text(
            text = formatStartTime(tournament.startsAt),
            style = CubeClashTheme.typography.small,
            color = colors.textMuted,
        )

        if (tournament.description.isNotBlank()) {
            Text(
                text = tournament.description,
                style = CubeClashTheme.typography.body,
                color = colors.textSecondary,
            )
        }
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Header · Light", showBackground = true)
@Preview(name = "Header · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun TournamentHeaderPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            TournamentHeader(
                tournament = sampleTournament(
                    name = "Global Weekly · 3×3",
                    status = TournamentStatus.LIVE,
                    entrants = 64,
                    capacity = 64,
                ),
            )
        }
    }
}
