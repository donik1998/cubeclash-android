package com.donik1998.cubeclash.core.data.mapper

import com.donik1998.cubeclash.core.network.dto.RacePlayerDto
import com.donik1998.cubeclash.core.network.dto.UserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserMappersTest {

    @Test
    fun `a complete user maps to the domain`() {
        val user = UserDto(
            id = "u-1",
            displayName = "kian_r",
            email = "k@example.com",
            country = "IR",
            elo = 1180,
            solveCount = 42,
        ).toDomain()

        assertEquals("u-1", user.id)
        assertEquals("kian_r", user.displayName)
        assertEquals("k@example.com", user.email)
        assertEquals("IR", user.country)
        assertEquals(1180, user.elo)
        assertEquals(42, user.solveCount)
    }

    @Test
    fun `a user with no elo on the wire maps to 1000, not 1200`() {
        // The DTO default mirrors the backend schema default (users.ts), so a row that omits
        // elo lands on 1000 rather than inventing a rating 200 points too high.
        val user = UserDto(id = "u-1", displayName = "kian_r").toDomain()
        assertEquals(1000, user.elo)
    }

    @Test
    fun `a race player maps to the domain without an avatar`() {
        val player = RacePlayerDto(
            userId = "u-1",
            displayName = "kian_r",
            country = "IR",
            elo = null,
            ready = true,
            timeMs = 6310,
            penalty = "none",
        ).toDomain()

        assertEquals("u-1", player.userId)
        assertEquals("kian_r", player.displayName)
        assertEquals("IR", player.country)
        assertNull(player.elo)
        assertEquals(true, player.isReady)
        assertEquals(6310L, player.finalTimeMs)
    }
}
