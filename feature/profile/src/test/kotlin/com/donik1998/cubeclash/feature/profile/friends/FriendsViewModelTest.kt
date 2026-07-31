package com.donik1998.cubeclash.feature.profile.friends

import app.cash.turbine.test
import com.donik1998.cubeclash.core.domain.common.AppError
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.FriendsRepository
import com.donik1998.cubeclash.core.model.Friend
import com.donik1998.cubeclash.core.model.FriendStatus
import com.donik1998.cubeclash.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FriendsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val incoming =
        Friend("f-aiko", "aiko_m", FriendStatus.PENDING, "JP", null, 5_820, incoming = true)
    private val outgoing =
        Friend("f-owen", "owen_p", FriendStatus.PENDING, "GB", null, 9_140, incoming = false)
    private val accepted =
        Friend("f-kian", "kian_r", FriendStatus.ACCEPTED, "IR", null, 6_310)

    @Test
    fun `incoming and outgoing pending invites land in different sections with different actions`() =
        runTest {
            val vm = FriendsViewModel(
                StubFriendsRepository(listOf(incoming, outgoing, accepted)),
            )
            vm.uiState.test {
                val state = awaitSuccess()
                // Incoming request is the only row Accept is offered on.
                assertEquals(listOf("f-aiko"), state.incoming.map { it.userId })
                // Outgoing invite is a distinct group — "Pending", no Accept.
                assertEquals(listOf("f-owen"), state.outgoing.map { it.userId })
                assertEquals(listOf("f-kian"), state.accepted.map { it.userId })
                // The outgoing pending row never sits in the incoming (Accept-able) group.
                assertFalse(state.incoming.any { it.userId == "f-owen" })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `accept flips an incoming request out of the requests section into friends`() = runTest {
        val vm = FriendsViewModel(StubFriendsRepository(listOf(incoming, accepted)))
        vm.uiState.test {
            awaitSuccess() // initial load

            vm.onAccept("f-aiko")

            var next = awaitItem()
            while (next !is FriendsUiState.Success || next.incoming.isNotEmpty()) next = awaitItem()
            // No longer an incoming request…
            assertTrue(next.incoming.isEmpty())
            // …and now an accepted friend that is not incoming.
            val row = next.accepted.first { it.userId == "f-aiko" }
            assertEquals(FriendStatus.ACCEPTED, row.status)
            assertFalse(row.incoming)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an empty friends list surfaces the empty state`() = runTest {
        val vm = FriendsViewModel(StubFriendsRepository(emptyList()))
        vm.uiState.test {
            val state = awaitSuccess()
            assertTrue(state.isEmpty)
            assertTrue(state.incoming.isEmpty())
            assertTrue(state.accepted.isEmpty())
            assertTrue(state.outgoing.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failed first load exposes the error and retry re-loads successfully`() = runTest {
        val repo = StubFriendsRepository(
            listOf(accepted),
            firstLoadError = AppError.NotFound("No friends endpoint yet."),
        )
        val vm = FriendsViewModel(repo)
        vm.uiState.test {
            var state = awaitItem()
            while (state !is FriendsUiState.Failure) state = awaitItem()
            assertEquals("No friends endpoint yet.", state.message)

            vm.onRetry()

            val retried = awaitSuccess()
            assertEquals(listOf("f-kian"), retried.accepted.map { it.userId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an invite with an empty query surfaces the validation message and does not add a row`() =
        runTest {
            val vm = FriendsViewModel(StubFriendsRepository(listOf(accepted)))
            vm.uiState.test {
                awaitSuccess()

                vm.onInvite("   ")

                var state = awaitItem()
                while (state !is FriendsUiState.Success || state.transientMessage == null) {
                    state = awaitItem()
                }
                assertEquals("Enter a username to invite.", state.transientMessage)
                // The list is unchanged — no phantom pending row.
                assertEquals(listOf("f-kian"), state.friends.map { it.userId })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a valid invite appends a pending outgoing row and confirms`() = runTest {
        val vm = FriendsViewModel(StubFriendsRepository(listOf(accepted)))
        vm.uiState.test {
            awaitSuccess()

            vm.onInvite("newcomer")

            var state = awaitItem()
            while (state !is FriendsUiState.Success || state.outgoing.isEmpty()) state = awaitItem()
            assertEquals("Invite sent.", state.transientMessage)
            val invited = state.outgoing.single()
            assertEquals("newcomer", invited.displayName)
            assertEquals(FriendStatus.PENDING, invited.status)
            assertFalse(invited.incoming)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `starts in a loading state`() = runTest {
        val vm = FriendsViewModel(StubFriendsRepository(emptyList()))
        assertTrue(vm.uiState.value is FriendsUiState.Loading)
    }

    @Test
    fun `the transient message clears once the screen has shown it`() = runTest {
        val vm = FriendsViewModel(StubFriendsRepository(listOf(accepted)))
        vm.uiState.test {
            awaitSuccess()
            vm.onInvite("newcomer")

            var withMessage = awaitItem()
            while (withMessage !is FriendsUiState.Success || withMessage.transientMessage == null) {
                withMessage = awaitItem()
            }

            vm.onMessageShown()

            var cleared = awaitItem()
            while (cleared !is FriendsUiState.Success || cleared.transientMessage != null) {
                cleared = awaitItem()
            }
            assertNull(cleared.transientMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun app.cash.turbine.ReceiveTurbine<FriendsUiState>.awaitSuccess(): FriendsUiState.Success {
        var state = awaitItem()
        while (state !is FriendsUiState.Success) state = awaitItem()
        return state
    }

    /**
     * Stateful in-memory friends repo mirroring `FakeFriendsRepository`: [accept] flips an incoming
     * row to accepted in place and [invite] appends a pending outgoing row, so the ViewModel's
     * re-load after either reflects the change. [firstLoadError], when set, fails the first
     * `friends()` call only — exercising error-then-retry with one stub.
     */
    private class StubFriendsRepository(
        initial: List<Friend>,
        private val firstLoadError: AppError? = null,
    ) : FriendsRepository {
        private var rows = initial
        private var loads = 0

        override suspend fun friends(): DataResult<List<Friend>> {
            loads++
            if (loads == 1 && firstLoadError != null) return DataResult.Failure(firstLoadError)
            return DataResult.Success(rows)
        }

        override suspend fun invite(query: String): DataResult<Unit> {
            val q = query.trim()
            if (q.isEmpty()) {
                return DataResult.Failure(AppError.Validation("Enter a username to invite."))
            }
            rows = rows + Friend(
                userId = "invited-${q.lowercase()}",
                displayName = q,
                status = FriendStatus.PENDING,
                incoming = false,
            )
            return DataResult.Success(Unit)
        }

        override suspend fun accept(userId: String): DataResult<Unit> {
            val target = rows.firstOrNull { it.userId == userId }
                ?: return DataResult.Failure(AppError.NotFound("That request is gone."))
            if (!target.incoming) {
                return DataResult.Failure(AppError.Validation("Only incoming requests can be accepted."))
            }
            rows = rows.map {
                if (it.userId == userId) it.copy(status = FriendStatus.ACCEPTED, incoming = false) else it
            }
            return DataResult.Success(Unit)
        }
    }
}
