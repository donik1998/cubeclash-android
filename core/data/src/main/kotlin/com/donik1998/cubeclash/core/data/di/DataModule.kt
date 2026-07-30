package com.donik1998.cubeclash.core.data.di

import com.donik1998.cubeclash.core.data.BuildConfig
import com.donik1998.cubeclash.core.data.fake.FakeAuthRepository
import com.donik1998.cubeclash.core.data.fake.FakeProfileRepository
import com.donik1998.cubeclash.core.data.fake.FakeRaceRepository
import com.donik1998.cubeclash.core.data.fake.FakeSolveRepository
import com.donik1998.cubeclash.core.data.fake.FakeStatsRepository
import com.donik1998.cubeclash.core.data.repository.AuthRepositoryImpl
import com.donik1998.cubeclash.core.data.repository.LocalScrambleRepository
import com.donik1998.cubeclash.core.data.repository.ProfileRepositoryImpl
import com.donik1998.cubeclash.core.data.repository.RaceRepositoryImpl
import com.donik1998.cubeclash.core.data.repository.SolveRepositoryImpl
import com.donik1998.cubeclash.core.data.repository.StatsRepositoryImpl
import com.donik1998.cubeclash.core.domain.common.CubeClashDispatcher
import com.donik1998.cubeclash.core.domain.common.Dispatcher
import com.donik1998.cubeclash.core.domain.common.TimeSource
import com.donik1998.cubeclash.core.domain.repository.AuthRepository
import com.donik1998.cubeclash.core.domain.repository.ProfileRepository
import com.donik1998.cubeclash.core.domain.repository.RaceRepository
import com.donik1998.cubeclash.core.domain.repository.ScrambleRepository
import com.donik1998.cubeclash.core.domain.repository.SolveRepository
import com.donik1998.cubeclash.core.domain.repository.StatsRepository
import com.donik1998.cubeclash.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * One switch, one place.
 *
 * `cubeclash.useFakeData` decides whether the app talks to `cubeclash-backend` or to the
 * in-memory fakes. Nothing above this module knows which it got — that is the point of the
 * repository interfaces, and it is what let the Flutter client ship a complete, demoable app
 * before the server existed.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun solveRepository(
        real: Provider<SolveRepositoryImpl>,
        fake: Provider<FakeSolveRepository>,
    ): SolveRepository = if (BuildConfig.USE_FAKE_DATA) fake.get() else real.get()

    @Provides
    @Singleton
    fun authRepository(
        real: Provider<AuthRepositoryImpl>,
        fake: Provider<FakeAuthRepository>,
    ): AuthRepository = if (BuildConfig.USE_FAKE_DATA) fake.get() else real.get()

    @Provides
    @Singleton
    fun userRepository(
        real: Provider<AuthRepositoryImpl>,
        fake: Provider<FakeAuthRepository>,
    ): UserRepository = if (BuildConfig.USE_FAKE_DATA) fake.get() else real.get()

    @Provides
    @Singleton
    fun statsRepository(
        real: Provider<StatsRepositoryImpl>,
        fake: Provider<FakeStatsRepository>,
    ): StatsRepository = if (BuildConfig.USE_FAKE_DATA) fake.get() else real.get()

    @Provides
    @Singleton
    fun profileRepository(
        real: Provider<ProfileRepositoryImpl>,
        fake: Provider<FakeProfileRepository>,
    ): ProfileRepository = if (BuildConfig.USE_FAKE_DATA) fake.get() else real.get()

    @Provides
    @Singleton
    fun raceRepository(
        real: Provider<RaceRepositoryImpl>,
        fake: Provider<FakeRaceRepository>,
    ): RaceRepository = if (BuildConfig.USE_FAKE_DATA) fake.get() else real.get()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ScrambleModule {
    /** Local either way — the generator does not need a server. */
    @Binds
    @Singleton
    abstract fun scrambleRepository(impl: LocalScrambleRepository): ScrambleRepository
}

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @Dispatcher(CubeClashDispatcher.IO)
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(CubeClashDispatcher.Default)
    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    fun timeSource(): TimeSource = TimeSource { System.currentTimeMillis() }
}
