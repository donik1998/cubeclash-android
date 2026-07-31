package com.donik1998.cubeclash.feature.race.tournament

import app.cash.turbine.test
import com.donik1998.cubeclash.core.domain.common.AppError
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentStatus
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.testing.MainDispatcherRule
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TournamentListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun tournament(id: String, name: String) = Tournament(
        id = id,
        name = name,
        event = WcaEvent.THREE,
        status = TournamentStatus.UPCOMING,
        entrants = 4,
        capacity = 16,
        startsAt = Instant.parse("2026-08-01T18:00:00Z"),
        description = "",
    )

    @Test
    fun `happy path exposes the loaded list`() = runTest {
        val list = listOf(tournament("a", "Alpha"), tournament("b", "Beta"))
        val vm = TournamentListViewModel(
            StubTournamentRepository(listResults = listOf(DataResult.Success(list))),
        )
        vm.uiState.test {
            var state = awaitItem()
            while (state !is TournamentListUiState.Success) state = awaitItem()
            assertEquals(2, state.tournaments.size)
            assertEquals("Alpha", state.tournaments.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty list surfaces as an empty Success, not a Failure`() = runTest {
        val vm = TournamentListViewModel(
            StubTournamentRepository(listResults = listOf(DataResult.Success(emptyList()))),
        )
        vm.uiState.test {
            var state = awaitItem()
            while (state !is TournamentListUiState.Success) state = awaitItem()
            assertTrue(state.tournaments.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure path exposes the error message`() = runTest {
        val vm = TournamentListViewModel(
            StubTournamentRepository(listResults = listOf(DataResult.Failure(AppError.Network()))),
        )
        vm.uiState.test {
            var state = awaitItem()
            while (state !is TournamentListUiState.Failure) state = awaitItem()
            assertEquals(AppError.Network().message, state.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry after an error re-loads and succeeds`() = runTest {
        val repo = StubTournamentRepository(
            listResults = listOf(
                DataResult.Failure(AppError.NotFound("404")),
                DataResult.Success(listOf(tournament("a", "Alpha"))),
            ),
        )
        val vm = TournamentListViewModel(repo)
        vm.uiState.test {
            var state = awaitItem()
            while (state !is TournamentListUiState.Failure) state = awaitItem()

            vm.onRetry()

            var retried = awaitItem()
            while (retried !is TournamentListUiState.Success) retried = awaitItem()
            assertEquals("Alpha", retried.tournaments.single().name)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, repo.listCalls)
    }

    @Test
    fun `starts in a loading state`() = runTest {
        val vm = TournamentListViewModel(
            StubTournamentRepository(listResults = listOf(DataResult.Success(emptyList()))),
        )
        assertTrue(vm.uiState.value is TournamentListUiState.Loading)
    }
}
