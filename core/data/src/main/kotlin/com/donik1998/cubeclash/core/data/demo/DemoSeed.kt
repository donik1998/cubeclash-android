package com.donik1998.cubeclash.core.data.demo

import com.donik1998.cubeclash.core.domain.scramble.ScrambleGenerator
import com.donik1998.cubeclash.core.model.Penalty
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
