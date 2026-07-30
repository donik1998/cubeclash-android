package com.donik1998.cubeclash.feature.stats.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.HeadToHead
import com.donik1998.cubeclash.core.model.PublicProfile
import com.donik1998.cubeclash.feature.stats.player.component.HeadToHeadSection
import com.donik1998.cubeclash.feature.stats.player.component.PersonalBestsSection
import com.donik1998.cubeclash.feature.stats.player.component.PlayerProfileHero

/**
 * Layer B: the pure, testable body of a player's public profile. Given a resolved [PublicProfile]
 * it assembles the hero card, the personal-bests row and the head-to-head block, stacked with
 * [Spacing.md] gaps and vertically scrollable. It imports no Hilt and no ViewModel, so every state
 * — including the collapse cases (no country, all-null bests, never-raced) — is reachable from a
 * preview or a test with fixed data.
 */
@Composable
fun PlayerProfileContent(
    profile: PublicProfile,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        PlayerProfileHero(
            displayName = profile.displayName,
            country = profile.country,
            elo = profile.elo,
        )
        PersonalBestsSection(profile = profile)
        HeadToHeadSection(
            record = profile.headToHead,
            opponentName = profile.displayName,
        )
    }
}

// --- Sample data (shared by this file's and the component files' previews) ---------------------

internal fun samplePublicProfile(
    displayName: String = "kian_r",
    country: String? = "IR",
    elo: Int = 1180,
    bestSingleMs: Long? = 6_310,
    bestAo5Ms: Long? = 7_020,
    bestAo12Ms: Long? = 7_540,
    headToHead: HeadToHead? = HeadToHead(wins = 7, losses = 3),
) = PublicProfile(
    id = "u-1",
    displayName = displayName,
    country = country,
    elo = elo,
    bestSingleMs = bestSingleMs,
    bestAo5Ms = bestAo5Ms,
    bestAo12Ms = bestAo12Ms,
    headToHead = headToHead,
)

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Populated · Light", showBackground = true)
@Preview(name = "Populated · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PlayerProfileContentPopulatedPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            PlayerProfileContent(samplePublicProfile())
        }
    }
}

@Preview(name = "Never raced · Light", showBackground = true)
@Preview(name = "Never raced · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PlayerProfileContentNeverRacedPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            PlayerProfileContent(samplePublicProfile(headToHead = null))
        }
    }
}

@Preview(name = "Zero-zero record · Light", showBackground = true)
@Preview(name = "Zero-zero record · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PlayerProfileContentZeroZeroPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            PlayerProfileContent(samplePublicProfile(headToHead = HeadToHead(0, 0)))
        }
    }
}

@Preview(name = "New player · Light", showBackground = true)
@Preview(name = "New player · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PlayerProfileContentNewPlayerPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            PlayerProfileContent(
                samplePublicProfile(
                    displayName = "fresh_cuber",
                    country = null,
                    elo = 1000,
                    bestSingleMs = null,
                    bestAo5Ms = null,
                    bestAo12Ms = null,
                    headToHead = null,
                ),
            )
        }
    }
}
