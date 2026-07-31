package com.donik1998.cubeclash.core.data.repository

import com.donik1998.cubeclash.core.data.mapper.toDomain
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.AuthRepository
import com.donik1998.cubeclash.core.domain.repository.TokenStore
import com.donik1998.cubeclash.core.domain.repository.UserRepository
import com.donik1998.cubeclash.core.model.AuthState
import com.donik1998.cubeclash.core.model.PublicProfile
import com.donik1998.cubeclash.core.model.User
import com.donik1998.cubeclash.core.network.ApiErrorMapper
import com.donik1998.cubeclash.core.network.CubeClashApi
import com.donik1998.cubeclash.core.network.call
import com.donik1998.cubeclash.core.network.dto.LoginRequest
import com.donik1998.cubeclash.core.network.dto.RefreshRequest
import com.donik1998.cubeclash.core.network.dto.RegisterRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: CubeClashApi,
    private val tokenStore: TokenStore,
    private val errors: ApiErrorMapper,
) : AuthRepository, UserRepository {

    private val cachedUser = MutableStateFlow<User?>(null)

    /**
     * Signed-in-ness is derived from the token store, not from a boolean this class keeps:
     * the authenticator can clear the tokens from underneath a request, and the UI has to
     * notice that without being told twice.
     */
    override val authState: Flow<AuthState> =
        combine(tokenStore.isSignedIn, cachedUser) { signedIn, user ->
            when {
                !signedIn -> AuthState.SignedOut
                user != null -> AuthState.SignedIn(user)
                else -> AuthState.Unknown
            }
        }

    override fun observeMe(): Flow<User?> = cachedUser.map { it }

    override suspend fun signIn(email: String, password: String): DataResult<User> =
        errors.call { api.login(LoginRequest(email, password)) }.let { result ->
            when (result) {
                is DataResult.Success -> {
                    tokenStore.save(result.data.tokens.access, result.data.tokens.refresh)
                    refreshMe()
                }

                is DataResult.Failure -> result
            }
        }

    override suspend fun signUp(email: String, password: String, displayName: String): DataResult<User> =
        errors.call { api.register(RegisterRequest(email, password, displayName)) }.let { result ->
            when (result) {
                is DataResult.Success -> {
                    tokenStore.save(result.data.tokens.access, result.data.tokens.refresh)
                    refreshMe()
                }

                is DataResult.Failure -> result
            }
        }

    override suspend fun signOut(): DataResult<Unit> {
        // Revocation is by refresh token: the server 400s a body-less logout and never revokes,
        // so the token must be read out and sent *before* the local clear.
        val refresh = tokenStore.refreshToken()
        val result = refresh?.let { token -> errors.call { api.logout(RefreshRequest(token)) } }
            ?: DataResult.Success(Unit)
        tokenStore.clear()
        cachedUser.value = null
        // Local sign-out happens regardless: a failed logout call must not trap someone in a
        // session they have already left.
        return if (result is DataResult.Failure) DataResult.Success(Unit) else result
    }

    override suspend fun refreshMe(): DataResult<User> =
        errors.call {
            api.me().user?.toDomain()
                ?: throw IllegalStateException("GET /me response had no user object.")
        }.let { result ->
            if (result is DataResult.Success) cachedUser.value = result.data
            result
        }

    override suspend fun updateProfile(displayName: String?, country: String?): DataResult<User> =
        errors.call {
            api.updateMe(
                buildMap {
                    displayName?.let { put("display_name", it) }
                    country?.let { put("country", it) }
                },
            ).user?.toDomain()
                ?: throw IllegalStateException("PATCH /me response had no user object.")
        }.let { result ->
            if (result is DataResult.Success) cachedUser.value = result.data
            result
        }

    override suspend fun publicProfile(id: String): DataResult<PublicProfile> =
        errors.call {
            api.publicProfile(id).toDomain()
                ?: throw IllegalStateException("Public profile response had no usable user.")
        }
}
