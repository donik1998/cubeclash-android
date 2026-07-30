package com.donik1998.cubeclash.feature.stats.player

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.donik1998.cubeclash.core.domain.common.AppError
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.UserRepository
import com.donik1998.cubeclash.core.model.HeadToHead
import com.donik1998.cubeclash.core.model.PublicProfile
import com.donik1998.cubeclash.core.model.User
import com.donik1998.cubeclash.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlayerProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun profile(headToHead: HeadToHead?) = PublicProfile(
        id = "u-1",
        displayName = "kian_r",
        country = "IR",
        elo = 1180,
        bestSingleMs = 6_310,
        bestAo5Ms = 7_020,
        bestAo12Ms = 7_540,
        headToHead = headToHead,
    )

    private fun handle() = SavedStateHandle(mapOf("userId" to "u-1"))

    @Test
    fun `happy path exposes the public profile`() = runTest {
        val vm = PlayerProfileViewModel(
            savedStateHandle = handle(),
            userRepository = StubUserRepository(DataResult.Success(profile(HeadToHead(7, 3)))),
        )
        vm.uiState.test {
            var state = awaitItem()
            while (state !is PlayerProfileUiState.Success) state = awaitItem()
            assertEquals("kian_r", state.profile.displayName)
            assertEquals(1180, state.profile.elo)
            assertEquals(HeadToHead(7, 3), state.profile.headToHead)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `never-raced null head-to-head is preserved distinct from zero-zero`() = runTest {
        val vm = PlayerProfileViewModel(
            savedStateHandle = handle(),
            userRepository = StubUserRepository(DataResult.Success(profile(headToHead = null))),
        )
        vm.uiState.test {
            var state = awaitItem()
            while (state !is PlayerProfileUiState.Success) state = awaitItem()
            // Null must NOT be collapsed to HeadToHead(0, 0): they are different states.
            assertNull(state.profile.headToHead)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present zero-zero head-to-head is preserved distinct from null`() = runTest {
        val vm = PlayerProfileViewModel(
            savedStateHandle = handle(),
            userRepository = StubUserRepository(DataResult.Success(profile(HeadToHead(0, 0)))),
        )
        vm.uiState.test {
            var state = awaitItem()
            while (state !is PlayerProfileUiState.Success) state = awaitItem()
            // A present 0-0 means "raced, nobody won" — it survives as an object, not as null.
            assertEquals(HeadToHead(0, 0), state.profile.headToHead)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `all-null personal bests are preserved so each tile can render an em dash`() = runTest {
        val fresh = profile(headToHead = null).copy(
            bestSingleMs = null,
            bestAo5Ms = null,
            bestAo12Ms = null,
        )
        val vm = PlayerProfileViewModel(
            savedStateHandle = handle(),
            userRepository = StubUserRepository(DataResult.Success(fresh)),
        )
        vm.uiState.test {
            var state = awaitItem()
            while (state !is PlayerProfileUiState.Success) state = awaitItem()
            assertNull(state.profile.bestSingleMs)
            assertNull(state.profile.bestAo5Ms)
            assertNull(state.profile.bestAo12Ms)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure path exposes the error message`() = runTest {
        val vm = PlayerProfileViewModel(
            savedStateHandle = handle(),
            userRepository = StubUserRepository(DataResult.Failure(AppError.Network())),
        )
        vm.uiState.test {
            var state = awaitItem()
            while (state !is PlayerProfileUiState.Failure) state = awaitItem()
            assertEquals(AppError.Network().message, state.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry after an error re-loads and succeeds`() = runTest {
        val repo = StubUserRepository(
            DataResult.Failure(AppError.Network()),
            DataResult.Success(profile(HeadToHead(1, 0))),
        )
        val vm = PlayerProfileViewModel(savedStateHandle = handle(), userRepository = repo)
        vm.uiState.test {
            // First load fails.
            var state = awaitItem()
            while (state !is PlayerProfileUiState.Failure) state = awaitItem()

            vm.onRetry()

            // Retry drains the second (success) result.
            var retried = awaitItem()
            while (retried !is PlayerProfileUiState.Success) retried = awaitItem()
            assertEquals("kian_r", retried.profile.displayName)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, repo.calls)
    }

    @Test
    fun `starts in a loading state`() = runTest {
        val vm = PlayerProfileViewModel(
            savedStateHandle = handle(),
            userRepository = StubUserRepository(DataResult.Success(profile(null))),
        )
        // Before the dispatcher runs the launched coroutine, the state is Loading.
        assertTrue(vm.uiState.value is PlayerProfileUiState.Loading)
    }

    /**
     * Returns each supplied result once per call, in order, holding the last one thereafter — so a
     * fail-then-succeed sequence exercises error-then-retry with one stub.
     */
    private class StubUserRepository(
        private vararg val results: DataResult<PublicProfile>,
    ) : UserRepository {
        var calls = 0
            private set

        override fun observeMe(): Flow<User?> = flowOf(null)
        override suspend fun refreshMe(): DataResult<User> = error("unused")
        override suspend fun updateProfile(displayName: String?, country: String?): DataResult<User> =
            error("unused")

        override suspend fun publicProfile(id: String): DataResult<PublicProfile> {
            val result = results[calls.coerceAtMost(results.lastIndex)]
            calls++
            return result
        }
    }
}
