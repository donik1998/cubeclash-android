package com.donik1998.cubeclash.core.data.di

import com.donik1998.cubeclash.core.data.BuildConfig
import com.donik1998.cubeclash.core.data.fake.FakeFriendsRepository
import com.donik1998.cubeclash.core.data.fake.FakeTournamentRepository
import com.donik1998.cubeclash.core.data.repository.FriendsRepositoryImpl
import com.donik1998.cubeclash.core.data.repository.TournamentRepositoryImpl
import com.donik1998.cubeclash.core.domain.repository.FriendsRepository
import com.donik1998.cubeclash.core.domain.repository.TournamentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Fake/real switch for the social & tournament repositories, kept in its own module.
 *
 * Same `cubeclash.useFakeData` flag as [RepositoryModule] — with the default `true`, the fakes run
 * and the app is fully demoable offline, which is exactly what these two need since the underlying
 * `friends`/`tournaments` endpoints do not exist on the backend yet (they 404). The real impls are
 * wired and ready for the day the routes ship.
 */
@Module
@InstallIn(SingletonComponent::class)
object SocialRepositoryModule {

    @Provides
    @Singleton
    fun friendsRepository(
        real: Provider<FriendsRepositoryImpl>,
        fake: Provider<FakeFriendsRepository>,
    ): FriendsRepository = if (BuildConfig.USE_FAKE_DATA) fake.get() else real.get()

    @Provides
    @Singleton
    fun tournamentRepository(
        real: Provider<TournamentRepositoryImpl>,
        fake: Provider<FakeTournamentRepository>,
    ): TournamentRepository = if (BuildConfig.USE_FAKE_DATA) fake.get() else real.get()
}
