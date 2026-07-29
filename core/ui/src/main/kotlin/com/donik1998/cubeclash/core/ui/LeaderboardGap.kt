package com.donik1998.cubeclash.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing

/**
 * The `· · ·` break between the top page of the leaderboard and the viewer's own row —
 * Figma `div` (44:227). It stands for the ranks the list is skipping over; the viewer sits
 * at ~1,204 while the page shows 1–5.
 */
@Composable
fun LeaderboardGap(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "· · ·",
            style = CubeClashTheme.typography.bodyStrong,
            color = CubeClashTheme.colors.textMuted,
        )
    }
}
