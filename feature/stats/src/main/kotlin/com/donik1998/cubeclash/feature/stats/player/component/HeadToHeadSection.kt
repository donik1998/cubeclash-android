package com.donik1998.cubeclash.feature.stats.player.component

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.donik1998.cubeclash.core.designsystem.component.CubeCard
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.designsystem.theme.tabular
import com.donik1998.cubeclash.core.model.HeadToHead

/**
 * The "HEAD TO HEAD" block — the reason you tapped a name on a leaderboard in the first place.
 *
 * The one distinction this component exists to preserve: [record] `null` means **you two have
 * never raced**, which is a genuinely different state from a present `HeadToHead(0, 0)` (you have
 * raced, neither has won). Null renders a plain "you haven't raced yet" line; a present record —
 * even 0-0 — renders the wins / losses tally. Collapsing the two would fabricate a history.
 */
@Composable
fun HeadToHeadSection(
    record: HeadToHead?,
    opponentName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        SectionHeader("Head to head")
        if (record == null) {
            NeverRacedCard(opponentName = opponentName)
        } else {
            TallyCard(record = record)
        }
    }
}

/** `null` head-to-head: never raced. A muted line inside a card, not a fabricated 0-0. */
@Composable
private fun NeverRacedCard(opponentName: String, modifier: Modifier = Modifier) {
    CubeCard(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "You haven't raced $opponentName yet",
                style = CubeClashTheme.typography.body,
                color = CubeClashTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * A present record — the wins / losses tally, valid even at 0-0. Wins take the success colour and
 * losses the danger colour; the counts use tabular figures so the two columns stay aligned. The
 * viewer is always the "you" side, so the left column is the viewer's wins.
 */
@Composable
private fun TallyCard(record: HeadToHead, modifier: Modifier = Modifier) {
    CubeCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TallyColumn(
                count = record.wins,
                label = "Wins",
                color = CubeClashTheme.colors.success,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "–",
                style = CubeClashTheme.typography.h2,
                color = CubeClashTheme.colors.textMuted,
                modifier = Modifier.padding(horizontal = Spacing.sm),
            )
            TallyColumn(
                count = record.losses,
                label = "Losses",
                color = CubeClashTheme.colors.danger,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TallyColumn(
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = count.toString(),
            style = CubeClashTheme.typography.h1.tabular(),
            color = color,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label.uppercase(),
            style = CubeClashTheme.typography.overline,
            color = CubeClashTheme.colors.textMuted,
        )
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Never raced · Light", showBackground = true)
@Preview(name = "Never raced · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun HeadToHeadNeverRacedPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            HeadToHeadSection(record = null, opponentName = "kian_r")
        }
    }
}

@Preview(name = "Zero-zero · Light", showBackground = true)
@Preview(name = "Zero-zero · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun HeadToHeadZeroZeroPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            HeadToHeadSection(record = HeadToHead(wins = 0, losses = 0), opponentName = "kian_r")
        }
    }
}

@Preview(name = "Populated · Light", showBackground = true)
@Preview(name = "Populated · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun HeadToHeadPopulatedPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            HeadToHeadSection(record = HeadToHead(wins = 7, losses = 3), opponentName = "kian_r")
        }
    }
}
