package com.donik1998.cubeclash.feature.stats

import app.cash.turbine.test
import com.donik1998.cubeclash.core.domain.common.AppError
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.SettingsRepository
import com.donik1998.cubeclash.core.domain.repository.StatsRepository
import com.donik1998.cubeclash.core.model.AppSettings
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.LeaderboardEntry
import com.donik1998.cubeclash.core.model.LeaderboardMetric
import com.donik1998.cubeclash.core.model.LeaderboardPage
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.ThemeMode
import com.donik1998.cubeclash.core.model.TimerStyle
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.testing.MainDispatcherRule
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val page = LeaderboardPage(
        entries = listOf(
            LeaderboardEntry(1, "a", "kian_r", "IR", 6000, WcaEvent.THREE, Instant.EPOCH),
            LeaderboardEntry(2, "b", "mira_v", null, 6200, WcaEvent.THREE, Instant.EPOCH),
        ),
        nextCursor = "c2",
        viewer = LeaderboardEntry(1204, "me", "me", "UZ", 9000, WcaEvent.THREE, Instant.EPOCH),
    )

    @Test
    fun `success path exposes the mapped page`() = runTest {
        val vm = StatsViewModel(
            statsRepository = StubStatsRepository(DataResult.Success(page)),
            settingsRepository = StubSettingsRepository(),
        )
        vm.uiState.test {
            // Skip states until the leaderboard resolves to Success.
            var success = awaitItem().leaderboard
            while (success !is LeaderboardUiState.Success) success = awaitItem().leaderboard
            assertEquals(2, success.page.entries.size)
            assertEquals(1204, success.page.viewer?.rank)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure path exposes the error message`() = runTest {
        val vm = StatsViewModel(
            statsRepository = StubStatsRepository(DataResult.Failure(AppError.Network())),
            settingsRepository = StubSettingsRepository(),
        )
        vm.uiState.test {
            var failure = awaitItem().leaderboard
            while (failure !is LeaderboardUiState.Failure) failure = awaitItem().leaderboard
            assertEquals(AppError.Network().message, failure.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `starts in a loading state`() = runTest {
        val vm = StatsViewModel(
            statsRepository = StubStatsRepository(DataResult.Success(page)),
            settingsRepository = StubSettingsRepository(),
        )
        // Before the dispatcher runs the launched coroutine, the leaderboard is Loading.
        assertTrue(vm.uiState.value.leaderboard is LeaderboardUiState.Loading)
    }

    private class StubStatsRepository(
        private val result: DataResult<LeaderboardPage>,
    ) : StatsRepository {
        override fun observeStats(event: WcaEvent): Flow<EventStats> = flowOf(EventStats(event))
        override suspend fun leaderboard(
            event: WcaEvent,
            metric: LeaderboardMetric,
            scope: LeaderboardScope,
            cursor: String?,
        ): DataResult<LeaderboardPage> = result
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
