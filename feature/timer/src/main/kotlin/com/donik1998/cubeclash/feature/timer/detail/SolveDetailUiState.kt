package com.donik1998.cubeclash.feature.timer.detail

import com.donik1998.cubeclash.core.model.Solve

/**
 * The Solve Detail screen's frame.
 *
 * The solve is resolved from the history flow rather than a by-id endpoint, so [isLoading] covers
 * "still looking" and a null [solve] once loading finishes means "not here" — deleted on another
 * device, most likely. [isSaving] gates the penalty and delete controls while a write is in flight
 * so a double tap can't race.
 */
data class SolveDetailUiState(
    val solveId: String,
    val solve: Solve? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val message: String? = null,
    /** Flipped once the solve is gone, so the route can pop back exactly once. */
    val isDeleted: Boolean = false,
)
