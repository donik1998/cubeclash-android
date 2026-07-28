package com.donik1998.cubeclash.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): CubeClashDatabase =
        Room.databaseBuilder(context, CubeClashDatabase::class.java, CubeClashDatabase.NAME).build()

    @Provides
    fun solveDao(database: CubeClashDatabase): SolveDao = database.solveDao()
}
