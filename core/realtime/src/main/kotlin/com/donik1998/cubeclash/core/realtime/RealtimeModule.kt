package com.donik1998.cubeclash.core.realtime

import com.donik1998.cubeclash.core.domain.realtime.RaceGateway
import com.donik1998.cubeclash.core.domain.repository.TokenStore
import com.donik1998.cubeclash.core.domain.scramble.ScrambleGenerator
import com.donik1998.cubeclash.core.network.di.SocketUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealtimeModule {

    /**
     * The one place the demo build and the real build diverge. `cubeclash.useFakeData=false`
     * points the same feature code at the live Socket.IO gateway.
     */
    @Provides
    @Singleton
    fun raceGateway(
        @SocketUrl socketUrl: String,
        tokenStore: TokenStore,
        scrambleGenerator: ScrambleGenerator,
    ): RaceGateway = if (BuildConfig.USE_FAKE_DATA) {
        FakeRaceGateway(scrambleGenerator)
    } else {
        SocketIoRaceGateway(socketUrl, tokenStore)
    }
}
