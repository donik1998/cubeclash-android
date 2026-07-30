package com.donik1998.cubeclash.core.network.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PublicUserDtoTest {

    // The same configuration NetworkModule provides in production.
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }

    @Test
    fun `full public profile round-trips including snake_case`() {
        // The exact bytes GET /users/:id returned from the live server, plus a present head_to_head.
        val wire = """
            {
              "user": {
                "id": "4ee0009d-34b6-41e6-a5f2-ad215e35df87",
                "display_name": "Probe",
                "country": "UZ",
                "elo": 1000,
                "best_single_ms": 7137,
                "ao5": 7411,
                "ao12": 7800,
                "head_to_head": {"wins": 3, "losses": 1}
              }
            }
        """.trimIndent()

        val dto = json.decodeFromString<PublicUserResponseDto>(wire)
        assertEquals("4ee0009d-34b6-41e6-a5f2-ad215e35df87", dto.user?.id)
        assertEquals("Probe", dto.user?.displayName)
        assertEquals("UZ", dto.user?.country)
        assertEquals(1000, dto.user?.elo)
        assertEquals(7137L, dto.user?.bestSingleMs)
        assertEquals(7411L, dto.user?.ao5)
        assertEquals(7800L, dto.user?.ao12)
        assertEquals(3, dto.user?.headToHead?.wins)
        assertEquals(1, dto.user?.headToHead?.losses)

        val reencoded = json.decodeFromString<PublicUserResponseDto>(json.encodeToString(dto))
        assertEquals(dto, reencoded)
    }

    @Test
    fun `the verified fresh-account shape decodes with nulls where the server sends them`() {
        // Byte-for-byte the live response for a seeded user: country, ao12 and head_to_head null.
        val wire = """
            {"user":{"id":"4ee0009d-34b6-41e6-a5f2-ad215e35df87","display_name":"Probe","country":null,"elo":1000,"best_single_ms":7137,"ao5":7411,"ao12":null,"head_to_head":null}}
        """.trimIndent()

        val dto = json.decodeFromString<PublicUserResponseDto>(wire)
        assertEquals("Probe", dto.user?.displayName)
        assertNull(dto.user?.country)
        assertEquals(7137L, dto.user?.bestSingleMs)
        assertEquals(7411L, dto.user?.ao5)
        assertNull(dto.user?.ao12)
        assertNull(dto.user?.headToHead)
    }

    @Test
    fun `empty object decodes without throwing and the user is null`() {
        // The house-rule empty-object test: the wrapper absorbs a totally empty payload.
        val dto = json.decodeFromString<PublicUserResponseDto>("{}")
        assertNull(dto.user)
    }

    @Test
    fun `head_to_head zero-zero is preserved distinct from an absent object`() {
        val present = json.decodeFromString<PublicUserResponseDto>(
            """{"user":{"id":"u-1","display_name":"P","head_to_head":{"wins":0,"losses":0}}}""",
        )
        val absent = json.decodeFromString<PublicUserResponseDto>(
            """{"user":{"id":"u-1","display_name":"P","head_to_head":null}}""",
        )
        assertEquals(0, present.user?.headToHead?.wins)
        assertEquals(0, present.user?.headToHead?.losses)
        assertNull(absent.user?.headToHead)
    }
}
