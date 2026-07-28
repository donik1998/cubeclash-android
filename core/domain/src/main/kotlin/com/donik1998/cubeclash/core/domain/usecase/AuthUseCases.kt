package com.donik1998.cubeclash.core.domain.usecase

import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.AuthRepository
import com.donik1998.cubeclash.core.model.AuthState
import com.donik1998.cubeclash.core.model.User
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthState> = authRepository.authState
}

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): DataResult<User> =
        authRepository.signIn(email.trim(), password)
}

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, displayName: String): DataResult<User> =
        authRepository.signUp(email.trim(), password, displayName.trim())
}

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): DataResult<Unit> = authRepository.signOut()
}
