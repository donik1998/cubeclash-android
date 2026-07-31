package com.donik1998.cubeclash.feature.profile.friends.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.Friend
import com.donik1998.cubeclash.core.ui.EmptyState

/**
 * The populated Friends body — Figma frame content. A scrolling list that always leads with the
 * [InviteCard], then, in the order they need attention:
 *  1. **Requests** — incoming invites, the only rows with an Accept button;
 *  2. **Friends** — accepted friendships;
 *  3. **Invited** — outgoing invites the viewer is waiting on, shown as "Pending".
 *
 * When there are no friends at all it shows a human [EmptyState] under the invite card, so the
 * invite affordance is never hidden behind the empty message. It imports no Hilt and no ViewModel:
 * every arrangement is reachable from a preview with fixed data.
 */
@Composable
fun FriendsContent(
    incoming: List<Friend>,
    accepted: List<Friend>,
    outgoing: List<Friend>,
    onInvite: (String) -> Unit,
    onAccept: (String) -> Unit,
    onOpenPlayer: (String) -> Unit,
    modifier: Modifier = Modifier,
    submitting: Boolean = false,
) {
    val isEmpty = incoming.isEmpty() && accepted.isEmpty() && outgoing.isEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item(key = "invite") {
            InviteCard(onInvite = onInvite, submitting = submitting)
        }

        if (isEmpty) {
            item(key = "empty") {
                EmptyState(
                    title = "No friends yet",
                    message = "Invite someone above and their times show up on your friends " +
                        "leaderboard.",
                    modifier = Modifier.padding(top = Spacing.xl),
                )
            }
            return@LazyColumn
        }

        if (incoming.isNotEmpty()) {
            section(title = "Requests · ${incoming.size}", key = "requests")
            items(incoming, key = { "in-${it.userId}" }) { friend ->
                FriendRow(
                    friend = friend,
                    onOpen = { onOpenPlayer(friend.userId) },
                    onAccept = { onAccept(friend.userId) },
                    acceptEnabled = !submitting,
                )
            }
        }

        if (accepted.isNotEmpty()) {
            section(title = "Friends", key = "friends")
            items(accepted, key = { "ac-${it.userId}" }) { friend ->
                FriendRow(friend = friend, onOpen = { onOpenPlayer(friend.userId) })
            }
        }

        if (outgoing.isNotEmpty()) {
            section(title = "Invited", key = "invited")
            items(outgoing, key = { "out-${it.userId}" }) { friend ->
                FriendRow(friend = friend, onOpen = { onOpenPlayer(friend.userId) })
            }
        }
    }
}

/** A spaced [SectionHeader] as its own list item, so groups read apart from the rows above. */
private fun LazyListScope.section(title: String, key: String) {
    item(key = key) {
        Column(modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs)) {
            SectionHeader(title)
        }
    }
}
