package com.donik1998.cubeclash.feature.stats.player

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.donik1998.cubeclash.core.model.HeadToHead

/**
 * Layer A: the only place that knows about state. It owns the [PlayerProfileViewModel] (which reads
 * the tapped `userId` from the back stack), subscribes to it, wires the retry, and pops itself on
 * back. It holds no layout or styling — everything visual lives in the stateless [PlayerProfileScreen].
 */
@Composable
fun PlayerProfileRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    PlayerProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

/**
 * Layer B: the pure, testable screen — a back affordance plus the "Player" header, then an
 * exhaustive `when` over the aggregate's three states: a skeleton while loading, a "Player not
 * found" error with a retry, or the resolved [PlayerProfileContent]. It imports no Hilt and no
 * ViewModel, so every state is reachable from a preview or a test with fixed data.
 */
@Composable
fun PlayerProfileScreen(
    uiState: PlayerProfileUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
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
                text = "Player",
                style = CubeClashTheme.typography.h1,
                color = CubeClashTheme.colors.textPrimary,
            )
        }

        when (uiState) {
            is PlayerProfileUiState.Loading -> PlayerProfileSkeleton()

            is PlayerProfileUiState.Failure -> PlayerProfileError(
                message = uiState.message,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )

            is PlayerProfileUiState.Success -> PlayerProfileContent(profile = uiState.profile)
        }
    }
}

/** The loading placeholder: a hero block, three tile blocks and a head-to-head block. */
@Composable
private fun PlayerProfileSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SkeletonBlock(height = 180.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            repeat(3) { SkeletonBlock(height = 72.dp, modifier = Modifier.weight(1f)) }
        }
        SkeletonBlock(height = 96.dp)
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
 * The failure arm, framed as "Player not found" — the mapper folds a 404 and a malformed body into
 * the same failure, and to the viewer both mean the profile they tapped won't open. The upstream
 * message, when present, sits under the title so a network blip still reads as a network blip.
 */
@Composable
private fun PlayerProfileError(
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
            text = "Player not found",
            style = CubeClashTheme.typography.h2,
            color = CubeClashTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message ?: "We couldn't load this player.",
            style = CubeClashTheme.typography.body,
            color = CubeClashTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        CubeSecondaryButton(text = "Retry", onClick = onRetry)
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Loading · Light", showBackground = true)
@Preview(name = "Loading · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PlayerProfileScreenLoadingPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas)) {
            PlayerProfileScreen(PlayerProfileUiState.Loading, {}, {})
        }
    }
}

@Preview(name = "Not found · Light", showBackground = true)
@Preview(name = "Not found · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PlayerProfileScreenErrorPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas)) {
            PlayerProfileScreen(PlayerProfileUiState.Failure("The network is unreachable."), {}, {})
        }
    }
}

@Preview(name = "Success · Light", showBackground = true)
@Preview(name = "Success · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun PlayerProfileScreenSuccessPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas)) {
            PlayerProfileScreen(
                PlayerProfileUiState.Success(samplePublicProfile(headToHead = HeadToHead(0, 0))),
                {}, {},
            )
        }
    }
}
