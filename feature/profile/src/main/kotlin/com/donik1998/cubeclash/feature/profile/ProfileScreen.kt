package com.donik1998.cubeclash.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.CubeCard
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.component.SegmentedControl
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.ThemeMode
import com.donik1998.cubeclash.core.model.TimerStyle

@Composable
fun ProfileRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = "You",
            style = CubeClashTheme.typography.h1,
            color = CubeClashTheme.colors.textPrimary,
            modifier = Modifier.padding(top = Spacing.md),
        )

        CubeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = uiState.user?.displayName ?: "Not signed in",
                style = CubeClashTheme.typography.h2,
                color = CubeClashTheme.colors.textPrimary,
            )
            Text(
                text = listOfNotNull(uiState.user?.country, "${uiState.user?.elo ?: 0} Elo").joinToString(" · "),
                style = CubeClashTheme.typography.small,
                color = CubeClashTheme.colors.textSecondary,
            )
        }

        SectionHeader("Appearance")
        SegmentedControl(
            options = ThemeMode.entries,
            selected = uiState.settings.themeMode,
            onSelect = viewModel::setThemeMode,
            label = { it.label },
        )

        SectionHeader("Timer")
        SegmentedControl(
            options = TimerStyle.entries,
            selected = uiState.settings.timerStyle,
            onSelect = viewModel::setTimerStyle,
            label = { it.label },
        )

        SettingToggle(
            label = "15-second inspection",
            checked = uiState.settings.inspectionEnabled,
            onCheckedChange = viewModel::setInspection,
        )
        SettingToggle(
            label = "Haptics",
            checked = uiState.settings.hapticsEnabled,
            onCheckedChange = viewModel::setHaptics,
        )

        CubeSecondaryButton(
            text = "Sign out",
            onClick = { viewModel.signOut() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xl),
        )
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = CubeClashTheme.typography.body, color = CubeClashTheme.colors.textPrimary)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
