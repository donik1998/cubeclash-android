package com.donik1998.cubeclash.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.repository.SettingsRepository
import com.donik1998.cubeclash.core.domain.repository.UserRepository
import com.donik1998.cubeclash.core.domain.usecase.SignOutUseCase
import com.donik1998.cubeclash.core.model.AppSettings
import com.donik1998.cubeclash.core.model.ThemeMode
import com.donik1998.cubeclash.core.model.TimerStyle
import com.donik1998.cubeclash.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val settings: AppSettings = AppSettings(),
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.observeMe().collect { user -> _uiState.update { it.copy(user = user) } }
        }
        viewModelScope.launch {
            settingsRepository.settings.collect { settings -> _uiState.update { it.copy(settings = settings) } }
        }
        viewModelScope.launch { userRepository.refreshMe() }
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }

    fun setTimerStyle(style: TimerStyle) = viewModelScope.launch { settingsRepository.setTimerStyle(style) }

    fun setInspection(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setInspectionEnabled(enabled) }

    fun setHaptics(enabled: Boolean) = viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }

    fun signOut() = viewModelScope.launch { signOutUseCase() }
}
