package com.donik1998.cubeclash.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius

/**
 * The recessed-track / elevated-thumb control from the frames. Used for the scramble source,
 * My Stats vs Leaderboards, and the lobby's three modes.
 */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String,
) {
    val colors = CubeClashTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.segTrack, Radius.pill)
            .padding(4.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val elevation by animateFloatAsState(if (isSelected) 2f else 0f, label = "segThumb")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(elevation.dp, Radius.pill)
                                .background(colors.segThumb, Radius.pill)
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSelect(option) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(option),
                    style = CubeClashTheme.typography.label,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) colors.textPrimary else colors.textSecondary,
                )
            }
        }
    }
}
