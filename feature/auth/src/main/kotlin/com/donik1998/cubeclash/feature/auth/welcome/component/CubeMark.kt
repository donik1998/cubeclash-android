package com.donik1998.cubeclash.feature.auth.welcome.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius

/**
 * The product mark: a 3×3 cube face in the brand palette. Purely decorative and stateless, drawn
 * from tokens rather than a bundled asset so it flips with the theme like everything else. One tile
 * is lit in [CubeClashColors.accentEnergy] — the same "somebody else is solving too" energy the
 * pitch line carries.
 */
@Composable
fun CubeMark(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
) {
    val colors = CubeClashTheme.colors
    val tile = (size - Padding * 2 - Gap * 2) / 3

    // Which of the nine tiles is the accent one, reading left-to-right, top-to-bottom.
    val accentIndex = 4

    Box(
        modifier = modifier
            .size(size)
            .background(colors.brandPrimarySoft, Radius.lg)
            .padding(Padding),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Gap)) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Gap)) {
                    repeat(3) { col ->
                        val index = row * 3 + col
                        val fill: Color =
                            if (index == accentIndex) colors.accentEnergy else colors.brandPrimary
                        Box(
                            modifier = Modifier
                                .size(tile)
                                .background(fill, Radius.sm),
                        )
                    }
                }
            }
        }
    }
}

private val Padding = 10.dp
private val Gap = 6.dp
