package com.donik1998.cubeclash.feature.stats.player.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.component.StatCard
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.PublicProfile
import com.donik1998.cubeclash.core.model.ResultFormatter
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.feature.stats.player.samplePublicProfile

/**
 * The "PERSONAL BESTS" block: three equal-width [StatCard]s for best-ever single / ao5 / ao12.
 *
 * These are **best-ever** values, not the current rolling average — the labels say "best single /
 * best ao5 / best ao12" precisely so a reader can't confuse them with the rolling ao5/ao12 on the
 * Stats screen. Each value is nullable in its own right (a player with no solves has all three
 * null), so each collapses to an em dash in its **own** tile rather than the tile disappearing —
 * [ResultFormatter.formatAverage] already returns "—" for null, so the collapse is centralized.
 */
@Composable
fun PersonalBestsSection(
    profile: PublicProfile,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        SectionHeader("Personal bests")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            StatCard(
                label = "best single",
                value = ResultFormatter.formatAverage(profile.bestSingleMs, WcaEvent.THREE),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "best ao5",
                value = ResultFormatter.formatAverage(profile.bestAo5Ms, WcaEvent.THREE),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "best ao12",
                value = ResultFormatter.formatAverage(profile.bestAo12Ms, WcaEvent.THREE),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Populated · Light", showBackground = true)
@Preview(name = "Populated · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PersonalBestsPopulatedPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            PersonalBestsSection(
                samplePublicProfile(bestSingleMs = 6_310, bestAo5Ms = 7_020, bestAo12Ms = 7_540),
            )
        }
    }
}

@Preview(name = "All null · Light", showBackground = true)
@Preview(name = "All null · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PersonalBestsAllNullPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            PersonalBestsSection(
                samplePublicProfile(bestSingleMs = null, bestAo5Ms = null, bestAo12Ms = null),
            )
        }
    }
}
