package com.donik1998.cubeclash.feature.auth.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.CubeGhostButton
import com.donik1998.cubeclash.core.designsystem.component.CubePrimaryButton
import com.donik1998.cubeclash.core.designsystem.component.CubeTextField
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.feature.auth.setup.component.CountryPicker

/**
 * Layer A: owns the [ProfileSetupViewModel], collects its state, and advances into the shell
 * exactly once when the profile is done — whether that came from a successful update or a skip.
 * No layout or styling lives here.
 */
@Composable
fun ProfileSetupRoute(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) onDone()
    }

    ProfileSetupScreen(
        uiState = uiState,
        onDisplayNameChange = viewModel::setDisplayName,
        onToggleCountry = viewModel::toggleCountry,
        onSubmit = viewModel::submit,
        onSkip = viewModel::skip,
        modifier = modifier,
    )
}

/**
 * Layer B: pure and testable. Given a resolved [ProfileSetupUiState] and callbacks it draws the
 * name field, the optional country chips, the submit button and the skip path. It imports no Hilt
 * and no ViewModel, so idle, invalid, in-flight and error are all reachable from a preview.
 */
@Composable
fun ProfileSetupScreen(
    uiState: ProfileSetupUiState,
    onDisplayNameChange: (String) -> Unit,
    onToggleCountry: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
    ) {
        Text(
            text = "Almost there",
            style = CubeClashTheme.typography.h2,
            color = CubeClashTheme.colors.textPrimary,
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = "This is what other cubers see on leaderboards and in races.",
            style = CubeClashTheme.typography.small,
            color = CubeClashTheme.colors.textSecondary,
        )

        Spacer(Modifier.height(Spacing.xl))
        CubeTextField(
            value = uiState.displayName,
            onValueChange = onDisplayNameChange,
            label = "Display name",
            placeholder = "cuber99",
        )

        Spacer(Modifier.height(Spacing.xl))
        SectionHeader("Country")
        Spacer(Modifier.height(Spacing.xxs))
        Text(
            text = "Optional — it puts you on the country leaderboard.",
            style = CubeClashTheme.typography.caption,
            color = CubeClashTheme.colors.textMuted,
        )
        Spacer(Modifier.height(Spacing.sm))
        CountryPicker(selected = uiState.country, onToggle = onToggleCountry)

        // A failed update surfaces here rather than dropping the user into the shell.
        if (uiState.error != null) {
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = uiState.error,
                style = CubeClashTheme.typography.caption,
                color = CubeClashTheme.colors.danger,
            )
        }

        Spacer(Modifier.height(Spacing.xl))
        CubePrimaryButton(
            // The button text carries the retry affordance after a failure.
            text = if (uiState.error != null) "Try again" else "Start solving",
            onClick = onSubmit,
            enabled = uiState.canSubmit,
            loading = uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.xs))
        CubeGhostButton(
            text = "Skip for now",
            onClick = onSkip,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.lg))
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Setup idle · Light", showBackground = true)
@Preview(name = "Setup idle · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileSetupIdlePreview() {
    CubeClashTheme {
        ProfileSetupScreen(
            uiState = ProfileSetupUiState(displayName = "cuber99", country = "UZ"),
            onDisplayNameChange = {}, onToggleCountry = {}, onSubmit = {}, onSkip = {},
        )
    }
}

@Preview(name = "Setup invalid · Light", showBackground = true)
@Preview(name = "Setup invalid · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileSetupInvalidPreview() {
    CubeClashTheme {
        ProfileSetupScreen(
            uiState = ProfileSetupUiState(displayName = ""),
            onDisplayNameChange = {}, onToggleCountry = {}, onSubmit = {}, onSkip = {},
        )
    }
}

@Preview(name = "Setup in-flight · Light", showBackground = true)
@Preview(name = "Setup in-flight · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileSetupInFlightPreview() {
    CubeClashTheme {
        ProfileSetupScreen(
            uiState = ProfileSetupUiState(displayName = "cuber99", isSubmitting = true),
            onDisplayNameChange = {}, onToggleCountry = {}, onSubmit = {}, onSkip = {},
        )
    }
}

@Preview(name = "Setup error · Light", showBackground = true)
@Preview(name = "Setup error · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileSetupErrorPreview() {
    CubeClashTheme {
        ProfileSetupScreen(
            uiState = ProfileSetupUiState(
                displayName = "cuber99",
                error = "Can't reach CubeClash right now.",
            ),
            onDisplayNameChange = {}, onToggleCountry = {}, onSubmit = {}, onSkip = {},
        )
    }
}
