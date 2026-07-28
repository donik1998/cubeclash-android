package com.donik1998.cubeclash.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius

enum class ChipTone { Brand, Warning, Danger, Neutral, Success }

@Composable
fun CubeChip(
    label: String,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.Neutral,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = CubeClashTheme.colors
    val content: Color = when (tone) {
        ChipTone.Brand -> colors.brandPrimary
        ChipTone.Warning -> colors.warning
        ChipTone.Danger -> colors.danger
        ChipTone.Success -> colors.success
        ChipTone.Neutral -> colors.textSecondary
    }
    val container = when {
        tone == ChipTone.Brand || selected -> colors.brandPrimarySoft
        else -> colors.surfaceAlt
    }

    Row(
        modifier = modifier
            .background(container, Radius.pill)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) colors.brandPrimary else Color.Transparent,
                shape = Radius.pill,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        leading?.invoke()
        Text(
            text = label,
            style = CubeClashTheme.typography.caption,
            color = if (selected) colors.brandPrimary else content,
        )
    }
}
