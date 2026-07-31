package com.donik1998.cubeclash.feature.race.tournament

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.TournamentStatus
import com.donik1998.cubeclash.feature.race.tournament.component.sampleTournament

/**
 * Layer B: the pure, testable detail screen — a back affordance plus the "Tournament" header, then
 * an exhaustive `when` over the three states: a skeleton while loading, a "Couldn't load
 * tournament" error with a retry (real — the endpoint 404s today), or the resolved
 * [TournamentDetailContent]. It imports no Hilt and no ViewModel.
 */
@Composable
fun TournamentDetailScreen(
    uiState: TournamentDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRegister: () -> Unit,
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
                text = "Tournament",
                style = CubeClashTheme.typography.h1,
                color = CubeClashTheme.colors.textPrimary,
            )
        }

        when (uiState) {
            is TournamentDetailUiState.Loading -> TournamentDetailSkeleton()

            is TournamentDetailUiState.Failure -> TournamentDetailError(
                message = uiState.message,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )

            is TournamentDetailUiState.Success -> TournamentDetailContent(
                detail = uiState.detail,
                onRegister = onRegister,
                registering = uiState.registering,
                registerError = uiState.registerError,
            )
        }
    }
}

/** The loading placeholder: a header block, a button block and two match blocks. */
@Composable
private fun TournamentDetailSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SkeletonBlock(height = 140.dp)
        SkeletonBlock(height = 48.dp)
        SkeletonBlock(height = 84.dp)
        SkeletonBlock(height = 84.dp)
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

/** The failure arm. `GET /tournaments/{id}` 404s on the live server today, so this is a real path. */
@Composable
private fun TournamentDetailError(
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
            text = "Couldn't load tournament",
            style = CubeClashTheme.typography.h2,
            color = CubeClashTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message ?: "We couldn't load this bracket.",
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
private fun TournamentDetailLoadingPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas)) {
            TournamentDetailScreen(TournamentDetailUiState.Loading, {}, {}, {})
        }
    }
}

@Preview(name = "Error · Light", showBackground = true)
@Preview(name = "Error · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun TournamentDetailErrorPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas)) {
            TournamentDetailScreen(
                TournamentDetailUiState.Failure("That tournament could not be found (404)."),
                {}, {}, {},
            )
        }
    }
}

@Preview(name = "Success · Light", showBackground = true)
@Preview(name = "Success · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun TournamentDetailSuccessPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas)) {
            TournamentDetailScreen(
                TournamentDetailUiState.Success(
                    sampleDetail(
                        sampleTournament(
                            name = "Global Weekly · 3×3",
                            status = TournamentStatus.LIVE,
                            entrants = 64,
                            capacity = 64,
                        ),
                    ),
                ),
                {}, {}, {},
            )
        }
    }
}
