package com.donik1998.cubeclash.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.icon.PuzzleBadge
import com.donik1998.cubeclash.core.designsystem.icon.PuzzleIcon
import com.donik1998.cubeclash.core.designsystem.icon.PuzzleShape
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.model.PuzzleFamily
import com.donik1998.cubeclash.core.model.PuzzleModifier
import com.donik1998.cubeclash.core.model.WcaEvent

/** The one place the event table meets the icon table. Adding an event touches neither. */
@Composable
fun EventIcon(
    event: WcaEvent,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    size: Dp = 24.dp,
    tint: Color = if (active) CubeClashTheme.colors.brandPrimary else CubeClashTheme.colors.textMuted,
) {
    PuzzleIcon(
        shape = event.family.toShape(),
        modifier = modifier,
        gridSize = event.size ?: 3,
        badge = event.modifier.toBadge(),
        tint = tint,
        size = size,
    )
}

private fun PuzzleFamily.toShape(): PuzzleShape = when (this) {
    PuzzleFamily.NXN -> PuzzleShape.Grid
    PuzzleFamily.MEGAMINX -> PuzzleShape.Pentagon
    PuzzleFamily.PYRAMINX -> PuzzleShape.Triangle
    PuzzleFamily.SKEWB -> PuzzleShape.SkewbCut
    PuzzleFamily.SQUARE_ONE -> PuzzleShape.SquareOne
    PuzzleFamily.CLOCK -> PuzzleShape.Clock
}

private fun PuzzleModifier.toBadge(): PuzzleBadge = when (this) {
    PuzzleModifier.NONE -> PuzzleBadge.None
    PuzzleModifier.BLINDFOLDED -> PuzzleBadge.Blindfold
    PuzzleModifier.ONE_HANDED -> PuzzleBadge.OneHanded
    PuzzleModifier.FEWEST_MOVES -> PuzzleBadge.FewestMoves
    PuzzleModifier.MULTI_BLIND -> PuzzleBadge.MultiBlind
}
