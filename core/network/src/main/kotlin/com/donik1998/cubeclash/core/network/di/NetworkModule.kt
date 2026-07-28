package com.donik1998.cubeclash.core.network.di

import com.donik1998.cubeclash.core.network.ApiErrorMapper
import com.donik1998.cubeclash.core.network.BuildConfig
import com.donik1998.cubeclash.core.network.CubeClashApi
import com.donik1998.cubeclash.core.network.interceptor.AuthInterceptor
import com.donik1998.cubeclash.core.network.interceptor.TokenRefreshAuthenticator
import com.donik1998.cubeclash.core.domain.repository.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SocketUrl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        // Absent means *inapplicable* for the three long-form solve columns, so a null is
        // never written onto the wire.
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }

    @Provides
    @ApiBaseUrl
    fun apiBaseUrl(): String = BuildConfig.API_BASE_URL

    @Provides
    @SocketUrl
    fun socketUrl(): String = BuildConfig.SOCKET_URL

    @Provides
    @Singleton
    fun authenticator(
        tokenStore: TokenStore,
        json: Json,
        clientProvider: Provider<OkHttpClient>,
        @ApiBaseUrl baseUrl: String,
    ): TokenRefreshAuthenticator = TokenRefreshAuthenticator(tokenStore, json, clientProvider, baseUrl)

    @Provides
    @Singleton
    fun okHttpClient(
        authInterceptor: AuthInterceptor,
        authenticator: TokenRefreshAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(authenticator)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .build()

    @Provides
    @Singleton
    fun retrofit(
        client: OkHttpClient,
        json: Json,
        @ApiBaseUrl baseUrl: String,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun api(retrofit: Retrofit): CubeClashApi = retrofit.create(CubeClashApi::class.java)

    @Provides
    @Singleton
    fun apiErrorMapper(json: Json): ApiErrorMapper = ApiErrorMapper(json)
}
