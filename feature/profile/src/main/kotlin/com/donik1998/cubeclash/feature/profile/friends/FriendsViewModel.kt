package com.donik1998.cubeclash.feature.profile.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.FriendsRepository
import com.donik1998.cubeclash.core.model.Friend
import com.donik1998.cubeclash.core.model.FriendStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The Friends aggregate's loading/success/failure lifecycle. [Success] carries the domain list
 * directly — no DTO ever reaches the UI — and pre-splits it into the three groups the screen
 * renders, in the order they need attention: incoming requests (the only rows with an Accept
 * action), then accepted friends, then outgoing invites the viewer is waiting on.
 *
 * [Success.transientMessage] is a one-shot surface for an invite/accept result (the fake's
 * "Enter a username to invite." validation, or a successful "Invite sent"): it rides on the same
 * state so the screen can show it without a second channel, and the screen clears it via
 * [onMessageShown] once consumed.
 */
sealed interface FriendsUiState {
    data object Loading : FriendsUiState

    data class Success(
        val friends: List<Friend>,
        val transientMessage: String? = null,
    ) : FriendsUiState {
        /** They invited you — the only rows the Accept action is offered on. */
        val incoming: List<Friend> = friends.filter { it.incoming }

        /** Accepted friendships, the main list. */
        val accepted: List<Friend> =
            friends.filter { !it.incoming && it.status == FriendStatus.ACCEPTED }

        /** Invites you sent, still pending — shown as "Pending" with no action. */
        val outgoing: List<Friend> =
            friends.filter { !it.incoming && it.status == FriendStatus.PENDING }

        /** No incoming, accepted, or outgoing rows at all — the empty state. */
        val isEmpty: Boolean = friends.isEmpty()
    }

    data class Failure(val message: String?) : FriendsUiState
}

/**
 * Layer A's brains: loads the viewer's friends on init and drives the invite/accept flow.
 *
 * ⚠️ `GET /friends` 404s against a real server today (the backend `friends` module is a bare
 * `.module.ts`), so [Failure] is a real, reachable state — not hypothetical. The fake is the
 * working path and it is stateful: [onAccept] flips an incoming row to accepted in place and
 * [onInvite] appends a pending outgoing row, so a reload after either reflects the change.
 */
@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FriendsUiState>(FriendsUiState.Loading)
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    /**
     * Sends an invite by handle or email, then reflects the result: a failure surfaces its message
     * (e.g. "Enter a username to invite.") without disturbing the list; a success re-loads so the
     * new pending row appears, carrying a confirmation message.
     */
    fun onInvite(query: String) {
        viewModelScope.launch {
            when (val result = friendsRepository.invite(query)) {
                is DataResult.Success -> reload(message = "Invite sent.")
                is DataResult.Failure -> showMessage(result.error.message)
            }
        }
    }

    /**
     * Accepts an incoming request, then re-loads so the accepted row moves out of the requests
     * section and into the friends list. A failure surfaces its message.
     */
    fun onAccept(userId: String) {
        viewModelScope.launch {
            when (val result = friendsRepository.accept(userId)) {
                is DataResult.Success -> reload(message = null)
                is DataResult.Failure -> showMessage(result.error.message)
            }
        }
    }

    /** Clears the one-shot invite/accept message once the screen has shown it. */
    fun onMessageShown() {
        _uiState.update { current ->
            if (current is FriendsUiState.Success) current.copy(transientMessage = null) else current
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = FriendsUiState.Loading
            _uiState.value = toState(friendsRepository.friends(), message = null)
        }
    }

    /** Re-fetches the list after a mutation, keeping the caller's confirmation/validation message. */
    private suspend fun reload(message: String?) {
        _uiState.value = toState(friendsRepository.friends(), message = message)
    }

    private fun toState(result: DataResult<List<Friend>>, message: String?): FriendsUiState =
        when (result) {
            is DataResult.Success -> FriendsUiState.Success(result.data, transientMessage = message)
            is DataResult.Failure -> FriendsUiState.Failure(result.error.message)
        }

    /** Attaches a transient message to an already-loaded list without a re-fetch. */
    private fun showMessage(message: String?) {
        _uiState.update { current ->
            if (current is FriendsUiState.Success) current.copy(transientMessage = message) else current
        }
    }
}
