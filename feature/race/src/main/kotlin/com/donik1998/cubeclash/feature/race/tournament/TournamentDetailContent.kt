package com.donik1998.cubeclash.feature.race.tournament

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
import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentDetail
import com.donik1998.cubeclash.core.model.TournamentStatus
import com.donik1998.cubeclash.feature.race.tournament.component.BracketSection
import com.donik1998.cubeclash.feature.race.tournament.component.RegisterButton
import com.donik1998.cubeclash.feature.race.tournament.component.TournamentHeader
import com.donik1998.cubeclash.feature.race.tournament.component.sampleRounds
import com.donik1998.cubeclash.feature.race.tournament.component.sampleTournament

/**
 * Layer B: the pure, testable body of a resolved tournament — the header, the register affordance
 * (its state derived once, purely, from the model) and the bracket, scrollable. It imports no Hilt
 * and no ViewModel, so every combination — full-but-registered included — is reachable from a
 * preview or a test with fixed data.
 */
@Composable
fun TournamentDetailContent(
    detail: TournamentDetail,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
    registering: Boolean = false,
    registerError: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        TournamentHeader(tournament = detail.tournament)
        RegisterButton(
            state = RegisterButtonState.from(detail.tournament),
            onRegister = onRegister,
            registering = registering,
            registerError = registerError,
        )
        BracketSection(rounds = detail.rounds)
    }
}

// --- Sample data ------------------------------------------------------------------------------

internal fun sampleDetail(tournament: Tournament) =
    TournamentDetail(tournament = tournament, rounds = sampleRounds())

// --- Previews (the button-state matrix) -------------------------------------------------------

@Preview(name = "Register available · Light", showBackground = true)
@Preview(name = "Register available · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun DetailRegisterAvailablePreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            TournamentDetailContent(
                detail = sampleDetail(
                    sampleTournament(status = TournamentStatus.UPCOMING, entrants = 22, capacity = 32),
                ),
                onRegister = {},
            )
        }
    }
}

@Preview(name = "Already registered · Light", showBackground = true)
@Preview(name = "Already registered · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun DetailRegisteredPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            TournamentDetailContent(
                detail = sampleDetail(
                    sampleTournament(
                        status = TournamentStatus.UPCOMING,
                        entrants = 13,
                        capacity = 16,
                        registered = true,
                    ),
                ),
                onRegister = {},
            )
        }
    }
}

@Preview(name = "Full, not registered · Light", showBackground = true)
@Preview(name = "Full, not registered · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun DetailFullPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            TournamentDetailContent(
                detail = sampleDetail(
                    sampleTournament(status = TournamentStatus.LIVE, entrants = 64, capacity = 64),
                ),
                onRegister = {},
            )
        }
    }
}

@Preview(name = "Full AND registered · Light", showBackground = true)
@Preview(name = "Full AND registered · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun DetailFullAndRegisteredPreview() {
    // Proves a full bracket the viewer is already in reads "Registered", not the locked-out "Full".
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            TournamentDetailContent(
                detail = sampleDetail(
                    sampleTournament(
                        status = TournamentStatus.LIVE,
                        entrants = 64,
                        capacity = 64,
                        registered = true,
                    ),
                ),
                onRegister = {},
            )
        }
    }
}
