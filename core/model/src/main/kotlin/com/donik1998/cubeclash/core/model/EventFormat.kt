package com.donik1998.cubeclash.core.model

/**
 * Competition format, WCA Regulation 9b — and the three cards that sit under the timer.
 *
 * The decision encoded here: **competition format leads, practice statistic follows.**
 * They answer different questions and a practice timer is asked both. The competition
 * format is the only number that transfers to a real result, so it leads; `ao5`/`ao12`
 * answer *"am I getting better"*, which is what a solo session is actually for.
 *
 * `ao12` is dropped from the [MO3] row rather than squeezed in: twelve three-minute
 * solves is most of an hour, so the number would almost never fill.
 */
enum class EventFormat(
    val label: String,
    /** The regulation this row encodes. Quoted so a future edit has to argue with the WCA. */
    val regulation: String,
    val sessionStats: List<SessionStatKind>,
) {
    /** 9b1a — average of 5, dropping best and worst. */
    AO5("Ao5", "9b1a", listOf(SessionStatKind.BEST, SessionStatKind.AO5, SessionStatKind.AO12)),

    /** 9b2a (big cubes) and 9b4a (Fewest Moves) — mean of 3, no trimming. */
    MO3("Mo3", "9b2a / 9b4a", listOf(SessionStatKind.BEST, SessionStatKind.MO3, SessionStatKind.AO5)),

    /** 9b3a — best of 3. 9b3b additionally recognises a Mo3 ranking, hence the second card. */
    BO3("Bo3", "9b3a", listOf(SessionStatKind.BEST, SessionStatKind.MO3, SessionStatKind.AO5)),

    /**
     * 9b5a — best of X, X ∈ {1,2,3}. Bo1 is a *choice*: a solo session has no round, so one
     * attempt is the unit. Multi-Blind gets no average at all — averaging attempts that each
     * chose a different cube count is meaningless.
     */
    BO1("Bo1", "9b5a", listOf(SessionStatKind.BEST, SessionStatKind.LAST, SessionStatKind.COUNT)),
}

enum class SessionStatKind(val label: String) {
    BEST("Best"),
    AO5("Ao5"),
    AO12("Ao12"),
    MO3("Mo3"),
    LAST("Last"),
    COUNT("Solves"),
}
