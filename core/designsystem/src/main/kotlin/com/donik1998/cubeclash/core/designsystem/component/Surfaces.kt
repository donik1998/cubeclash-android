package com.donik1998.cubeclash.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Spacing

@Composable
fun CubeCard(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(Spacing.md),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(CubeClashTheme.colors.surface, Radius.card)
            .border(1.dp, CubeClashTheme.colors.borderSubtle, Radius.card)
            .padding(contentPadding),
        content = content,
    )
}

/** Value over label. The value uses tabular figures so a changing average does not shift width. */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    Column(
        modifier = modifier
            .background(
                if (highlighted) CubeClashTheme.colors.brandPrimarySoft else CubeClashTheme.colors.surfaceAlt,
                Radius.lg,
            )
            .padding(vertical = Spacing.sm, horizontal = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = CubeClashTheme.typography.title,
            color = if (highlighted) CubeClashTheme.colors.brandPrimary else CubeClashTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label.uppercase(),
            style = CubeClashTheme.typography.overline,
            color = CubeClashTheme.colors.textMuted,
        )
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = CubeClashTheme.typography.overline,
        color = CubeClashTheme.colors.textMuted,
    )
}
