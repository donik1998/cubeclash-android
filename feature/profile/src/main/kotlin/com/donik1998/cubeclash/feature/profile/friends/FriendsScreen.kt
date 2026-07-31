package com.donik1998.cubeclash.feature.profile.friends

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.Friend
import com.donik1998.cubeclash.core.model.FriendStatus
import com.donik1998.cubeclash.feature.profile.friends.component.FriendsContent

/**
 * Layer A: the only place that knows about state. It owns the [FriendsViewModel], subscribes to it,
 * wires retry/invite/accept, pops itself on back, and surfaces the one-shot invite/accept message
 * (the fake's "Enter a username to invite." validation, or "Invite sent.") through a snackbar. It
 * holds no layout or styling — everything visual lives in the stateless [FriendsScreen].
 *
 * [onOpenPlayer] is passed in by the app's nav graph and wired to the existing PlayerProfile screen;
 * this module does not depend on `:feature:stats`.
 */
@Composable
fun FriendsRoute(
    onBack: () -> Unit,
    onOpenPlayer: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FriendsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(onBack = onBack)

    val message = (uiState as? FriendsUiState.Success)?.transientMessage
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    FriendsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        onInvite = viewModel::onInvite,
        onAccept = viewModel::onAccept,
        onOpenPlayer = onOpenPlayer,
        modifier = modifier,
    )
}

/**
 * Layer B: the pure, testable screen — a back affordance plus the "Friends" header, then an
 * exhaustive `when` over the aggregate's three states: a skeleton while loading, a friends-specific
 * error with a retry, or the resolved [FriendsContent]. It imports no Hilt and no ViewModel, so
 * every state is reachable from a preview or a test with fixed data. The snackbar host is passed in
 * so the pure screen owns the placement while the Route owns the message.
 */
@Composable
fun FriendsScreen(
    uiState: FriendsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onInvite: (String) -> Unit,
    onAccept: (String) -> Unit,
    onOpenPlayer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(Spacing.xxl)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = CubeClashTheme.colors.textPrimary,
                    )
                }
                Text(
                    text = "Friends",
                    style = CubeClashTheme.typography.h1,
                    color = CubeClashTheme.colors.textPrimary,
                )
            }

            when (uiState) {
                is FriendsUiState.Loading -> FriendsSkeleton()

                is FriendsUiState.Failure -> FriendsError(
                    message = uiState.message,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )

                is FriendsUiState.Success -> FriendsContent(
                    incoming = uiState.incoming,
                    accepted = uiState.accepted,
                    outgoing = uiState.outgoing,
                    onInvite = onInvite,
                    onAccept = onAccept,
                    onOpenPlayer = onOpenPlayer,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** The loading placeholder: an invite-card block over a few row blocks. */
@Composable
private fun FriendsSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        SkeletonBlock(height = 132.dp)
        repeat(4) { SkeletonBlock(height = 64.dp) }
    }
}

@Composable
private fun SkeletonBlock(height: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(CubeClashTheme.colors.surfaceAlt, Radius.card),
    )
}

/**
 * The failure arm, framed for friends — `GET /friends` 404s against a real server today, so this
 * is a state a cuber will actually hit. The upstream message, when present, sits under the title so
 * a network blip still reads as a network blip.
 */
@Composable
private fun FriendsError(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Couldn't load your friends",
            style = CubeClashTheme.typography.h2,
            color = CubeClashTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message ?: "Something went wrong reaching your friends list.",
            style = CubeClashTheme.typography.body,
            color = CubeClashTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        CubeSecondaryButton(text = "Retry", onClick = onRetry)
    }
}

// --- Previews ---------------------------------------------------------------------------------

private val sampleFriends = listOf(
    Friend("f-aiko", "aiko_m", FriendStatus.PENDING, "JP", null, 5_820, incoming = true),
    Friend("f-kian", "kian_r", FriendStatus.ACCEPTED, "IR", null, 6_310),
    Friend("f-mira", "mira_v", FriendStatus.ACCEPTED, "DE", null, 7_020),
    Friend("f-sora", "sora_h", FriendStatus.ACCEPTED, null, null, null),
    Friend("f-owen", "owen_p", FriendStatus.PENDING, "GB", null, 9_140),
)

@Composable
private fun PreviewShell(uiState: FriendsUiState) {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas)) {
            FriendsScreen(
                uiState = uiState,
                snackbarHostState = remember { SnackbarHostState() },
                onBack = {},
                onRetry = {},
                onInvite = {},
                onAccept = {},
                onOpenPlayer = {},
            )
        }
    }
}

@Preview(name = "Populated · Light", showBackground = true)
@Preview(name = "Populated · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun FriendsScreenPopulatedPreview() {
    PreviewShell(FriendsUiState.Success(sampleFriends))
}

@Preview(name = "Empty · Light", showBackground = true)
@Preview(name = "Empty · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun FriendsScreenEmptyPreview() {
    PreviewShell(FriendsUiState.Success(emptyList()))
}

@Preview(name = "Loading · Light", showBackground = true)
@Preview(name = "Loading · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun FriendsScreenLoadingPreview() {
    PreviewShell(FriendsUiState.Loading)
}

@Preview(name = "Error · Light", showBackground = true)
@Preview(name = "Error · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun FriendsScreenErrorPreview() {
    PreviewShell(FriendsUiState.Failure("The network is unreachable."))
}
