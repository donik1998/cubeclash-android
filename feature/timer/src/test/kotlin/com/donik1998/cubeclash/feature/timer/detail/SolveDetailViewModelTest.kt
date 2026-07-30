package com.donik1998.cubeclash.feature.timer.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.donik1998.cubeclash.core.domain.common.AppError
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.SettingsRepository
import com.donik1998.cubeclash.core.domain.repository.SolveRepository
import com.donik1998.cubeclash.core.domain.usecase.DeleteSolveUseCase
import com.donik1998.cubeclash.core.domain.usecase.ObserveHistoryUseCase
import com.donik1998.cubeclash.core.domain.usecase.UpdatePenaltyUseCase
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SolveDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val solve = Solve(
        id = "solve-1",
        clientId = "solve-1",
        event = WcaEvent.THREE,
        scramble = Scramble.parse("R U R' U'", ScrambleNotation.FACE_TURN),
        scrambleSource = ScrambleSource.RANDOM,
        timeMs = 14_340,
        penalty = Penalty.NONE,
        solvedAt = Instant.EPOCH,
    )

    private fun viewModel(
        repository: FakeSolveRepository,
        solveId: String = "solve-1",
    ) = SolveDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(SolveDetailViewModel.SOLVE_ID_KEY to solveId)),
        observeHistory = ObserveHistoryUseCase(repository),
        updatePenalty = UpdatePenaltyUseCase(repository),
        deleteSolve = DeleteSolveUseCase(repository),
        settingsRepository = StubSettingsRepository(),
    )

    @Test
    fun `resolves the solve from the history flow`() = runTest {
        val vm = viewModel(FakeSolveRepository(listOf(solve)))
        vm.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals("solve-1", state.solve?.id)
            assertEquals(14_340L, state.solve?.timeMs)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing the penalty updates the displayed solve`() = runTest {
        val vm = viewModel(FakeSolveRepository(listOf(solve)))
        vm.uiState.test {
            var state = awaitItem()
            while (state.solve == null) state = awaitItem()

            vm.changePenalty(Penalty.PLUS_TWO)

            var updated = awaitItem()
            while (updated.solve?.penalty != Penalty.PLUS_TWO) updated = awaitItem()
            assertEquals(Penalty.PLUS_TWO, updated.solve?.penalty)
            // +2 folds two seconds into the ranked result.
            assertEquals(16_340L, updated.solve?.result?.rankingValue)
            assertFalse(updated.isSaving)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete flips isDeleted so the route can pop`() = runTest {
        val vm = viewModel(FakeSolveRepository(listOf(solve)))
        vm.uiState.test {
            var state = awaitItem()
            while (state.solve == null) state = awaitItem()

            vm.delete()

            var deleted = awaitItem()
            while (!deleted.isDeleted) deleted = awaitItem()
            assertTrue(deleted.isDeleted)
            assertFalse(deleted.isSaving)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an unknown id resolves to not-found once loading finishes`() = runTest {
        val vm = viewModel(FakeSolveRepository(listOf(solve)), solveId = "missing")
        vm.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertNull(state.solve)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeSolveRepository(initial: List<Solve>) : SolveRepository {
        private val history = MutableStateFlow(initial)

        override fun observeHistory(event: WcaEvent): Flow<List<Solve>> = history
        override fun observeSession(event: WcaEvent): Flow<List<Solve>> = history

        override suspend fun updatePenalty(solveId: String, penalty: Penalty): DataResult<Solve> {
            var updated: Solve? = null
            history.value = history.value.map { s ->
                if (s.id == solveId) s.copy(penalty = penalty).also { updated = it } else s
            }
            return updated?.let { DataResult.Success(it) }
                ?: DataResult.Failure(AppError.NotFound())
        }

        override suspend fun deleteSolve(solveId: String): DataResult<Unit> {
            history.value = history.value.filterNot { it.id == solveId }
            return DataResult.Success(Unit)
        }

        override suspend fun logSolve(solve: Solve): DataResult<Solve> = DataResult.Success(solve)
        override suspend fun clearSession(event: WcaEvent): DataResult<Unit> = DataResult.Success(Unit)
        override suspend fun sync(): DataResult<Int> = DataResult.Success(0)
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
