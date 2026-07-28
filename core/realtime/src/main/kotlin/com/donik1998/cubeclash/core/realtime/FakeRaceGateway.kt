package com.donik1998.cubeclash.core.realtime

import com.donik1998.cubeclash.core.domain.realtime.RaceGateway
import com.donik1998.cubeclash.core.domain.scramble.ScrambleGenerator
import com.donik1998.cubeclash.core.model.RaceCommand
import com.donik1998.cubeclash.core.model.RaceEvent
import com.donik1998.cubeclash.core.model.RaceMode
import com.donik1998.cubeclash.core.model.RacePlayer
import com.donik1998.cubeclash.core.model.RaceResult
import com.donik1998.cubeclash.core.model.RaceRoom
import com.donik1998.cubeclash.core.model.RaceStatus
import com.donik1998.cubeclash.core.model.WcaEvent
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * A scripted opponent.
 *
 * This exists so the race — the most interesting screen in the app — can be demoed, reviewed
 * and screenshotted before `cubeclash-backend`'s race gateway is live. It plays the *same*
 * protocol the real server does, in the same order, so swapping [SocketIoRaceGateway] in
 * changes one Hilt binding and nothing else.
 *
 * It is not a simulator and does not pretend to be authoritative: it always settles the room
 * itself, exactly as the server would, because the client must never be the thing that decides.
 */
@Singleton
class FakeRaceGateway @Inject constructor(
    private val scrambleGenerator: ScrambleGenerator,
) : RaceGateway {

    private val commands = MutableSharedFlow<RaceCommand>(
        replay = 1,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val random = Random(42)

    override fun events(): Flow<RaceEvent> = channelFlow {
        var event = WcaEvent.THREE
        var mode = RaceMode.QUICK
        var yourTimeMs: Long? = null

        val you = RacePlayer(userId = ME, displayName = "You", country = "UZ", elo = 1214)
        val rival = RacePlayer(userId = RIVAL, displayName = "Kaito M.", country = "JP", elo = 1268)

        launch {
            commands.collect { command ->
                when (command) {
                    is RaceCommand.Create -> {
                        event = command.event
                        mode = command.mode
                        channel.send(RaceEvent.State(room(event, mode, RaceStatus.WAITING, listOf(you))))
                        delay(MATCHMAKING_MS)
                        channel.send(RaceEvent.State(room(event, mode, RaceStatus.READY_CHECK, listOf(you, rival))))
                        // The opponent is never instantly ready — that reads as a bot.
                        delay(RIVAL_READY_MS)
                        channel.send(RaceEvent.ReadyUpdate(RIVAL, ready = true))
                    }

                    is RaceCommand.Join -> {
                        channel.send(RaceEvent.State(room(event, RaceMode.PRIVATE, RaceStatus.READY_CHECK, listOf(you, rival))))
                        delay(RIVAL_READY_MS)
                        channel.send(RaceEvent.ReadyUpdate(RIVAL, ready = true))
                    }

                    RaceCommand.Ready -> {
                        channel.send(RaceEvent.State(room(event, mode, RaceStatus.COUNTDOWN, listOf(you, rival))))
                        (3 downTo 1).forEach {
                            channel.send(RaceEvent.Countdown(it))
                            delay(1_000)
                        }
                        channel.send(RaceEvent.ScrambleRevealed(scrambleGenerator.generate(event)))
                        channel.send(RaceEvent.State(room(event, mode, RaceStatus.RACING, listOf(you, rival))))

                        val rivalTime = 9_000L + random.nextLong(6_000)
                        launch {
                            var elapsed = 0L
                            while (elapsed < rivalTime) {
                                delay(PROGRESS_TICK_MS)
                                elapsed += PROGRESS_TICK_MS
                                channel.send(RaceEvent.OpponentProgress(RIVAL, elapsed))
                            }
                            channel.settleIfReady(yourTimeMs, rivalTime)
                        }
                    }

                    is RaceCommand.SolveStop -> {
                        yourTimeMs = command.clientTimeMs
                        channel.settleIfReady(yourTimeMs, null)
                    }

                    RaceCommand.SolveStart, RaceCommand.Leave -> Unit
                }
            }
        }
    }

    /**
     * Settling is the server's call in the real protocol, so the fake makes it in one place too,
     * only once both times exist.
     */
    private suspend fun SendChannel<RaceEvent>.settleIfReady(
        yourTimeMs: Long?,
        rivalTimeMs: Long?,
    ) {
        val mine = yourTimeMs ?: return
        val theirs = rivalTimeMs ?: rivalFallbackMs
        rivalFallbackMs = theirs
        val youWon = mine < theirs
        send(
            RaceEvent.Settled(
                RaceResult(
                    winnerUserId = if (youWon) ME else RIVAL,
                    yourTimeMs = mine,
                    opponentTimeMs = theirs,
                    eloDelta = if (youWon) 12 else -11,
                ),
            ),
        )
    }

    private var rivalFallbackMs: Long = 11_240

    private fun room(
        event: WcaEvent,
        mode: RaceMode,
        status: RaceStatus,
        players: List<RacePlayer>,
    ) = RaceRoom(id = "fake-race", mode = mode, event = event, status = status, players = players)

    override suspend fun send(command: RaceCommand) {
        commands.emit(command)
    }

    companion object {
        const val ME = "me"
        const val RIVAL = "rival"
        private const val MATCHMAKING_MS = 1_800L
        private const val RIVAL_READY_MS = 1_200L

        /** 10 Hz for short events; the real cadence drops to 4 Hz for long ones. */
        private const val PROGRESS_TICK_MS = 100L
    }
}
