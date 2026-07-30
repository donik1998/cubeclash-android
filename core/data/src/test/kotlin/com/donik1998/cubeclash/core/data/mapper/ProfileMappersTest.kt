package com.donik1998.cubeclash.core.data.mapper

import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.dto.ProfileRankDto
import com.donik1998.cubeclash.core.network.dto.ProfileResponseDto
import com.donik1998.cubeclash.core.network.dto.ProfileStatsDto
import com.donik1998.cubeclash.core.network.dto.ProfileUserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileMappersTest {

    private val event = WcaEvent.THREE
    private val scope = LeaderboardScope.GLOBAL

    private fun user(
        id: String? = "u-1",
        displayName: String? = "cuber_98",
        country: String? = "UZ",
        elo: Int? = 1180,
    ) = ProfileUserDto(id, displayName, country, elo)

    private fun rank(
        eventId: String? = "3x3",
        metric: String? = "single",
        scopeWire: String? = "global",
        position: Int? = 1204,
    ) = ProfileRankDto(eventId, metric, scopeWire, position)

    private fun stats(
        bestSingleMs: Long? = 8420,
        bestSingleEvent: String? = "3x3",
        totalSolves: Int? = 3204,
        winRate: Double? = 0.68,
        wins: Int? = 217,
        losses: Int? = 102,
    ) = ProfileStatsDto(bestSingleMs, bestSingleEvent, totalSolves, winRate, wins, losses)

    private fun response(
        user: ProfileUserDto? = user(),
        rank: ProfileRankDto? = rank(),
        stats: ProfileStatsDto? = stats(),
        friendCount: Int? = 48,
    ) = ProfileResponseDto(user, rank, stats, friendCount)

    @Test
    fun `a complete payload maps to a domain profile`() {
        val profile = response().toDomain(event, scope)!!
        assertEquals("u-1", profile.id)
        assertEquals("cuber_98", profile.displayName)
        assertEquals("UZ", profile.country)
        assertEquals(1180, profile.elo)
        assertEquals(1204, profile.rank?.position)
        assertEquals(WcaEvent.THREE, profile.rank?.event)
        assertEquals("single", profile.rank?.metric)
        assertEquals(LeaderboardScope.GLOBAL, profile.rank?.scope)
        assertEquals(8420L, profile.bestSingleMs)
        assertEquals(WcaEvent.THREE, profile.bestSingleEvent)
        assertEquals(3204, profile.totalSolves)
        assertEquals(0.68, profile.winRate!!, 0.0001)
        assertEquals(217, profile.wins)
        assertEquals(102, profile.losses)
        assertEquals(48, profile.friendCount)
    }

    @Test
    fun `null user drops the whole profile`() {
        assertNull(response(user = null).toDomain(event, scope))
    }

    @Test
    fun `null or blank id drops the whole profile`() {
        assertNull(response(user = user(id = null)).toDomain(event, scope))
        assertNull(response(user = user(id = "  ")).toDomain(event, scope))
    }

    @Test
    fun `null or blank display_name drops the whole profile`() {
        assertNull(response(user = user(displayName = null)).toDomain(event, scope))
        assertNull(response(user = user(displayName = "")).toDomain(event, scope))
    }

    @Test
    fun `null country stays null in the model`() {
        val profile = response(user = user(country = null)).toDomain(event, scope)!!
        assertNull(profile.country)
    }

    @Test
    fun `null elo defaults to 1000`() {
        val profile = response(user = user(elo = null)).toDomain(event, scope)!!
        assertEquals(1000, profile.elo)
    }

    @Test
    fun `null rank object yields a null rank`() {
        val profile = response(rank = null).toDomain(event, scope)!!
        assertNull(profile.rank)
    }

    @Test
    fun `rank present but position null yields a null rank`() {
        val profile = response(rank = rank(position = null)).toDomain(event, scope)!!
        assertNull(profile.rank)
    }

    @Test
    fun `null rank event, metric and scope default to the request`() {
        val profile = response(
            rank = rank(eventId = null, metric = null, scopeWire = null),
        ).toDomain(WcaEvent.FOUR, LeaderboardScope.FRIENDS)!!
        assertEquals(WcaEvent.FOUR, profile.rank?.event)
        assertEquals("single", profile.rank?.metric)
        assertEquals(LeaderboardScope.FRIENDS, profile.rank?.scope)
    }

    @Test
    fun `null best_single_ms stays null`() {
        val profile = response(stats = stats(bestSingleMs = null)).toDomain(event, scope)!!
        assertNull(profile.bestSingleMs)
    }

    @Test
    fun `null best_single_event defaults to the requested event`() {
        val profile = response(stats = stats(bestSingleEvent = null)).toDomain(WcaEvent.FOUR, scope)!!
        assertEquals(WcaEvent.FOUR, profile.bestSingleEvent)
    }

    @Test
    fun `null win_rate stays null`() {
        val profile = response(stats = stats(winRate = null)).toDomain(event, scope)!!
        assertNull(profile.winRate)
    }

    @Test
    fun `null counts default to zero`() {
        val profile = response(
            stats = stats(totalSolves = null, wins = null, losses = null),
            friendCount = null,
        ).toDomain(event, scope)!!
        assertEquals(0, profile.totalSolves)
        assertEquals(0, profile.wins)
        assertEquals(0, profile.losses)
        assertEquals(0, profile.friendCount)
    }

    @Test
    fun `null stats object leaves derived fields at their neutral defaults`() {
        val profile = response(stats = null).toDomain(event, scope)!!
        assertNull(profile.bestSingleMs)
        assertNull(profile.winRate)
        assertEquals(WcaEvent.THREE, profile.bestSingleEvent)
        assertEquals(0, profile.totalSolves)
        assertEquals(0, profile.wins)
        assertEquals(0, profile.losses)
    }
}
