package com.donik1998.cubeclash.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.component.CubeCard
import com.donik1998.cubeclash.core.designsystem.component.IconTile
import com.donik1998.cubeclash.core.designsystem.component.ProfileMenuRow
import com.donik1998.cubeclash.core.designsystem.component.StatCard
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.designsystem.theme.tabular
import com.donik1998.cubeclash.core.model.CountryNames
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.PlayerProfile
import com.donik1998.cubeclash.core.model.ProfileRank
import com.donik1998.cubeclash.core.model.ResultFormatter
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.ui.ProfileAvatar
import java.util.Locale
import kotlin.math.roundToInt

/**
 * The pure, testable body of the You · Profile screen — Figma `body` (47:164): the hero card, the
 * three summary tiles, and the Friends / Share / Settings menu, stacked with [Spacing.md] gaps and
 * vertically scrollable. It takes a resolved [PlayerProfile] plus callbacks and imports no Hilt and
 * no ViewModel, so every state — including the collapse cases and layout breakers — is reachable
 * from a preview or a test with fixed data.
 */
@Composable
fun ProfileContent(
    profile: PlayerProfile,
    onFriends: () -> Unit,
    onShare: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        ProfileHero(profile = profile)
        StatRow(profile = profile)
        ProfileMenuCard(
            friendCount = profile.friendCount,
            onFriends = onFriends,
            onShare = onShare,
            onSettings = onSettings,
        )
    }
}

/**
 * Figma `hero` (47:165): a [CubeCard] with a centered [ProfileAvatar], the bold serif display name,
 * and the [ProfileSubtitle], all center-aligned. A very long name ellipsizes so the card stays
 * centered rather than pushing the layout around.
 */
@Composable
private fun ProfileHero(profile: PlayerProfile, modifier: Modifier = Modifier) {
    CubeCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ProfileAvatar(displayName = profile.displayName)
            Text(
                text = profile.displayName,
                style = CubeClashTheme.typography.h2,
                color = CubeClashTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            ProfileSubtitle(
                country = profile.country,
                elo = profile.elo,
                rankPosition = profile.rank?.position,
            )
        }
    }
}

/**
 * Figma `Uzbekistan · Elo 1180 · #1,204` (47:168): the muted subtitle. Joins the present segments
 * with " · " — the localized country name (dropped when [country] is null or unknown), the always-
 * present "Elo {n}", and the grouped rank "#{n}" (dropped when [rankPosition] is null). So a new
 * cuber with no country and no rank reads simply "Elo 1000".
 */
@Composable
private fun ProfileSubtitle(
    country: String?,
    elo: Int,
    rankPosition: Int?,
    modifier: Modifier = Modifier,
) {
    val segments = listOfNotNull(
        CountryNames.displayName(country),
        "Elo $elo",
        rankPosition?.let { "#" + String.format(Locale.US, "%,d", it) },
    )
    Text(
        text = segments.joinToString(" · "),
        modifier = modifier,
        style = CubeClashTheme.typography.small.tabular(),
        color = CubeClashTheme.colors.textMuted,
        textAlign = TextAlign.Center,
    )
}

/**
 * Figma `s3` (47:169): three equal-width [StatCard]s with a [Spacing.sm] gap — best single, total
 * solves, win rate. Each value is pre-formatted here (tabular figures inside the card) with the
 * nullable ones collapsing to an em dash: no ranked solve → best "—", no settled races → win rate
 * "—". This is layout only; it holds no state.
 */
@Composable
private fun StatRow(profile: PlayerProfile, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        StatCard(
            label = "best",
            value = profile.bestSingleMs?.let { ResultFormatter.formatDuration(it) } ?: "—",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "solves",
            value = String.format(Locale.US, "%,d", profile.totalSolves),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "win rate",
            value = profile.winRate?.let { "${(it * 100).roundToInt()}%" } ?: "—",
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Figma `group` (47:205): one [CubeCard] holding the three menu rows separated by left-inset
 * hairline dividers so the dividers start past the icon tiles. Friends carries a trailing count;
 * Share and Settings are chevron-only. The Settings tile is the neutral [surfaceAlt] fill with a
 * [textMuted] glyph (spec §11.6) — no hardcoded color anywhere.
 */
@Composable
private fun ProfileMenuCard(
    friendCount: Int,
    onFriends: () -> Unit,
    onShare: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CubeClashTheme.colors
    CubeCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.md),
    ) {
        ProfileMenuRow(
            icon = Icons.Rounded.People,
            iconTileColor = colors.brandPrimary,
            label = "Friends",
            trailingValue = String.format(Locale.US, "%,d", friendCount),
            onTap = onFriends,
        )
        MenuDivider()
        ProfileMenuRow(
            icon = Icons.Rounded.IosShare,
            iconTileColor = colors.success,
            label = "Share profile",
            onTap = onShare,
        )
        MenuDivider()
        ProfileMenuRow(
            icon = Icons.Rounded.Tune,
            iconTileColor = colors.surfaceAlt,
            iconTint = colors.textMuted,
            label = "Settings",
            onTap = onSettings,
        )
    }
}

/** The 56-inset hairline between menu rows — starts past the 30 tile + its gap (Figma `dv`). */
@Composable
private fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 30.dp + Spacing.sm),
        thickness = 1.dp,
        color = CubeClashTheme.colors.borderSubtle,
    )
}

// --- Previews ---------------------------------------------------------------------------------

internal fun sampleProfile(
    displayName: String = "cuber_98",
    country: String? = "UZ",
    elo: Int = 1180,
    rankPosition: Int? = 1_204,
    bestSingleMs: Long? = 8_420,
    totalSolves: Int = 3_204,
    winRate: Double? = 0.68,
    friendCount: Int = 48,
) = PlayerProfile(
    id = "u-1",
    displayName = displayName,
    country = country,
    elo = elo,
    rank = rankPosition?.let {
        ProfileRank(
            position = it,
            event = WcaEvent.THREE,
            metric = "single",
            scope = LeaderboardScope.GLOBAL,
        )
    },
    bestSingleMs = bestSingleMs,
    bestSingleEvent = WcaEvent.THREE,
    totalSolves = totalSolves,
    winRate = winRate,
    wins = 217,
    losses = 102,
    friendCount = friendCount,
)

@Preview(name = "Populated · Light", showBackground = true)
@Preview(name = "Populated · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileContentPopulatedPreview() {
    CubeClashTheme {
        androidx.compose.foundation.layout.Box(Modifier.padding(Spacing.md)) {
            ProfileContent(sampleProfile(), {}, {}, {})
        }
    }
}

@Preview(name = "No country · Light", showBackground = true)
@Preview(name = "No country · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileContentNoCountryPreview() {
    CubeClashTheme {
        androidx.compose.foundation.layout.Box(Modifier.padding(Spacing.md)) {
            ProfileContent(sampleProfile(country = null), {}, {}, {})
        }
    }
}

@Preview(name = "Unranked · Light", showBackground = true)
@Preview(name = "Unranked · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileContentUnrankedPreview() {
    CubeClashTheme {
        androidx.compose.foundation.layout.Box(Modifier.padding(Spacing.md)) {
            ProfileContent(
                sampleProfile(
                    elo = 1000,
                    rankPosition = null,
                    bestSingleMs = null,
                    winRate = null,
                ),
                {}, {}, {},
            )
        }
    }
}

@Preview(name = "No solves / races · Light", showBackground = true)
@Preview(name = "No solves / races · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileContentEmptyStatsPreview() {
    CubeClashTheme {
        androidx.compose.foundation.layout.Box(Modifier.padding(Spacing.md)) {
            ProfileContent(
                sampleProfile(
                    bestSingleMs = null,
                    totalSolves = 0,
                    winRate = null,
                    friendCount = 0,
                ),
                {}, {}, {},
            )
        }
    }
}

@Preview(name = "Layout breakers · Light", showBackground = true)
@Preview(name = "Layout breakers · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileContentExtremesPreview() {
    CubeClashTheme {
        androidx.compose.foundation.layout.Box(Modifier.padding(Spacing.md)) {
            ProfileContent(
                sampleProfile(
                    displayName = "the_longest_possible_speedcuber_handle_2026",
                    rankPosition = 12_048,
                    bestSingleMs = 12_340,
                    totalSolves = 128_540,
                    friendCount = 4_812,
                ),
                {}, {}, {},
            )
        }
    }
}
