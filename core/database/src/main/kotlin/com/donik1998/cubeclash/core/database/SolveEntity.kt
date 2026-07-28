package com.donik1998.cubeclash.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The local mirror of a solve.
 *
 * Keyed on `client_id`, not on the server id: a solve exists on the device before the server
 * has ever heard of it, and the client id is what makes the eventual upsert idempotent.
 * `event` is a plain string for the same reason it is on the server — an eighteenth event
 * should be a row, not a migration.
 */
@Entity(
    tableName = "solves",
    indices = [Index(value = ["event", "solved_at"]), Index(value = ["sync_state"])],
)
data class SolveEntity(
    @PrimaryKey
    @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "server_id") val serverId: String? = null,
    val event: String,
    /** Newline-separated, exactly as it goes on the wire. */
    val scramble: String,
    @ColumnInfo(name = "scramble_source") val scrambleSource: String,
    @ColumnInfo(name = "time_ms") val timeMs: Long,
    val penalty: String,
    @ColumnInfo(name = "solved_at") val solvedAt: Long,
    @ColumnInfo(name = "move_count") val moveCount: Int? = null,
    @ColumnInfo(name = "solved_count") val solvedCount: Int? = null,
    @ColumnInfo(name = "attempted_count") val attemptedCount: Int? = null,
    @ColumnInfo(name = "is_pb") val isPb: Boolean = false,
    @ColumnInfo(name = "sync_state") val syncState: String = "PENDING",
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)
