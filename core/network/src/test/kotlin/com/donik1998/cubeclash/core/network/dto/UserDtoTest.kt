package com.donik1998.cubeclash.core.network.dto

import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDtoTest {

    // The same configuration NetworkModule provides in production.
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }

    @Test
    fun `full user round-trips including snake_case`() {
        val wire = """
            {
              "id": "0e5b-uuid",
              "display_name": "cuber_98",
              "email": "c@example.com",
              "country": "UZ",
              "elo": 1180,
              "solve_count": 42
            }
        """.trimIndent()

        val dto = json.decodeFromString<UserDto>(wire)
        assertEquals("0e5b-uuid", dto.id)
        assertEquals("cuber_98", dto.displayName)
        assertEquals("c@example.com", dto.email)
        assertEquals("UZ", dto.country)
        assertEquals(1180, dto.elo)
        assertEquals(42, dto.solveCount)
    }

    @Test
    fun `missing elo defaults to 1000, matching the backend schema default`() {
        val wire = """{"id":"u-1","display_name":"kian_r"}"""
        val dto = json.decodeFromString<UserDto>(wire)
        assertEquals(1000, dto.elo)
        assertEquals(0, dto.solveCount)
        assertNull(dto.email)
        assertNull(dto.country)
    }

    @Test
    fun `a stray avatar_url on the wire is ignored, not carried`() {
        // There is no avatar column and no avatar field; an unknown key must not throw.
        val wire = """{"id":"u-1","display_name":"kian_r","avatar_url":"https://x/y.png"}"""
        val dto = json.decodeFromString<UserDto>(wire)
        val fields = UserDto.serializer().descriptor.elementNames.toList()
        assertTrue("UserDto must not declare an avatar field", "avatarUrl" !in fields)
        assertEquals("u-1", dto.id)
    }

    @Test
    fun `race player has no avatar field and elo stays nullable`() {
        val wire = """{"user_id":"u-1","display_name":"kian_r","avatar_url":"https://x/y.png"}"""
        val dto = json.decodeFromString<RacePlayerDto>(wire)
        val fields = RacePlayerDto.serializer().descriptor.elementNames.toList()
        assertTrue("RacePlayerDto must not declare an avatar field", "avatarUrl" !in fields)
        assertEquals("u-1", dto.userId)
        assertNull(dto.elo)
    }
}
