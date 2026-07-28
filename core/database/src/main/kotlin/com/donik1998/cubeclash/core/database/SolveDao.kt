package com.donik1998.cubeclash.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SolveDao {

    @Query(
        """
        SELECT * FROM solves
        WHERE event = :event AND deleted_at IS NULL
        ORDER BY solved_at DESC
        """,
    )
    fun observeByEvent(event: String): Flow<List<SolveEntity>>

    @Query(
        """
        SELECT * FROM solves
        WHERE event = :event AND deleted_at IS NULL
        ORDER BY solved_at DESC
        LIMIT :limit
        """,
    )
    fun observeSession(event: String, limit: Int = 100): Flow<List<SolveEntity>>

    @Query("SELECT * FROM solves WHERE sync_state != 'SYNCED'")
    suspend fun pending(): List<SolveEntity>

    @Upsert
    suspend fun upsert(solve: SolveEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(solves: List<SolveEntity>)

    @Query("UPDATE solves SET penalty = :penalty, sync_state = 'PENDING' WHERE client_id = :clientId")
    suspend fun updatePenalty(clientId: String, penalty: String)

    /** Soft delete, so the deletion itself can be synced rather than silently vanishing. */
    @Query("UPDATE solves SET deleted_at = :deletedAt, sync_state = 'PENDING' WHERE client_id = :clientId")
    suspend fun softDelete(clientId: String, deletedAt: Long)

    @Query("SELECT * FROM solves WHERE client_id = :clientId LIMIT 1")
    suspend fun byClientId(clientId: String): SolveEntity?

    @Query("DELETE FROM solves WHERE event = :event")
    suspend fun clearEvent(event: String)
}
