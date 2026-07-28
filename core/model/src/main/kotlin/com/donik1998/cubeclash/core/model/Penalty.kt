package com.donik1998.cubeclash.core.model

/** WCA penalties. A `+2` is a time penalty only; a DNF applies to every event. */
enum class Penalty(val wire: String) {
    NONE("none"),
    PLUS_TWO("plus_two"),
    DNF("dnf"),
    ;

    companion object {
        fun fromWire(wire: String?): Penalty = entries.firstOrNull { it.wire == wire } ?: NONE
    }
}

/** Where a scramble came from. Maps 1:1 to the API's `scramble_source`. */
enum class ScrambleSource(val wire: String, val label: String) {
    RANDOM("random", "Random"),
    WCA_COMPS("wca_comps", "WCA comps"),
    LAST_USED("last_used", "Last used"),
    ;

    companion object {
        fun fromWire(wire: String?): ScrambleSource = entries.firstOrNull { it.wire == wire } ?: RANDOM
    }
}
