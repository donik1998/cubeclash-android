package com.donik1998.cubeclash.core.data.demo

import com.donik1998.cubeclash.core.domain.scramble.ScrambleGenerator
import com.donik1998.cubeclash.core.model.LeaderboardEntry
import com.donik1998.cubeclash.core.model.LeaderboardMetric
import com.donik1998.cubeclash.core.model.LeaderboardPage
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.HeadToHead
import com.donik1998.cubeclash.core.model.PlayerProfile
import com.donik1998.cubeclash.core.model.ProfileRank
import com.donik1998.cubeclash.core.model.PublicProfile
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.SyncState
import com.donik1998.cubeclash.core.model.User
import com.donik1998.cubeclash.core.model.WcaEvent
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * The demo session.
 *
 * Built at runtime rather than checked in as fixtures: a seeded RNG gives a session that looks
 * like a real one — a plausible spread, one `+2`, one DNF — without a thousand lines of literal
 * data, and every screenshot of the app is reproducible because the seed is fixed.
 */
@Singleton
class DemoSeed @Inject constructor(
    private val scrambleGenerator: ScrambleGenerator,
) {
    val demoUser = User(
        id = "me",
        displayName = "Doniyor",
        email = "demo@cubeclash.app",
        country = "UZ",
        elo = 1214,
        solveCount = 1_482,
    )

    fun solves(event: WcaEvent, count: Int = 24): List<Solve> {
        val random = Random(event.ordinal * 31 + 7)
        val centre = centreMsFor(event)
        val now = Instant.now()

        return (0 until count).map { index ->
            val jitter = random.nextDouble(-0.18, 0.22)
            val timeMs = (centre * (1 + jitter)).toLong()
            val penalty = when {
                index == 3 -> Penalty.PLUS_TWO
                index == 9 -> Penalty.DNF
                else -> Penalty.NONE
            }
            Solve(
                id = "demo-${event.id}-$index",
                clientId = "demo-${event.id}-$index",
                event = event,
                scramble = scrambleGenerator.generate(event),
                scrambleSource = ScrambleSource.RANDOM,
                timeMs = timeMs,
                penalty = penalty,
                solvedAt = now.minus((index * 4).toLong(), ChronoUnit.MINUTES),
                moveCount = if (event == WcaEvent.FMC) 26 + random.nextInt(8) else null,
                solvedCount = if (event == WcaEvent.MBLD) 9 + random.nextInt(4) else null,
                attemptedCount = if (event == WcaEvent.MBLD) 13 else null,
                isPb = index == 5,
                syncState = SyncState.SYNCED,
            )
        }
    }

    /**
     * A demoable leaderboard page: a realistic global top, a tie sharing 3rd (`RANK()`, so the
     * next row is 5th, not 4th), one row with no country, and the viewer pinned outside page 1
     * at rank 1,204. Seeded off the event/metric/scope so the page is stable per selection.
     */
    fun leaderboard(
        event: WcaEvent,
        metric: LeaderboardMetric,
        scope: LeaderboardScope,
    ): LeaderboardPage {
        val random = Random(event.ordinal * 131 + metric.ordinal * 17 + scope.ordinal)
        // (displayName, country) — one entry has a null country on purpose.
        val roster = listOf(
            "kian_r" to "IR",
            "mira_v" to "DE",
            "leo_t" to "BR",
            // leo_t and nadia_k tie on 3rd; RANK() means the row after them is 5th, not 4th.
            "nadia_k" to "FR",
            "sora_h" to null,
            "owen_p" to "GB",
            "aiko_m" to "JP",
            "dario_s" to "IT",
        )
        val base = leaderboardCentreMsFor(event)

        // Build strictly increasing values, then force indices 2 and 3 to share one so they tie.
        val values = LongArray(roster.size)
        var running = base
        for (i in roster.indices) {
            running += random.nextLong(120, 900)
            values[i] = running
        }
        values[3] = values[2]

        val entries = roster.mapIndexed { index, (name, country) ->
            // RANK(): a row's rank is 1 + the count of strictly better (smaller) values.
            val rank = 1 + values.count { it < values[index] }
            LeaderboardEntry(
                rank = rank,
                userId = "demo-user-$index",
                displayName = name,
                country = country,
                valueMs = values[index],
                event = event,
                solvedAt = Instant.now().minus((index * 6).toLong(), ChronoUnit.HOURS),
            )
        }

        val viewer = LeaderboardEntry(
            rank = 1_204,
            userId = demoUser.id,
            displayName = demoUser.displayName,
            country = demoUser.country,
            valueMs = base + random.nextLong(9_000, 14_000),
            event = event,
            solvedAt = Instant.now().minus(2, ChronoUnit.DAYS),
        )

        return LeaderboardPage(
            entries = entries,
            nextCursor = "demo-cursor-2",
            viewer = viewer,
        )
    }

    /**
     * The You · Profile aggregate for the demo user — the screenshot's values: best 8.42s
     * (`8_420` ms), 3,204 solves, a 0.68 win rate, global rank #1,204, 48 friends, country UZ.
     * Rank and best echo the requested [event]/[scope] so the fake tracks the real contract.
     */
    fun profile(
        event: WcaEvent = WcaEvent.THREE,
        scope: LeaderboardScope = LeaderboardScope.GLOBAL,
    ): PlayerProfile = PlayerProfile(
        id = demoUser.id,
        displayName = demoUser.displayName,
        country = demoUser.country,
        elo = demoUser.elo,
        rank = ProfileRank(
            position = 1_204,
            event = event,
            metric = "single",
            scope = scope,
        ),
        bestSingleMs = 8_420,
        bestSingleEvent = event,
        totalSolves = 3_204,
        winRate = 0.68,
        wins = 217,
        losses = 102,
        friendCount = 48,
    )

    /**
     * A demoable *public* profile for some other player, keyed off [id] so different ids give
     * stable, distinct people. The head-to-head is present and non-zero so the demo exercises the
     * "we have raced" state; a fresh-account id (`"nobody"`) returns the all-null, never-raced
     * shape so both branches of the UI are reachable offline.
     */
    fun publicProfile(id: String): PublicProfile {
        if (id == "nobody") {
            return PublicProfile(
                id = id,
                displayName = "Fresh Cuber",
                country = null,
                elo = 1000,
                bestSingleMs = null,
                bestAo5Ms = null,
                bestAo12Ms = null,
                headToHead = null,
            )
        }
        val random = Random(id.hashCode())
        return PublicProfile(
            id = id,
            displayName = listOf("Probe", "Feliks_W", "Yiheng", "Max_P").random(random),
            country = listOf("UZ", "AU", "CN", "US", null).random(random),
            elo = 1_400 + random.nextInt(400),
            bestSingleMs = 5_200L + random.nextInt(3_000),
            bestAo5Ms = 6_100L + random.nextInt(3_000),
            bestAo12Ms = 6_600L + random.nextInt(3_000),
            headToHead = HeadToHead(wins = random.nextInt(6), losses = random.nextInt(6)),
        )
    }

    private fun leaderboardCentreMsFor(event: WcaEvent): Long = when (event.resultKind) {
        // Fewest Moves ranks on centi-moves; keep it in that unit so the UI formats it right.
        com.donik1998.cubeclash.core.model.ResultKind.MOVE_COUNT -> 2_100L
        else -> (centreMsFor(event) * 0.55).toLong()
    }

    /** Roughly what a competent club cuber averages, per event. */
    private fun centreMsFor(event: WcaEvent): Double = when (event) {
        WcaEvent.TWO -> 4_200.0
        WcaEvent.THREE -> 11_600.0
        WcaEvent.FOUR -> 42_000.0
        WcaEvent.FIVE -> 82_000.0
        WcaEvent.SIX -> 155_000.0
        WcaEvent.SEVEN -> 230_000.0
        WcaEvent.THREE_OH -> 21_400.0
        WcaEvent.CLOCK -> 9_800.0
        WcaEvent.MEGAMINX -> 68_000.0
        WcaEvent.PYRAMINX -> 6_100.0
        WcaEvent.SKEWB -> 7_300.0
        WcaEvent.SQUARE_ONE -> 16_500.0
        WcaEvent.THREE_BLD -> 96_000.0
        WcaEvent.FOUR_BLD -> 420_000.0
        WcaEvent.FIVE_BLD -> 900_000.0
        WcaEvent.FMC -> 1_800_000.0
        WcaEvent.MBLD -> 3_100_000.0
    }
}
