package com.donik1998.cubeclash.feature.auth.setup

import app.cash.turbine.test
import com.donik1998.cubeclash.core.domain.common.AppError
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.UserRepository
import com.donik1998.cubeclash.core.model.PublicProfile
import com.donik1998.cubeclash.core.model.User
import com.donik1998.cubeclash.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileSetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val registered = User(id = "u-1", displayName = "cuber99", email = "c@x.io")

    @Test
    fun `pre-fills the display name the user registered with`() = runTest {
        val vm = ProfileSetupViewModel(StubUserRepository(me = registered))
        vm.uiState.test {
            var state = awaitItem()
            while (state.displayName.isBlank()) state = awaitItem()
            assertEquals("cuber99", state.displayName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `blank display name blocks submit`() = runTest {
        val repo = StubUserRepository(me = null)
        val vm = ProfileSetupViewModel(repo)
        vm.setDisplayName("   ")
        assertFalse(vm.uiState.value.canSubmit)

        // A blank name never reaches the repository.
        vm.submit()
        assertEquals(0, repo.updateCalls)
    }

    @Test
    fun `a non-blank name enables submit`() = runTest {
        val vm = ProfileSetupViewModel(StubUserRepository(me = null))
        vm.setDisplayName("mira")
        assertTrue(vm.uiState.value.canSubmit)
    }

    @Test
    fun `successful update advances to done`() = runTest {
        val repo = StubUserRepository(me = null, results = arrayOf(DataResult.Success(registered)))
        val vm = ProfileSetupViewModel(repo)
        vm.setDisplayName("cuber99")
        vm.toggleCountry("UZ")

        vm.uiState.test {
            vm.submit()
            var state = awaitItem()
            while (!state.isDone) state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, repo.updateCalls)
        assertEquals("cuber99" to "UZ", repo.lastUpdate)
    }

    @Test
    fun `a failed update surfaces an error and stays out of the shell`() = runTest {
        val repo = StubUserRepository(
            me = null,
            results = arrayOf(DataResult.Failure(AppError.Network())),
        )
        val vm = ProfileSetupViewModel(repo)
        vm.setDisplayName("cuber99")

        vm.uiState.test {
            vm.submit()
            var state = awaitItem()
            while (state.error == null) state = awaitItem()
            assertEquals(AppError.Network().message, state.error)
            // A failed update must not advance into the shell.
            assertFalse(state.isDone)
            assertFalse(state.isSubmitting)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry after a failed update re-submits and succeeds`() = runTest {
        val repo = StubUserRepository(
            me = null,
            results = arrayOf(
                DataResult.Failure(AppError.Network()),
                DataResult.Success(registered),
            ),
        )
        val vm = ProfileSetupViewModel(repo)
        vm.setDisplayName("cuber99")

        vm.uiState.test {
            vm.submit()
            var failed = awaitItem()
            while (failed.error == null) failed = awaitItem()

            vm.submit() // same tap, now labelled "Try again"
            var done = awaitItem()
            while (!done.isDone) done = awaitItem()
            assertNull(done.error)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, repo.updateCalls)
    }

    @Test
    fun `skip advances without calling updateProfile`() = runTest {
        val repo = StubUserRepository(me = null)
        val vm = ProfileSetupViewModel(repo)

        vm.skip()

        assertTrue(vm.uiState.value.isDone)
        assertEquals(0, repo.updateCalls)
    }

    @Test
    fun `toggling the selected country clears it`() = runTest {
        val vm = ProfileSetupViewModel(StubUserRepository(me = null))
        vm.toggleCountry("UZ")
        assertEquals("UZ", vm.uiState.value.country)
        vm.toggleCountry("UZ")
        assertNull(vm.uiState.value.country)
    }

    /**
     * A [UserRepository] that pre-fills [observeMe] with [me] and returns each [results] entry once
     * per `updateProfile` call, in order, holding the last thereafter — so a fail-then-succeed
     * sequence exercises error-then-retry with one stub.
     */
    private class StubUserRepository(
        private val me: User?,
        private vararg val results: DataResult<User>,
    ) : UserRepository {
        var updateCalls = 0
            private set
        var lastUpdate: Pair<String?, String?>? = null
            private set

        override fun observeMe(): Flow<User?> = flowOf(me)
        override suspend fun refreshMe(): DataResult<User> = error("unused")
        override suspend fun publicProfile(id: String): DataResult<PublicProfile> = error("unused")

        override suspend fun updateProfile(displayName: String?, country: String?): DataResult<User> {
            lastUpdate = displayName to country
            val result = results[updateCalls.coerceAtMost(results.lastIndex)]
            updateCalls++
            return result
        }
    }
}
