package com.donik1998.cubeclash.feature.race.tournament

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.donik1998.cubeclash.core.domain.common.AppError
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentDetail
import com.donik1998.cubeclash.core.model.TournamentMatch
import com.donik1998.cubeclash.core.model.TournamentRound
import com.donik1998.cubeclash.core.model.TournamentStatus
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.testing.MainDispatcherRule
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TournamentDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun tournament(
        status: TournamentStatus = TournamentStatus.UPCOMING,
        entrants: Int = 4,
        capacity: Int = 16,
        registered: Boolean = false,
    ) = Tournament(
        id = "t-1",
        name = "Sub-15 Sprint",
        event = WcaEvent.THREE,
        status = status,
        entrants = entrants,
        capacity = capacity,
        startsAt = Instant.parse("2026-08-01T18:00:00Z"),
        description = "desc",
        registered = registered,
    )

    private fun detail(t: Tournament) = TournamentDetail(
        tournament = t,
        rounds = listOf(
            TournamentRound(
                name = "Final",
                matches = listOf(TournamentMatch("a", "b")),
            ),
        ),
    )

    private fun handle() = SavedStateHandle(mapOf("tournamentId" to "t-1"))

    // --- The register button-state matrix (pure derivation) -----------------------------------

    @Test
    fun `not registered, room to spare, joinable status maps to REGISTER`() {
        assertEquals(
            RegisterButtonState.REGISTER,
            RegisterButtonState.from(tournament(entrants = 4, capacity = 16, registered = false)),
        )
    }

    @Test
    fun `registered but not full maps to REGISTERED`() {
        assertEquals(
            RegisterButtonState.REGISTERED,
            RegisterButtonState.from(tournament(entrants = 4, capacity = 16, registered = true)),
        )
    }

    @Test
    fun `not registered and full maps to FULL`() {
        assertEquals(
            RegisterButtonState.FULL,
            RegisterButtonState.from(tournament(entrants = 16, capacity = 16, registered = false)),
        )
    }

    @Test
    fun `full AND registered maps to REGISTERED, never FULL`() {
        // The whole point: a full bracket the viewer is already in must not read as locked-out.
        assertEquals(
            RegisterButtonState.REGISTERED,
            RegisterButtonState.from(tournament(entrants = 16, capacity = 16, registered = true)),
        )
    }

    @Test
    fun `finished tournament has no register affordance regardless of registration`() {
        assertEquals(
            RegisterButtonState.FINISHED,
            RegisterButtonState.from(tournament(status = TournamentStatus.FINISHED)),
        )
        assertEquals(
            RegisterButtonState.FINISHED,
            RegisterButtonState.from(
                tournament(status = TournamentStatus.FINISHED, registered = true),
            ),
        )
    }

    @Test
    fun `unknown status is inert`() {
        assertEquals(
            RegisterButtonState.UNAVAILABLE,
            RegisterButtonState.from(tournament(status = TournamentStatus.UNKNOWN)),
        )
    }

    // --- ViewModel load / register / retry ----------------------------------------------------

    @Test
    fun `happy path exposes the loaded detail`() = runTest {
        val vm = TournamentDetailViewModel(
            handle(),
            StubTournamentRepository(detailResults = listOf(DataResult.Success(detail(tournament())))),
        )
        vm.uiState.test {
            var state = awaitItem()
            while (state !is TournamentDetailUiState.Success) state = awaitItem()
            assertEquals("Sub-15 Sprint", state.detail.tournament.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register success re-fetches and the detail flips to registered`() = runTest {
        val repo = StubTournamentRepository(
            // First load: not registered. After register, refresh returns the registered detail.
            detailResults = listOf(
                DataResult.Success(detail(tournament(registered = false))),
                DataResult.Success(detail(tournament(entrants = 5, registered = true))),
            ),
            registerResults = listOf(DataResult.Success(Unit)),
        )
        val vm = TournamentDetailViewModel(handle(), repo)
        vm.uiState.test {
            var state = awaitItem()
            while (state !is TournamentDetailUiState.Success) state = awaitItem()
            assertEquals(
                RegisterButtonState.REGISTER,
                RegisterButtonState.from(state.detail.tournament),
            )

            vm.onRegister()

            var after = awaitItem()
            while (after !is TournamentDetailUiState.Success ||
                !after.detail.tournament.registered
            ) {
                after = awaitItem()
            }
            assertEquals(
                RegisterButtonState.REGISTERED,
                RegisterButtonState.from(after.detail.tournament),
            )
            assertNull(after.registerError)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, repo.registerCalls)
        assertEquals(2, repo.detailCalls) // initial load + refresh
    }

    @Test
    fun `register failure keeps the detail and surfaces the error`() = runTest {
        val repo = StubTournamentRepository(
            detailResults = listOf(DataResult.Success(detail(tournament(registered = false)))),
            registerResults = listOf(DataResult.Failure(AppError.Validation("This bracket is full."))),
        )
        val vm = TournamentDetailViewModel(handle(), repo)
        vm.uiState.test {
            var state = awaitItem()
            while (state !is TournamentDetailUiState.Success) state = awaitItem()

            vm.onRegister()

            var after = awaitItem()
            while (after !is TournamentDetailUiState.Success || after.registerError == null) {
                after = awaitItem()
            }
            assertEquals("This bracket is full.", after.registerError)
            // Detail is still there — the failure didn't tear the screen down.
            assertEquals("Sub-15 Sprint", after.detail.tournament.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure path exposes the error message`() = runTest {
        val vm = TournamentDetailViewModel(
            handle(),
            StubTournamentRepository(detailResults = listOf(DataResult.Failure(AppError.NotFound("404")))),
        )
        vm.uiState.test {
            var state = awaitItem()
            while (state !is TournamentDetailUiState.Failure) state = awaitItem()
            assertEquals("404", state.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry after an error re-loads and succeeds`() = runTest {
        val repo = StubTournamentRepository(
            detailResults = listOf(
                DataResult.Failure(AppError.NotFound("404")),
                DataResult.Success(detail(tournament())),
            ),
        )
        val vm = TournamentDetailViewModel(handle(), repo)
        vm.uiState.test {
            var state = awaitItem()
            while (state !is TournamentDetailUiState.Failure) state = awaitItem()

            vm.onRetry()

            var retried = awaitItem()
            while (retried !is TournamentDetailUiState.Success) retried = awaitItem()
            assertEquals("Sub-15 Sprint", retried.detail.tournament.name)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, repo.detailCalls)
    }

    @Test
    fun `starts in a loading state`() = runTest {
        val vm = TournamentDetailViewModel(
            handle(),
            StubTournamentRepository(detailResults = listOf(DataResult.Success(detail(tournament())))),
        )
        assertTrue(vm.uiState.value is TournamentDetailUiState.Loading)
    }
}
