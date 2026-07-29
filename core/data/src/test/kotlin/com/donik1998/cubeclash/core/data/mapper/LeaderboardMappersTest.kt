package com.donik1998.cubeclash.core.data.mapper

import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.dto.LeaderboardItemDto
import com.donik1998.cubeclash.core.network.dto.LeaderboardResponseDto
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LeaderboardMappersTest {

    private val event = WcaEvent.THREE

    private fun validItem(
        rank: Int? = 1,
        userId: String? = "u-1",
        displayName: String? = "kian_r",
        country: String? = "IR",
        valueMs: Long? = 6310,
        eventId: String? = "3x3",
        solvedAt: String? = "2026-07-20T09:12:03.000Z",
    ) = LeaderboardItemDto(rank, userId, displayName, country, valueMs, eventId, solvedAt)

    @Test
    fun `a complete row maps to a domain entry`() {
        val entry = validItem().toDomain(event)!!
        assertEquals(1, entry.rank)
        assertEquals("u-1", entry.userId)
        assertEquals("kian_r", entry.displayName)
        assertEquals("IR", entry.country)
        assertEquals(6310L, entry.valueMs)
        assertEquals(WcaEvent.THREE, entry.event)
        assertEquals(Instant.parse("2026-07-20T09:12:03.000Z"), entry.solvedAt)
    }

    @Test
    fun `missing rank drops the row`() {
        assertNull(validItem(rank = null).toDomain(event))
    }

    @Test
    fun `missing user_id drops the row`() {
        assertNull(validItem(userId = null).toDomain(event))
        assertNull(validItem(userId = "  ").toDomain(event))
    }

    @Test
    fun `missing value_ms drops the row`() {
        assertNull(validItem(valueMs = null).toDomain(event))
    }

    @Test
    fun `missing display_name drops the row`() {
        assertNull(validItem(displayName = null).toDomain(event))
        assertNull(validItem(displayName = "").toDomain(event))
    }

    @Test
    fun `missing country stays null in the model`() {
        val entry = validItem(country = null).toDomain(event)!!
        assertNull(entry.country)
    }

    @Test
    fun `missing event defaults to the requested event`() {
        val entry = validItem(eventId = null).toDomain(WcaEvent.FOUR)!!
        assertEquals(WcaEvent.FOUR, entry.event)
    }

    @Test
    fun `unparseable solved_at falls back to EPOCH rather than dropping the row`() {
        val entry = validItem(solvedAt = "not-a-date").toDomain(event)!!
        assertEquals(Instant.EPOCH, entry.solvedAt)
    }

    @Test
    fun `one bad element does not blank the list`() {
        val response = LeaderboardResponseDto(
            items = listOf(
                validItem(rank = 1, userId = "a", displayName = "a"),
                LeaderboardItemDto(), // all-null: dropped
                null, // null element: dropped
                validItem(rank = 2, userId = "b", displayName = "b"),
            ),
            nextCursor = "c2",
            viewer = null,
        )
        val page = response.toDomain(event)
        assertEquals(2, page.entries.size)
        assertEquals(listOf("a", "b"), page.entries.map { it.userId })
        assertEquals("c2", page.nextCursor)
    }

    @Test
    fun `null items list yields an empty page, not a crash`() {
        val page = LeaderboardResponseDto().toDomain(event)
        assertEquals(0, page.entries.size)
        assertNull(page.nextCursor)
        assertNull(page.viewer)
    }

    @Test
    fun `viewer that fails the drop rules becomes a null viewer`() {
        val page = LeaderboardResponseDto(
            items = emptyList(),
            viewer = validItem(valueMs = null),
        ).toDomain(event)
        assertNull(page.viewer)
    }

    @Test
    fun `a valid viewer maps through`() {
        val page = LeaderboardResponseDto(
            items = emptyList(),
            viewer = validItem(rank = 1204, userId = "me", displayName = "me"),
        ).toDomain(event)
        assertEquals(1204, page.viewer?.rank)
        assertEquals("me", page.viewer?.userId)
    }
}
