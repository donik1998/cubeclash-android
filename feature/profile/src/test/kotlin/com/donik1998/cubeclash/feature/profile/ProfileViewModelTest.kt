package com.donik1998.cubeclash.feature.profile

import app.cash.turbine.test
import com.donik1998.cubeclash.core.domain.common.AppError
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.ProfileRepository
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.PlayerProfile
import com.donik1998.cubeclash.core.model.ProfileRank
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profile = PlayerProfile(
        id = "me",
        displayName = "cuber_98",
        country = "UZ",
        elo = 1180,
        rank = ProfileRank(1204, WcaEvent.THREE, "single", LeaderboardScope.GLOBAL),
        bestSingleMs = 8420,
        bestSingleEvent = WcaEvent.THREE,
        totalSolves = 3204,
        winRate = 0.68,
        wins = 217,
        losses = 102,
        friendCount = 48,
    )

    @Test
    fun `success path exposes the profile`() = runTest {
        val vm = ProfileViewModel(StubProfileRepository(DataResult.Success(profile)))
        vm.uiState.test {
            var state = awaitItem()
            while (state !is ProfileUiState.Success) state = awaitItem()
            assertEquals("cuber_98", state.profile.displayName)
            assertEquals(1204, state.profile.rank?.position)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure path exposes the error message`() = runTest {
        val vm = ProfileViewModel(StubProfileRepository(DataResult.Failure(AppError.Network())))
        vm.uiState.test {
            var state = awaitItem()
            while (state !is ProfileUiState.Failure) state = awaitItem()
            assertEquals(AppError.Network().message, state.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `starts in a loading state`() = runTest {
        val vm = ProfileViewModel(StubProfileRepository(DataResult.Success(profile)))
        // Before the dispatcher runs the launched coroutine, the state is Loading.
        assertTrue(vm.uiState.value is ProfileUiState.Loading)
    }

    private class StubProfileRepository(
        private val result: DataResult<PlayerProfile>,
    ) : ProfileRepository {
        override suspend fun profile(
            event: WcaEvent,
            rankScope: LeaderboardScope,
        ): DataResult<PlayerProfile> = result
    }
}
