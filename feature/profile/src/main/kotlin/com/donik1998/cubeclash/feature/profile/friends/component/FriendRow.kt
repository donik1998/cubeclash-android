package com.donik1998.cubeclash.feature.profile.friends.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.component.CubePrimaryButton
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.designsystem.theme.tabular
import com.donik1998.cubeclash.core.model.CountryNames
import com.donik1998.cubeclash.core.model.Friend
import com.donik1998.cubeclash.core.model.FriendStatus
import com.donik1998.cubeclash.core.model.ResultFormatter
import com.donik1998.cubeclash.core.ui.ProfileAvatar

/**
 * One friend card — Figma `FriendRow`. Tappable to open the tapped player's public profile via
 * [onOpen]. Layout mirrors [com.donik1998.cubeclash.core.ui.LeaderboardRow]: a surface card with a
 * hairline border, the shared initials [ProfileAvatar], a name/country·PB stack, and a trailing
 * action:
 *  - an **Accept** button on an incoming request only ([onAccept] non-null);
 *  - a "Pending" chip on an outgoing request the viewer sent (status PENDING, not incoming);
 *  - nothing on an accepted friend.
 *
 * An Accept button never appears on an outgoing invite — you can't accept your own invitation — so
 * [onAccept] is supplied by the caller for incoming rows and left null everywhere else. The PB uses
 * tabular figures so times align down the column, and renders an em dash when [Friend.bestSingleMs]
 * is null.
 */
@Composable
fun FriendRow(
    friend: Friend,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    onAccept: (() -> Unit)? = null,
    acceptEnabled: Boolean = true,
) {
    val colors = CubeClashTheme.colors
    val country = CountryNames.displayName(friend.countryCode)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, Radius.button)
            .border(1.dp, colors.borderSubtle, Radius.button)
            .clickable(onClick = onOpen)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        ProfileAvatar(displayName = friend.displayName, size = 40.dp)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = friend.displayName,
                style = CubeClashTheme.typography.bodyStrong,
                color = colors.textPrimary,
                maxLines = 1,
            )
            val subtitle = buildList {
                country?.let { add(it) }
                add(friend.bestSingleMs?.let { "PB ${ResultFormatter.formatDuration(it)}" } ?: "—")
            }.joinToString("  ·  ")
            Text(
                text = subtitle,
                style = CubeClashTheme.typography.caption.tabular(),
                color = colors.textMuted,
                maxLines = 1,
            )
        }

        when {
            onAccept != null -> CubePrimaryButton(
                text = "Accept",
                onClick = onAccept,
                enabled = acceptEnabled,
            )

            friend.status == FriendStatus.PENDING ->
                CubeChip(label = "Pending", tone = ChipTone.Neutral)
        }
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Friend rows · Light", showBackground = true)
@Preview(name = "Friend rows · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun FriendRowPreview() {
    CubeClashTheme {
        Column(
            modifier = Modifier
                .background(CubeClashTheme.colors.canvas)
                .width(360.dp)
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            FriendRow(
                friend = Friend("f-aiko", "aiko_m", FriendStatus.PENDING, "JP", null, 5_820, incoming = true),
                onOpen = {},
                onAccept = {},
            )
            FriendRow(
                friend = Friend("f-kian", "kian_r", FriendStatus.ACCEPTED, "IR", null, 6_310),
                onOpen = {},
            )
            FriendRow(
                friend = Friend("f-sora", "sora_h", FriendStatus.ACCEPTED, null, null, null),
                onOpen = {},
            )
            FriendRow(
                friend = Friend("f-owen", "owen_p", FriendStatus.PENDING, "GB", null, 9_140),
                onOpen = {},
            )
        }
    }
}
