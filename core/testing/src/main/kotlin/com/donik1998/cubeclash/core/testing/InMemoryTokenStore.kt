package com.donik1998.cubeclash.core.testing

import com.donik1998.cubeclash.core.domain.repository.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * A [TokenStore] that lives entirely in memory.
 *
 * The production store is DataStore-backed and needs an Android [android.content.Context], which
 * a plain JVM unit test does not have. The live-API suite wires the real Retrofit/OkHttp stack —
 * including the [com.donik1998.cubeclash.core.network.interceptor.AuthInterceptor] and
 * [com.donik1998.cubeclash.core.network.interceptor.TokenRefreshAuthenticator], both of which
 * `runBlocking` against this store on every request — so it must behave exactly like the real one,
 * just without persistence.
 */
class InMemoryTokenStore(
    access: String? = null,
    refresh: String? = null,
) : TokenStore {

    private val accessFlow = MutableStateFlow(access)
    private val refreshFlow = MutableStateFlow(refresh)

    override suspend fun accessToken(): String? = accessFlow.value

    override suspend fun refreshToken(): String? = refreshFlow.value

    override suspend fun save(access: String, refresh: String) {
        accessFlow.value = access
        refreshFlow.value = refresh
    }

    override suspend fun clear() {
        accessFlow.value = null
        refreshFlow.value = null
    }

    override val isSignedIn = accessFlow.map { it != null }
}
