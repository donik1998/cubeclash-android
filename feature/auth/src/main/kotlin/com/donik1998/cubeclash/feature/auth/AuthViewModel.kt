package com.donik1998.cubeclash.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.usecase.SignInUseCase
import com.donik1998.cubeclash.core.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode(val label: String) { SIGN_IN("Log in"), SIGN_UP("Sign up") }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean
        get() = email.contains("@") && password.length >= 8 &&
            (mode == AuthMode.SIGN_IN || displayName.isNotBlank())
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signIn: SignInUseCase,
    private val signUp: SignUpUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setMode(mode: AuthMode) = _uiState.update { it.copy(mode = mode, error = null) }
    fun setEmail(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun setPassword(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun setDisplayName(value: String) = _uiState.update { it.copy(displayName = value, error = null) }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit || state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = when (state.mode) {
                AuthMode.SIGN_IN -> signIn(state.email, state.password)
                AuthMode.SIGN_UP -> signUp(state.email, state.password, state.displayName)
            }
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    error = (result as? DataResult.Failure)?.error?.message,
                )
            }
        }
    }
}
