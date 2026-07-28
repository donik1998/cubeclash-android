package com.donik1998.cubeclash.core.domain.common

import javax.inject.Qualifier

/**
 * Dispatchers are injected, never referenced statically — that is the only way the timer
 * and race state machines stay testable with a virtual clock.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: CubeClashDispatcher)

enum class CubeClashDispatcher { Default, IO }

/** Injectable wall clock, so "now" is a parameter rather than a global. */
fun interface TimeSource {
    fun nowMillis(): Long
}
