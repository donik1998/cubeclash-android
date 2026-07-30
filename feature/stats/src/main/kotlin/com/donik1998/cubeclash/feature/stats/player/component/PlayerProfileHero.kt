package com.donik1998.cubeclash.feature.stats.player.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.designsystem.component.CubeCard
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.CountryNames
import com.donik1998.cubeclash.core.ui.ProfileAvatar

/**
 * The profile header card: the initials-only [ProfileAvatar] (there is no avatar field on the
 * public-profile endpoint, so it is always the letter fallback), the bold serif display name, the
 * localized country line (dropped when the country is null or unknown), and the Elo chip.
 *
 * Elo is server-owned — displayed, never derived — so it renders straight from [elo] with no
 * client arithmetic. A very long name ellipsizes so the card stays centered.
 */
@Composable
fun PlayerProfileHero(
    displayName: String,
    country: String?,
    elo: Int,
    modifier: Modifier = Modifier,
) {
    CubeCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ProfileAvatar(displayName = displayName)
            Text(
                text = displayName,
                style = CubeClashTheme.typography.h2,
                color = CubeClashTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CountryNames.displayName(country)?.let { name ->
                Text(
                    text = name,
                    style = CubeClashTheme.typography.small,
                    color = CubeClashTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                )
            }
            CubeChip(label = "Elo $elo", tone = ChipTone.Brand)
        }
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Populated · Light", showBackground = true)
@Preview(name = "Populated · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PlayerProfileHeroPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            PlayerProfileHero(displayName = "kian_r", country = "IR", elo = 1180)
        }
    }
}

@Preview(name = "No country · Light", showBackground = true)
@Preview(name = "No country · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PlayerProfileHeroNoCountryPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            PlayerProfileHero(displayName = "cuber_98", country = null, elo = 1000)
        }
    }
}

@Preview(name = "Long name · Light", showBackground = true)
@Preview(name = "Long name · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PlayerProfileHeroLongNamePreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            PlayerProfileHero(
                displayName = "the_longest_possible_speedcuber_handle_2026",
                country = "UZ",
                elo = 2480,
            )
        }
    }
}
