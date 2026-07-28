package com.donik1998.cubeclash.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.CubePrimaryButton
import com.donik1998.cubeclash.core.designsystem.component.CubeTextField
import com.donik1998.cubeclash.core.designsystem.component.SegmentedControl
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing

@Composable
fun AuthRoute(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
    ) {
        Text(
            text = "CubeClash",
            style = CubeClashTheme.typography.h1,
            color = CubeClashTheme.colors.textPrimary,
        )
        Text(
            text = "A WCA timer, and a live 1v1 race against somebody who is also trying.",
            style = CubeClashTheme.typography.body,
            color = CubeClashTheme.colors.textSecondary,
        )

        SegmentedControl(
            options = AuthMode.entries,
            selected = uiState.mode,
            onSelect = viewModel::setMode,
            label = { it.label },
        )

        if (uiState.mode == AuthMode.SIGN_UP) {
            CubeTextField(
                value = uiState.displayName,
                onValueChange = viewModel::setDisplayName,
                label = "Display name",
            )
        }

        CubeTextField(
            value = uiState.email,
            onValueChange = viewModel::setEmail,
            label = "Email",
            keyboardType = KeyboardType.Email,
        )
        CubeTextField(
            value = uiState.password,
            onValueChange = viewModel::setPassword,
            label = "Password",
            isPassword = true,
            error = uiState.error,
        )

        CubePrimaryButton(
            text = uiState.mode.label,
            onClick = viewModel::submit,
            enabled = uiState.canSubmit,
            loading = uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
