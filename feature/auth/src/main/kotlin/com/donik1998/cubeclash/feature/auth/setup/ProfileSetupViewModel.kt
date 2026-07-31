package com.donik1998.cubeclash.feature.auth.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The last step of registration: display name and (optional) country, submitted through
 * [UserRepository.updateProfile]. Country is genuinely optional — a cuber with no country is
 * valid everywhere else — so [ProfileSetupUiState.canSubmit] never depends on it.
 */
data class ProfileSetupUiState(
    val displayName: String = "",
    val country: String? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val isDone: Boolean = false,
) {
    /** A blank name is the only thing that blocks submit; whitespace-only counts as blank. */
    val canSubmit: Boolean
        get() = displayName.isNotBlank() && !isSubmitting
}

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        // Pre-fill the name the user just registered with, so the common case is one tap.
        viewModelScope.launch {
            val me = userRepository.observeMe().first()
            val name = me?.displayName.orEmpty()
            if (name.isNotBlank()) {
                _uiState.update { if (it.displayName.isBlank()) it.copy(displayName = name) else it }
            }
        }
    }

    fun setDisplayName(value: String) = _uiState.update { it.copy(displayName = value, error = null) }

    /** Tapping the selected country clears it — the field is optional, so it must be un-settable. */
    fun toggleCountry(code: String) = _uiState.update {
        it.copy(country = if (it.country == code) null else code, error = null)
    }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val result = userRepository.updateProfile(
                displayName = state.displayName.trim(),
                country = state.country,
            )
            _uiState.update {
                when (result) {
                    is DataResult.Success -> it.copy(isSubmitting = false, isDone = true)
                    // A failed update must NOT advance into the shell as if it worked; surface it
                    // inline with the button re-enabled so the retry is the same tap.
                    is DataResult.Failure -> it.copy(isSubmitting = false, error = result.error.message)
                }
            }
        }
    }

    /** Skip advances without touching the network — no [UserRepository.updateProfile] call. */
    fun skip() = _uiState.update { it.copy(isDone = true) }
}
