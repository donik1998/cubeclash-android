package com.donik1998.cubeclash.core.realtime

import com.donik1998.cubeclash.core.domain.realtime.RaceGateway
import com.donik1998.cubeclash.core.domain.repository.TokenStore
import com.donik1998.cubeclash.core.model.RaceCommand
import com.donik1998.cubeclash.core.model.RaceEvent
import com.donik1998.cubeclash.core.model.RacePlayer
import com.donik1998.cubeclash.core.model.RaceResult
import com.donik1998.cubeclash.core.model.RaceRoom
import com.donik1998.cubeclash.core.model.RaceStatus
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleNotation
import com.donik1998.cubeclash.core.model.WcaEvent
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

/**
 * The real gateway: Socket.IO on the `/race` namespace, because that is what the NestJS
 * gateway speaks.
 *
 * Everything here is translation — JSON in, typed [RaceEvent] out. No state machine, no
 * decisions: the server owns the room's status, and a client that could advance its own
 * state could also fake a time.
 */
@Singleton
class SocketIoRaceGateway @Inject constructor(
    private val socketUrl: String,
    private val tokenStore: TokenStore,
) : RaceGateway {

    private var socket: Socket? = null

    override fun events(): Flow<RaceEvent> = callbackFlow {
        val token = runBlocking { tokenStore.accessToken() }
        val options = IO.Options.builder()
            .setAuth(mapOf("token" to token.orEmpty()))
            .setTransports(arrayOf("websocket"))
            .build()

        val socket = IO.socket(URI.create("$socketUrl$NAMESPACE"), options).also { this@SocketIoRaceGateway.socket = it }

        fun on(name: String, map: (JSONObject) -> RaceEvent) {
            socket.on(name) { args ->
                val payload = args.firstOrNull() as? JSONObject ?: JSONObject()
                trySend(runCatching { map(payload) }.getOrElse { RaceEvent.Failed(it.message.orEmpty()) })
            }
        }

        on(EVENT_STATE) { RaceEvent.State(it.toRaceRoom()) }
        on(EVENT_READY_UPDATE) { RaceEvent.ReadyUpdate(it.optString("user_id"), it.optBoolean("ready")) }
        on(EVENT_COUNTDOWN) { RaceEvent.Countdown(it.optInt("n")) }
        on(EVENT_SCRAMBLE) {
            RaceEvent.ScrambleRevealed(Scramble.parse(it.optString("scramble"), ScrambleNotation.FACE_TURN))
        }
        on(EVENT_PROGRESS) {
            RaceEvent.OpponentProgress(it.optString("user_id"), it.optLong("running_ms"))
        }
        on(EVENT_RESULT) {
            RaceEvent.Settled(
                RaceResult(
                    winnerUserId = it.optString("winner_user_id").takeIf(String::isNotBlank),
                    yourTimeMs = it.optLong("your_time").takeIf { v -> v > 0 },
                    opponentTimeMs = it.optLong("opp_time").takeIf { v -> v > 0 },
                    eloDelta = it.optInt("elo_delta").takeIf { v -> v != 0 },
                ),
            )
        }

        socket.on(Socket.EVENT_DISCONNECT) { trySend(RaceEvent.Disconnected(null)) }
        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            trySend(RaceEvent.Failed(args.firstOrNull()?.toString().orEmpty()))
        }

        socket.connect()

        awaitClose {
            socket.off()
            socket.disconnect()
            this@SocketIoRaceGateway.socket = null
        }
    }

    override suspend fun send(command: RaceCommand) {
        val socket = socket ?: return
        when (command) {
            is RaceCommand.Create -> socket.emit(
                CMD_CREATE,
                JSONObject(mapOf("mode" to command.mode.wire, "event" to command.event.id)),
            )

            is RaceCommand.Join -> socket.emit(CMD_JOIN, JSONObject(mapOf("code" to command.code)))
            RaceCommand.Ready -> socket.emit(CMD_READY)
            RaceCommand.SolveStart -> socket.emit(CMD_SOLVE_START)
            is RaceCommand.SolveStop -> socket.emit(
                CMD_SOLVE_STOP,
                JSONObject(mapOf("client_time_ms" to command.clientTimeMs)),
            )

            RaceCommand.Leave -> socket.disconnect()
        }
    }

    private fun JSONObject.toRaceRoom(): RaceRoom {
        val playersJson = optJSONArray("players")
        val players = buildList {
            repeat(playersJson?.length() ?: 0) { index ->
                val player = playersJson?.optJSONObject(index) ?: return@repeat
                add(
                    RacePlayer(
                        userId = player.optString("user_id"),
                        displayName = player.optString("display_name"),
                        country = player.optString("country").takeIf(String::isNotBlank),
                        elo = player.optInt("elo").takeIf { it > 0 },
                        isReady = player.optBoolean("ready"),
                        runningMs = player.optLong("running_ms").takeIf { it > 0 },
                        finalTimeMs = player.optLong("time_ms").takeIf { it > 0 },
                    ),
                )
            }
        }
        return RaceRoom(
            id = optString("race_id"),
            mode = com.donik1998.cubeclash.core.model.RaceMode.QUICK,
            event = WcaEvent.fromId(optString("event")),
            status = RaceStatus.fromWire(optString("status")),
            code = optString("code").takeIf(String::isNotBlank),
            players = players,
            scramble = optString("scramble")
                .takeIf(String::isNotBlank)
                ?.let { Scramble.parse(it, ScrambleNotation.FACE_TURN) }
                ?: Scramble.EMPTY,
        )
    }

    private companion object {
        const val NAMESPACE = "/race"

        const val EVENT_STATE = "race:state"
        const val EVENT_READY_UPDATE = "race:ready_update"
        const val EVENT_COUNTDOWN = "race:countdown"
        const val EVENT_SCRAMBLE = "race:scramble"
        const val EVENT_PROGRESS = "race:opponent_progress"
        const val EVENT_RESULT = "race:result"

        const val CMD_CREATE = "race:create"
        const val CMD_JOIN = "race:join"
        const val CMD_READY = "race:ready"
        const val CMD_SOLVE_START = "solve:start"
        const val CMD_SOLVE_STOP = "solve:stop"
    }
}
