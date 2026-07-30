package com.donik1998.cubeclash.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.ProfileRepository
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.PlayerProfile
import com.donik1998.cubeclash.core.model.WcaEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The You · Profile aggregate's loading/success/failure lifecycle. [Success] carries the domain
 * [PlayerProfile] directly — no DTO ever reaches the UI — and [Failure] carries a renderable
 * message plus a retry affordance.
 */
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profile: PlayerProfile) : ProfileUiState
    data class Failure(val message: String?) : ProfileUiState
}

/**
 * Owns only the profile aggregate. Settings (theme/timer/inspection/haptics/sign-out) moved to
 * [SettingsViewModel] so the Settings screen can drive them independently (spec §11.7).
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            _uiState.value = when (
                val result = profileRepository.profile(WcaEvent.THREE, LeaderboardScope.GLOBAL)
            ) {
                is DataResult.Success -> ProfileUiState.Success(result.data)
                is DataResult.Failure -> ProfileUiState.Failure(result.error.message)
            }
        }
    }
}
