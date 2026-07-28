package com.donik1998.cubeclash.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.navigation.TopLevelDestination

/**
 * A floating rounded card inset from the screen edges — not a full-width bar with a divider.
 *
 * The signature motion (variant C) is a **pinch-squeeze** on the selected pill plus a
 * **directional icon tilt**: the icon leans towards the tab you came from, so the bar reads as
 * one object being pushed around rather than four independent buttons lighting up.
 */
@Composable
fun CubeClashBottomBar(
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CubeClashTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            .shadow(12.dp, Radius.pill)
            .background(colors.surface, Radius.pill)
            .padding(Spacing.xxs),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val isSelected = destination == selected
            val direction = (destination.ordinal - selected.ordinal).coerceIn(-1, 1)

            val squeeze by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.92f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
                label = "squeeze",
            )
            val tilt by animateFloatAsState(
                targetValue = if (isSelected) 0f else direction * 8f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
                label = "tilt",
            )

            Column(
                modifier = Modifier
                    .scale(squeeze)
                    .background(
                        if (isSelected) colors.brandPrimarySoft else androidx.compose.ui.graphics.Color.Transparent,
                        Radius.pill,
                    )
                    .clickable(
                        interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(destination) },
                    )
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = destination.icon(),
                    contentDescription = destination.label,
                    tint = if (isSelected) colors.brandPrimary else colors.textMuted,
                    modifier = Modifier.graphicsLayer { rotationZ = tilt },
                )
                Text(
                    text = destination.label,
                    style = CubeClashTheme.typography.caption,
                    color = if (isSelected) colors.brandPrimary else colors.textMuted,
                )
            }
        }
    }
}

private fun TopLevelDestination.icon() = when (this) {
    TopLevelDestination.TIMER -> Icons.Rounded.Timer
    TopLevelDestination.RACE -> Icons.Rounded.Bolt
    TopLevelDestination.STATS -> Icons.Rounded.BarChart
    TopLevelDestination.YOU -> Icons.Rounded.Person
}
