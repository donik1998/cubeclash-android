package com.donik1998.cubeclash.core.network.interceptor

import com.donik1998.cubeclash.core.domain.repository.TokenStore
import com.donik1998.cubeclash.core.network.dto.AuthResponse
import com.donik1998.cubeclash.core.network.dto.RefreshRequest
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

/**
 * Transparent refresh on a 401.
 *
 * OkHttp's [Authenticator] is the right hook rather than an interceptor: it fires *after* a
 * 401, it serialises concurrent retries, and [Response.priorResponse] gives a natural way to
 * stop after one attempt instead of looping a dead refresh token forever.
 */
@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val json: Json,
    private val clientProvider: Provider<OkHttpClient>,
    private val baseUrl: String,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // One retry only. A second 401 means the refresh token is gone too.
        if (response.priorResponse != null) return null

        val refresh = runBlocking { tokenStore.refreshToken() } ?: return null

        val refreshed = runCatching {
            val body = json.encodeToString(RefreshRequest(refresh))
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${baseUrl.trimEnd('/')}/auth/refresh")
                .post(body)
                .build()

            clientProvider.get().newCall(request).execute().use { refreshResponse ->
                if (!refreshResponse.isSuccessful) return@runCatching null
                refreshResponse.body?.string()?.let { json.decodeFromString<AuthResponse>(it) }
            }
        }.getOrNull() ?: run {
            runBlocking { tokenStore.clear() }
            return null
        }

        runBlocking { tokenStore.save(refreshed.tokens.access, refreshed.tokens.refresh) }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshed.tokens.access}")
            .build()
    }
}
