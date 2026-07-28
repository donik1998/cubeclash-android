package com.donik1998.cubeclash.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius

private val ButtonPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
private val MinHeight = 48.dp

@Composable
fun CubePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = MinHeight),
        enabled = enabled && !loading,
        shape = Radius.button,
        contentPadding = ButtonPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = CubeClashTheme.colors.brandPrimary,
            contentColor = CubeClashTheme.colors.onBrandPrimary,
            disabledContainerColor = CubeClashTheme.colors.surfaceAlt,
            disabledContentColor = CubeClashTheme.colors.textMuted,
        ),
    ) {
        ButtonContent(text, loading)
    }
}

@Composable
fun CubeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = MinHeight),
        enabled = enabled && !loading,
        shape = Radius.button,
        contentPadding = ButtonPadding,
        border = BorderStroke(1.dp, CubeClashTheme.colors.borderStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CubeClashTheme.colors.surfaceAlt,
            contentColor = CubeClashTheme.colors.textPrimary,
        ),
    ) {
        ButtonContent(text, loading)
    }
}

@Composable
fun CubeGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = MinHeight),
        enabled = enabled,
        shape = Radius.button,
        contentPadding = ButtonPadding,
    ) {
        Text(text, style = CubeClashTheme.typography.label, color = CubeClashTheme.colors.brandPrimary)
    }
}

@Composable
private fun ButtonContent(text: String, loading: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = CubeClashTheme.colors.onBrandPrimary,
            )
        }
        Text(text, style = CubeClashTheme.typography.label)
    }
}
