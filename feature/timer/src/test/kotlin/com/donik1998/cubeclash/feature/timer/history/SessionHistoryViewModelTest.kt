package com.donik1998.cubeclash.feature.timer.history

import app.cash.turbine.test
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.SettingsRepository
import com.donik1998.cubeclash.core.domain.repository.SolveRepository
import com.donik1998.cubeclash.core.domain.stats.SessionStatsCalculator
import com.donik1998.cubeclash.core.domain.usecase.ObserveHistoryUseCase
import com.donik1998.cubeclash.core.model.AppSettings
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleNotation
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.ThemeMode
import com.donik1998.cubeclash.core.model.TimerStyle
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.testing.MainDispatcherRule
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SessionHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun solve(id: String, daysAgo: Long, timeMs: Long) = Solve(
        id = id,
        clientId = id,
        event = WcaEvent.THREE,
        scramble = Scramble.parse("R U R' U'", ScrambleNotation.FACE_TURN),
        scrambleSource = ScrambleSource.RANDOM,
        timeMs = timeMs,
        penalty = Penalty.NONE,
        solvedAt = Instant.now().minus(daysAgo, ChronoUnit.DAYS),
    )

    private fun viewModel(repository: SolveRepository) = SessionHistoryViewModel(
        observeHistory = ObserveHistoryUseCase(repository),
        settingsRepository = StubSettingsRepository(),
        statsCalculator = SessionStatsCalculator(),
        zoneId = ZoneOffset.UTC,
    )

    @Test
    fun `groups solves by day, newest first`() = runTest {
        val repo = FakeSolveRepository(
            MutableStateFlow(listOf(solve("a", 0, 7_000), solve("b", 0, 9_000), solve("c", 2, 8_000))),
        )
        val vm = viewModel(repo)
        vm.uiState.test {
            var state = awaitItem()
            while (state !is SessionHistoryUiState.Content) state = awaitItem()
            val content = state as SessionHistoryUiState.Content
            assertEquals(2, content.groups.size)
            // Newest day first, and it holds the two solves from today.
            assertEquals(2, content.groups.first().count)
            assertEquals(7_000L, content.groups.first().best)
            assertTrue(content.groups.first().date.isAfter(content.groups.last().date))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty history resolves to an empty content state`() = runTest {
        val vm = viewModel(FakeSolveRepository(MutableStateFlow(emptyList())))
        vm.uiState.test {
            var state = awaitItem()
            while (state !is SessionHistoryUiState.Content) state = awaitItem()
            assertTrue((state as SessionHistoryUiState.Content).isEmpty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failing flow surfaces an error, and retry recovers`() = runTest {
        val repo = ThrowThenSucceedRepository(listOf(solve("a", 0, 7_000)))
        val vm = viewModel(repo)
        vm.uiState.test {
            var state = awaitItem()
            while (state !is SessionHistoryUiState.Error) state = awaitItem()
            assertTrue(state is SessionHistoryUiState.Error)

            vm.retry()

            var recovered = awaitItem()
            while (recovered !is SessionHistoryUiState.Content) recovered = awaitItem()
            assertEquals(1, (recovered as SessionHistoryUiState.Content).groups.sumOf { it.count })
            cancelAndIgnoreRemainingEvents()
        }
    }

    private open class FakeSolveRepository(
        private val history: MutableStateFlow<List<Solve>>,
    ) : SolveRepository {
        override fun observeHistory(event: WcaEvent): Flow<List<Solve>> = history
        override fun observeSession(event: WcaEvent): Flow<List<Solve>> = history
        override suspend fun updatePenalty(solveId: String, penalty: Penalty): DataResult<Solve> =
            DataResult.Failure(com.donik1998.cubeclash.core.domain.common.AppError.NotFound())
        override suspend fun deleteSolve(solveId: String): DataResult<Unit> = DataResult.Success(Unit)
        override suspend fun logSolve(solve: Solve): DataResult<Solve> = DataResult.Success(solve)
        override suspend fun clearSession(event: WcaEvent): DataResult<Unit> = DataResult.Success(Unit)
        override suspend fun sync(): DataResult<Int> = DataResult.Success(0)
    }

    /** Throws the first time it's collected, then serves [solves] on every later subscription. */
    private class ThrowThenSucceedRepository(private val solves: List<Solve>) :
        FakeSolveRepository(MutableStateFlow(emptyList())) {
        private var attempts = 0
        override fun observeHistory(event: WcaEvent): Flow<List<Solve>> = flow {
            if (attempts++ == 0) throw IllegalStateException("boom")
            emit(solves)
        }
    }

    private class StubSettingsRepository : SettingsRepository {
        override val settings: Flow<AppSettings> = MutableStateFlow(AppSettings())
        override suspend fun setThemeMode(mode: ThemeMode) = Unit
        override suspend fun setTimerStyle(style: TimerStyle) = Unit
        override suspend fun setInspectionEnabled(enabled: Boolean) = Unit
        override suspend fun setHapticsEnabled(enabled: Boolean) = Unit
        override suspend fun setLastEvent(event: WcaEvent) = Unit
    }
}
