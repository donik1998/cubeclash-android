package com.donik1998.cubeclash.feature.race.tournament.component

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.donik1998.cubeclash.core.designsystem.component.CubeCard
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.designsystem.theme.tabular
import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentStatus
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.ui.EventIcon
import com.donik1998.cubeclash.feature.race.tournament.chipTone
import com.donik1998.cubeclash.feature.race.tournament.formatEntrants
import com.donik1998.cubeclash.feature.race.tournament.formatStartTime
import com.donik1998.cubeclash.feature.race.tournament.label
import java.time.Instant

/**
 * One tappable tournament in the lobby list: the name, its event chip and a status chip on the top
 * line, then the start time (device zone, "Time TBD" when unknown) and the entrant count. The count
 * uses tabular figures so a two- vs three-digit entrant number leaves the layout put.
 *
 * [Tournament.isFull] is read straight off the model — never recomputed here.
 */
@Composable
fun TournamentRow(
    tournament: Tournament,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CubeClashTheme.colors
    CubeCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = tournament.name,
                style = CubeClashTheme.typography.title,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            CubeChip(
                label = tournament.status.label(),
                tone = tournament.status.chipTone(),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            CubeChip(
                label = tournament.event.displayName,
                leading = { EventIcon(event = tournament.event, size = Spacing.md) },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatStartTime(tournament.startsAt),
                    style = CubeClashTheme.typography.small,
                    color = colors.textSecondary,
                )
                Text(
                    text = formatEntrants(tournament.entrants, tournament.capacity),
                    style = CubeClashTheme.typography.caption.tabular(),
                    color = if (tournament.isFull) colors.textMuted else colors.textSecondary,
                )
            }
        }
    }
}

// --- Previews ---------------------------------------------------------------------------------

internal fun sampleTournament(
    id: String = "sub15-sprint",
    name: String = "Sub-15 Sprint",
    event: WcaEvent = WcaEvent.THREE,
    status: TournamentStatus = TournamentStatus.UPCOMING,
    entrants: Int = 22,
    capacity: Int = 32,
    startsAt: Instant? = Instant.parse("2026-08-01T18:00:00Z"),
    description: String = "For sub-15 averages only. Fast games, best of three.",
    registered: Boolean = false,
) = Tournament(
    id = id,
    name = name,
    event = event,
    status = status,
    entrants = entrants,
    capacity = capacity,
    startsAt = startsAt,
    description = description,
    registered = registered,
)

@Preview(name = "Row · Light", showBackground = true)
@Preview(name = "Row · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun TournamentRowPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                TournamentRow(
                    tournament = sampleTournament(
                        id = "weekly",
                        name = "Global Weekly · 3×3",
                        status = TournamentStatus.LIVE,
                        entrants = 64,
                        capacity = 64,
                        startsAt = null,
                    ),
                    onClick = {},
                )
                TournamentRow(tournament = sampleTournament(), onClick = {})
            }
        }
    }
}
