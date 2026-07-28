package com.donik1998.cubeclash.core.designsystem.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Event icons are **composed, not enumerated**.
 *
 * Seventeen events are eleven puzzles and five disciplines — `3BLD` *is* a 3×3, one-handed
 * *is* a 3×3, and cubers name them that way. So the icon is a base shape plus an optional
 * badge in the corner the shape vacates, drawn parametrically. An eighteenth event becomes a
 * row in a table rather than a new vector file, and a 4×4 and a 4BLD read as the same puzzle
 * at a glance — which is the actual job of an icon in a picker.
 */
enum class PuzzleShape { Grid, Pentagon, Triangle, SkewbCut, SquareOne, Clock }

enum class PuzzleBadge { None, Blindfold, MultiBlind, OneHanded, FewestMoves }

@Composable
fun PuzzleIcon(
    shape: PuzzleShape,
    modifier: Modifier = Modifier,
    gridSize: Int = 3,
    badge: PuzzleBadge = PuzzleBadge.None,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val side = min(this.size.width, this.size.height)
        val inset = side * 0.08f
        val box = Rect(Offset(inset, inset), Size(side - inset * 2, side - inset * 2))

        when (shape) {
            PuzzleShape.Grid -> drawGrid(box, gridSize, tint, stroke)
            PuzzleShape.Pentagon -> drawPolygon(box, sides = 5, tint = tint, stroke = stroke)
            PuzzleShape.Triangle -> drawPolygon(box, sides = 3, tint = tint, stroke = stroke)
            PuzzleShape.SkewbCut -> drawSkewb(box, tint, stroke)
            PuzzleShape.SquareOne -> drawSquareOne(box, tint, stroke)
            PuzzleShape.Clock -> drawClock(box, tint, stroke)
        }

        if (badge != PuzzleBadge.None) drawBadge(badge, box, tint, stroke)
    }
}

private fun DrawScope.drawGrid(box: Rect, gridSize: Int, tint: Color, stroke: Stroke) {
    val radius = box.width * 0.14f
    drawRoundRect(
        color = tint,
        topLeft = box.topLeft,
        size = box.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
        style = stroke,
    )
    // Only the interior lines change with N, which is the whole point of drawing rather than exporting.
    val step = box.width / gridSize
    for (i in 1 until gridSize) {
        val offset = i * step
        drawLine(tint, Offset(box.left + offset, box.top), Offset(box.left + offset, box.bottom), stroke.width)
        drawLine(tint, Offset(box.left, box.top + offset), Offset(box.right, box.top + offset), stroke.width)
    }
}

private fun DrawScope.drawPolygon(box: Rect, sides: Int, tint: Color, stroke: Stroke) {
    val radius = box.width / 2f
    val center = box.center
    val path = Path()
    for (i in 0 until sides) {
        val angle = (-Math.PI / 2 + 2 * Math.PI * i / sides).toFloat()
        val point = Offset(center.x + radius * cos(angle), center.y + radius * sin(angle))
        if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    drawPath(path, tint, style = stroke)
}

private fun DrawScope.drawSkewb(box: Rect, tint: Color, stroke: Stroke) {
    drawRect(tint, box.topLeft, box.size, style = stroke)
    drawLine(tint, box.topLeft, box.bottomRight, stroke.width)
    drawLine(tint, Offset(box.right, box.top), Offset(box.left, box.bottom), stroke.width)
}

private fun DrawScope.drawSquareOne(box: Rect, tint: Color, stroke: Stroke) {
    drawRect(tint, box.topLeft, box.size, style = stroke)
    // The off-centre bisection is the puzzle's signature: two unequal halves.
    val split = box.left + box.width * 0.38f
    drawLine(tint, Offset(split, box.top), Offset(split, box.bottom), stroke.width)
    drawLine(tint, Offset(box.left, box.center.y), Offset(box.right, box.center.y), stroke.width)
}

private fun DrawScope.drawClock(box: Rect, tint: Color, stroke: Stroke) {
    drawCircle(tint, radius = box.width / 2f, center = box.center, style = stroke)
    drawLine(tint, box.center, Offset(box.center.x, box.top + box.height * 0.18f), stroke.width)
    drawLine(tint, box.center, Offset(box.right - box.width * 0.22f, box.center.y), stroke.width)
}

/** Bottom-right, in the corner the base shape vacates. */
private fun DrawScope.drawBadge(badge: PuzzleBadge, box: Rect, tint: Color, stroke: Stroke) {
    val badgeSize = box.width * 0.34f
    val center = Offset(box.right - badgeSize * 0.35f, box.bottom - badgeSize * 0.35f)
    drawCircle(Color.Black.copy(alpha = 0f), badgeSize / 2f, center)

    when (badge) {
        PuzzleBadge.Blindfold -> drawLine(
            tint,
            Offset(center.x - badgeSize / 2, center.y),
            Offset(center.x + badgeSize / 2, center.y),
            stroke.width * 1.4f,
            cap = StrokeCap.Round,
        )

        PuzzleBadge.MultiBlind -> listOf(-0.28f, 0f, 0.28f).forEach { dy ->
            drawLine(
                tint,
                Offset(center.x - badgeSize / 2, center.y + badgeSize * dy),
                Offset(center.x + badgeSize / 2, center.y + badgeSize * dy),
                stroke.width,
                cap = StrokeCap.Round,
            )
        }

        PuzzleBadge.OneHanded -> drawCircle(tint, badgeSize * 0.24f, center)

        PuzzleBadge.FewestMoves -> {
            listOf(-0.18f, 0.18f).forEach { d ->
                drawLine(
                    tint,
                    Offset(center.x + badgeSize * d, center.y - badgeSize / 2),
                    Offset(center.x + badgeSize * d, center.y + badgeSize / 2),
                    stroke.width,
                )
                drawLine(
                    tint,
                    Offset(center.x - badgeSize / 2, center.y + badgeSize * d),
                    Offset(center.x + badgeSize / 2, center.y + badgeSize * d),
                    stroke.width,
                )
            }
        }

        PuzzleBadge.None -> Unit
    }
}
