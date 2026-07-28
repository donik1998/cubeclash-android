package com.donik1998.cubeclash.core.domain.realtime

import com.donik1998.cubeclash.core.model.RaceCommand
import com.donik1998.cubeclash.core.model.RaceEvent
import kotlinx.coroutines.flow.Flow

/**
 * The live race channel, as the domain sees it: a stream in, commands out.
 *
 * Deliberately not "a WebSocket" — the transport is Socket.IO today because that is what the
 * NestJS gateway speaks, and the race feature has no business knowing that.
 */
interface RaceGateway {

    /** Cold: collecting it opens the connection, cancelling it closes cleanly. */
    fun events(): Flow<RaceEvent>

    suspend fun send(command: RaceCommand)

    companion object {
        /**
         * The client-side silence watchdog.
         *
         * The protocol documents no timeout and the server's disconnect grace window is
         * unspecified, so a client that blocks back-navigation mid-race can be stranded if
         * `race:result` never arrives. Twenty seconds with **no inbound message of any kind**
         * surfaces an exit.
         *
         * Measured as *silence* rather than as elapsed wait on purpose: `race:opponent_progress`
         * arrives continuously while the opponent is still solving, so no per-event timeout is
         * needed — twenty seconds of nothing is wrong whether the event is a 2×2 or a 7×7.
         *
         * It never decides an outcome. The server still owns the result; the submitted time
         * stays with it, so leaving is not a forfeit — and the UI has to say so, because
         * otherwise nobody takes the exit.
         */
        const val SILENCE_WATCHDOG_MS = 20_000L
    }
}
