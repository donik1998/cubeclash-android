package com.donik1998.cubeclash.feature.stats.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.UserRepository
import com.donik1998.cubeclash.core.model.PublicProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Another player's public profile, reached by tapping a name on a leaderboard. [Success] carries
 * the domain [PublicProfile] directly — no DTO reaches the UI — and [Failure] carries a renderable
 * message plus a retry affordance.
 *
 * There is no dedicated "not found" arm: the mapper drops an un-renderable profile (missing id or
 * name) into a `DataResult.Failure` upstream, so a 404 and a malformed body both surface here as
 * [Failure]. The screen frames that failure as "Player not found" with a retry.
 */
sealed interface PlayerProfileUiState {
    data object Loading : PlayerProfileUiState
    data class Success(val profile: PublicProfile) : PlayerProfileUiState
    data class Failure(val message: String?) : PlayerProfileUiState
}

/**
 * Layer A's brains: loads the public profile for the [PlayerProfileDestination.userId] carried in
 * the back stack. The id is read from [SavedStateHandle] under the destination's property name,
 * the same type-safe-nav convention `SolveDetailViewModel` uses.
 */
@HiltViewModel
class PlayerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val userId: String = requireNotNull(savedStateHandle[USER_ID_KEY]) {
        "PlayerProfileDestination requires a userId argument"
    }

    private val _uiState = MutableStateFlow<PlayerProfileUiState>(PlayerProfileUiState.Loading)
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = PlayerProfileUiState.Loading
            _uiState.value = when (val result = userRepository.publicProfile(userId)) {
                is DataResult.Success -> PlayerProfileUiState.Success(result.data)
                is DataResult.Failure -> PlayerProfileUiState.Failure(result.error.message)
            }
        }
    }

    companion object {
        /** Type-safe nav stores the destination's `userId` property under this key. */
        const val USER_ID_KEY = "userId"
    }
}
