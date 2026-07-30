package com.donik1998.cubeclash.feature.timer.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.designsystem.theme.tabular
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.ResultFormatter
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleNotation
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.ui.EventIcon
import com.donik1998.cubeclash.core.ui.ScrambleCard
import com.donik1998.cubeclash.feature.timer.detail.component.PenaltyControls
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Layer A: owns the [SolveDetailViewModel], collects its state, and pops back once the solve is
 * deleted. No layout or styling lives here.
 */
@Composable
fun SolveDetailRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SolveDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Deleting is a one-way trip; pop exactly once when the flag flips.
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onBack()
    }

    SolveDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onChangePenalty = viewModel::changePenalty,
        onDelete = viewModel::delete,
        modifier = modifier,
    )
}

/**
 * Layer B: pure and testable. Given a resolved [SolveDetailUiState] and callbacks it draws the
 * readout, the penalty selector, the scramble, and the delete flow. It imports no Hilt and no
 * ViewModel, so loading, not-found and every penalty state are reachable from a preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolveDetailScreen(
    uiState: SolveDetailUiState,
    onBack: () -> Unit,
    onChangePenalty: (Penalty) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CubeClashTheme.colors.canvas,
        topBar = {
            TopAppBar(
                title = { Text("Solve", style = CubeClashTheme.typography.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CubeClashTheme.colors.canvas,
                    titleContentColor = CubeClashTheme.colors.textPrimary,
                    navigationIconContentColor = CubeClashTheme.colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val solve = uiState.solve
            when {
                uiState.isLoading -> LoadingBody()
                solve == null -> NotFoundBody(message = uiState.message)
                else -> SolveBody(
                    solve = solve,
                    isSaving = uiState.isSaving,
                    onChangePenalty = onChangePenalty,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
private fun SolveBody(
    solve: Solve,
    isSaving: Boolean,
    onChangePenalty: (Penalty) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirming by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = ResultFormatter.format(solve.result),
            style = CubeClashTheme.typography.display,
            color = if (solve.isDnf) CubeClashTheme.colors.danger else CubeClashTheme.colors.textPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        if (solve.penalty != Penalty.NONE) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                // Show the arithmetic — a bare "14.34+" hides where it came from.
                text = if (solve.isDnf) {
                    "Did not finish"
                } else {
                    "Raw ${ResultFormatter.formatDuration(solve.timeMs)} + 2.00 penalty"
                },
                style = CubeClashTheme.typography.caption.tabular(),
                color = CubeClashTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(Spacing.xl))
        PenaltyControls(
            event = solve.event,
            selected = solve.penalty,
            onSelect = onChangePenalty,
            enabled = !isSaving,
        )

        if (solve.isPb) {
            Spacer(Modifier.height(Spacing.md))
            CubeChip(label = "Personal best", tone = ChipTone.Brand)
        }

        Spacer(Modifier.height(Spacing.xl))
        // Line breaks matter: a Megaminx scramble is seven lines and ScrambleCard keeps them.
        ScrambleCard(scramble = solve.scramble, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(Spacing.lg))
        DetailField(label = "Event") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                EventIcon(event = solve.event, active = true, size = Spacing.lg)
                Text(
                    text = solve.event.displayName,
                    style = CubeClashTheme.typography.body,
                    color = CubeClashTheme.colors.textPrimary,
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        DetailField(label = "Solved") {
            Text(
                text = formatTimestamp(solve.solvedAt),
                style = CubeClashTheme.typography.body.tabular(),
                color = CubeClashTheme.colors.textPrimary,
            )
        }

        Spacer(Modifier.height(Spacing.xxl))
        CubeSecondaryButton(
            text = "Delete solve",
            onClick = { confirming = true },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.lg))
    }

    if (confirming) {
        DeleteConfirmationDialog(
            onConfirm = {
                confirming = false
                onDelete()
            },
            onDismiss = { confirming = false },
        )
    }
}

@Composable
private fun DetailField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        SectionHeader(label)
        content()
    }
}

/** In-process confirmation — never a blocking system dialog. */
@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CubeClashTheme.colors.surface,
        titleContentColor = CubeClashTheme.colors.textPrimary,
        textContentColor = CubeClashTheme.colors.textSecondary,
        title = { Text("Delete this solve?", style = CubeClashTheme.typography.title) },
        text = {
            Text(
                "It will be removed from your history and your averages.",
                style = CubeClashTheme.typography.small,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = CubeClashTheme.colors.danger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CubeClashTheme.colors.textSecondary)
            }
        },
    )
}

@Composable
private fun LoadingBody() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CubeClashTheme.colors.brandPrimary)
    }
}

@Composable
private fun NotFoundBody(message: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterVertically),
    ) {
        Text(
            text = "Solve not found",
            style = CubeClashTheme.typography.title,
            color = CubeClashTheme.colors.textPrimary,
        )
        Text(
            text = message ?: "It may have been deleted on another device.",
            style = CubeClashTheme.typography.small,
            color = CubeClashTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/** `19 Jul 2026 at 14:32` — deliberately absolute; a solve's timestamp is a record. */
private val TimestampFormat = DateTimeFormatter.ofPattern("d MMM uuuu 'at' HH:mm", Locale.US)

private fun formatTimestamp(instant: Instant): String =
    TimestampFormat.format(instant.atZone(ZoneId.systemDefault()))

// --- Previews ---------------------------------------------------------------------------------

private fun previewSolve(
    event: WcaEvent = WcaEvent.THREE,
    timeMs: Long = 14_340,
    penalty: Penalty = Penalty.NONE,
    isPb: Boolean = false,
    scramble: String = "R U R' U' F' U F R2 D' R U2 R' D R U' R'",
) = Solve(
    id = "s1",
    clientId = "s1",
    event = event,
    scramble = Scramble.parse(scramble, event.notation),
    scrambleSource = ScrambleSource.RANDOM,
    timeMs = timeMs,
    penalty = penalty,
    solvedAt = Instant.parse("2026-07-19T14:32:00Z"),
    isPb = isPb,
)

@Preview(name = "Detail · Light", showBackground = true)
@Preview(name = "Detail · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun SolveDetailPopulatedPreview() {
    CubeClashTheme {
        SolveDetailScreen(
            uiState = SolveDetailUiState("s1", solve = previewSolve(isPb = true), isLoading = false),
            onBack = {}, onChangePenalty = {}, onDelete = {},
        )
    }
}

@Preview(name = "Detail +2 · Light", showBackground = true)
@Preview(name = "Detail +2 · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun SolveDetailPlusTwoPreview() {
    CubeClashTheme {
        SolveDetailScreen(
            uiState = SolveDetailUiState(
                "s1",
                solve = previewSolve(penalty = Penalty.PLUS_TWO),
                isLoading = false,
            ),
            onBack = {}, onChangePenalty = {}, onDelete = {},
        )
    }
}

@Preview(name = "Detail Megaminx · Light", showBackground = true)
@Preview(name = "Detail Megaminx · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun SolveDetailMegaminxPreview() {
    CubeClashTheme {
        SolveDetailScreen(
            uiState = SolveDetailUiState(
                "s1",
                solve = previewSolve(
                    event = WcaEvent.MEGAMINX,
                    timeMs = 92_450,
                    scramble = "R++ D-- R++ D-- R-- D++ U'\nR-- D++ R++ D-- R++ D-- U'\n" +
                        "R++ D++ R-- D++ R-- D++ U'",
                ),
                isLoading = false,
            ),
            onBack = {}, onChangePenalty = {}, onDelete = {},
        )
    }
}

@Preview(name = "Not found · Light", showBackground = true)
@Preview(name = "Not found · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun SolveDetailNotFoundPreview() {
    CubeClashTheme {
        SolveDetailScreen(
            uiState = SolveDetailUiState("s1", solve = null, isLoading = false),
            onBack = {}, onChangePenalty = {}, onDelete = {},
        )
    }
}
