package com.donik1998.cubeclash.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.donik1998.cubeclash.core.designsystem.component.CubeCard
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleDensity

/**
 * The scramble, rendered honestly.
 *
 * Line breaks are **structure, not wrapping**: each line is its own paragraph, so a Megaminx
 * keeps the seven lines cubers execute it in. Type size steps down with length, and past the
 * dense step the card collapses behind an expander rather than shrinking further — below that
 * a scramble stops being readable at arm's length, which is the only reason the card exists.
 */
@Composable
fun ScrambleCard(
    scramble: Scramble,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    var expanded by remember(scramble) { mutableStateOf(false) }
    val collapsed = scramble.density == ScrambleDensity.COLLAPSED && !expanded

    val style = when (scramble.density) {
        ScrambleDensity.NORMAL -> CubeClashTheme.typography.scramble
        ScrambleDensity.COMPACT -> CubeClashTheme.typography.scrambleCompact
        else -> CubeClashTheme.typography.scrambleDense
    }

    CubeCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (scramble.density == ScrambleDensity.COLLAPSED) Modifier.clickable { expanded = !expanded } else Modifier),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = (eyebrow ?: "Scramble").uppercase(),
                style = CubeClashTheme.typography.overline,
                color = CubeClashTheme.colors.textMuted,
            )
            trailing?.invoke()
        }

        if (collapsed) {
            Text(
                text = "${scramble.tokenCount} moves · tap to show",
                style = CubeClashTheme.typography.small,
                color = CubeClashTheme.colors.textSecondary,
            )
        }

        AnimatedVisibility(visible = !collapsed) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                scramble.lines.forEach { line ->
                    Text(
                        text = line.joinToString(" "),
                        style = style,
                        color = CubeClashTheme.colors.textPrimary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
