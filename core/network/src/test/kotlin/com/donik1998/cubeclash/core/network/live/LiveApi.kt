package com.donik1998.cubeclash.core.network.live

import com.donik1998.cubeclash.core.network.CubeClashApi
import com.donik1998.cubeclash.core.network.dto.RegisterRequest
import com.donik1998.cubeclash.core.network.interceptor.AuthInterceptor
import com.donik1998.cubeclash.core.network.interceptor.TokenRefreshAuthenticator
import com.donik1998.cubeclash.core.testing.InMemoryTokenStore
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Shared plumbing for the live-API suite: gate, base URL, and a factory that builds the **real**
 * Retrofit/OkHttp stack exactly as `NetworkModule` does in production — same [Json] config, same
 * [AuthInterceptor], same [TokenRefreshAuthenticator]. MockWebServer is deliberately not used; the
 * whole point is real sockets to the running backend.
 */
object LiveApi {

    /** Gate: `-Dcubeclash.liveApi=true` or `LIVE_API=true`. Absent → the suites Assume-skip. */
    val enabled: Boolean =
        System.getProperty("cubeclash.liveApi")?.toBoolean() == true ||
            System.getenv("LIVE_API")?.toBoolean() == true

    /** Overridable base, `-Dcubeclash.apiBaseUrl=… ` or `API_BASE_URL`. Must include the `/v1` prefix. */
    val baseUrl: String =
        System.getProperty("cubeclash.apiBaseUrl")
            ?: System.getenv("API_BASE_URL")
            ?: "http://localhost:3100/v1/"

    /** The exact [Json] config `NetworkModule.json()` provides. */
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }

    /**
     * A fresh, fully-wired stack. The [InMemoryTokenStore] stands in for the DataStore-backed
     * production store (which needs an Android Context); every other layer is the production one.
     */
    fun newStack(tokens: InMemoryTokenStore = InMemoryTokenStore()): Stack {
        val json = json()
        val base = baseUrl
        lateinit var client: OkHttpClient
        val authenticator = TokenRefreshAuthenticator(tokens, json, { client }, base)
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokens))
            // Test-only: the backend rate-limits, and running the whole suite trips it. A 429 is an
            // environment condition, not a client bug, so back off and retry rather than fail. This
            // interceptor exists ONLY on the test stack; production has no such retry.
            .addInterceptor(RetryOn429())
            .authenticator(authenticator)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(base)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return Stack(tokens, json, client, retrofit.create(CubeClashApi::class.java))
    }

    class Stack(
        val tokens: InMemoryTokenStore,
        val json: Json,
        val client: OkHttpClient,
        val api: CubeClashApi,
    )

    fun uniqueEmail(): String = "android_live_${System.nanoTime()}@ex.com"

    /**
     * Registers a throwaway user and seeds the token store, exactly as the real sign-up path does.
     * Backoff on the backend's rate limiter is handled transparently by [RetryOn429] on the stack.
     */
    suspend fun registerFresh(stack: Stack, displayName: String = "android_live"): String {
        val email = uniqueEmail()
        val res = stack.api.register(RegisterRequest(email, PASSWORD, displayName))
        stack.tokens.save(res.tokens.access, res.tokens.refresh)
        return email
    }

    const val PASSWORD = "hunter2hunter2"

    /**
     * A bare OkHttp client with no [AuthInterceptor] or [TokenRefreshAuthenticator] — the wire-shape
     * tests attach the Bearer by hand so they see exactly what the server sends, with no transparent
     * refresh in the way. It DOES carry [RetryOn429] so the backend's rate limiter cannot flake the
     * suite.
     */
    fun rawClient(): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(RetryOn429()).build()
}

/**
 * **Test-only.** The backend rate-limits `register` at 10/min/IP, and this suite registers a
 * throwaway user per test — so running it end to end trips the limiter by design. A 429 here is an
 * environment condition, not a client bug, so back off and retry. Production has no such
 * interceptor: a real 429 there surfaces as
 * [com.donik1998.cubeclash.core.domain.common.AppError.RateLimited] for the UI to handle.
 *
 * The server sends `Retry-After` with the window's exact remaining TTL, so the common path is a
 * single accurate sleep. The fallback matters anyway, and an earlier version got it wrong: it
 * backed off linearly 1s…6s, totalling 21s against a **60-second** window, so the suite failed two
 * tests with `HTTP 429` roughly every other run. The fallback now assumes a full window rather
 * than guessing short — waiting too long merely makes an opt-in suite slower, while waiting too
 * little makes it flaky, and a flaky suite is one nobody trusts.
 */
private class RetryOn429 : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())
        var attempt = 0
        while (response.code == 429 && attempt < MAX_ATTEMPTS) {
            val waitSeconds = response.header("Retry-After")?.toLongOrNull() ?: FALLBACK_WINDOW_SECONDS
            response.close()
            // +1s of slack: Retry-After is whole seconds, so the window can still have a
            // fractional remainder when it says 0.
            Thread.sleep((waitSeconds + 1).coerceIn(1, MAX_WAIT_SECONDS) * 1000)
            attempt++
            response = chain.proceed(chain.request())
        }
        return response
    }

    private companion object {
        const val MAX_ATTEMPTS = 4

        /** The `register` policy's window. Used only when the server omits `Retry-After`. */
        const val FALLBACK_WINDOW_SECONDS = 60L

        /** Never sleep longer than this on one attempt, however odd a header the server sends. */
        const val MAX_WAIT_SECONDS = 90L
    }
}
