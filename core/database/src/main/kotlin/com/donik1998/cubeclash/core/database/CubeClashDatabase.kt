package com.donik1998.cubeclash.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SolveEntity::class], version = 1, exportSchema = true)
abstract class CubeClashDatabase : RoomDatabase() {
    abstract fun solveDao(): SolveDao

    companion object {
        const val NAME = "cubeclash.db"
    }
}
