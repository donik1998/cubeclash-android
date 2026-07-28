package com.donik1998.cubeclash.core.network.interceptor

import com.donik1998.cubeclash.core.domain.repository.TokenStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches the access token. Auth endpoints are skipped explicitly rather than by trying to
 * guess from the response — sending a stale bearer to `/auth/refresh` is how refresh loops start.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.encodedPath.contains("/auth/")) return chain.proceed(request)

        val token = runBlocking { tokenStore.accessToken() } ?: return chain.proceed(request)

        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build(),
        )
    }
}
