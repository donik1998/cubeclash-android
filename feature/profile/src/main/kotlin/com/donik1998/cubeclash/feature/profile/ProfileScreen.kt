package com.donik1998.cubeclash.feature.profile

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.component.SegmentedControl
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.ThemeMode
import com.donik1998.cubeclash.core.model.TimerStyle

/**
 * Layer A: the only place that knows about state. It owns the [ProfileViewModel], subscribes to it,
 * and hands plain values plus navigation callbacks down to the stateless [ProfileScreen]. Share is
 * wired here to the platform share sheet (spec §11.5 — client-side only, a plain URL string); the
 * three destination lambdas are passed in by the app's nav graph.
 */
@Composable
fun ProfileRoute(
    onFriends: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ProfileScreen(
        uiState = uiState,
        onFriends = onFriends,
        onShare = {
            val id = (uiState as? ProfileUiState.Success)?.profile?.id
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "https://cubeclash.app/u/${id.orEmpty()}")
            }
            ContextCompat.startActivity(
                context,
                Intent.createChooser(intent, "Share profile"),
                null,
            )
        },
        onSettings = onSettings,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

/**
 * Layer B: the pure, testable screen — Figma frame 47:158. The serif "Profile" header plus an
 * exhaustive `when` over the aggregate's three states: a skeleton while loading, an error with a
 * retry, or the resolved [ProfileContent]. It imports no Hilt and no ViewModel, so every state is
 * reachable from a preview or a test with fixed data.
 */
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onFriends: () -> Unit,
    onShare: () -> Unit,
    onSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = "Profile",
            style = CubeClashTheme.typography.h1,
            color = CubeClashTheme.colors.textPrimary,
            modifier = Modifier.padding(top = Spacing.md),
        )

        when (uiState) {
            is ProfileUiState.Loading -> ProfileSkeleton()

            is ProfileUiState.Failure -> ProfileError(
                message = uiState.message,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )

            is ProfileUiState.Success -> ProfileContent(
                profile = uiState.profile,
                onFriends = onFriends,
                onShare = onShare,
                onSettings = onSettings,
            )
        }
    }
}

/** The loading placeholder: a hero block, three tile blocks and three menu-row blocks (spec §8). */
@Composable
private fun ProfileSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SkeletonBlock(height = 160.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            repeat(3) { SkeletonBlock(height = 72.dp, modifier = Modifier.weight(1f)) }
        }
        SkeletonBlock(height = 186.dp)
    }
}

@Composable
private fun SkeletonBlock(height: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(CubeClashTheme.colors.surfaceAlt, Radius.card),
    )
}

@Composable
private fun ProfileError(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message ?: "Couldn't load your profile.",
            style = CubeClashTheme.typography.body,
            color = CubeClashTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        CubeSecondaryButton(text = "Retry", onClick = onRetry)
    }
}

/**
 * The extracted settings form (theme / timer / inspection / haptics / sign-out), driven by
 * [SettingsViewModel]. It was the old Profile screen's whole reason to exist; the new profile
 * design pushes it behind the Settings menu row (spec §11.7), so it is now a sub-screen with a
 * back affordance rather than a tab. [onBack] pops it off the stack.
 */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(Spacing.xxl)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = CubeClashTheme.colors.textPrimary,
                )
            }
            Text(
                text = "Settings",
                style = CubeClashTheme.typography.h1,
                color = CubeClashTheme.colors.textPrimary,
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

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Loading · Light", showBackground = true)
@Preview(name = "Loading · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileScreenLoadingPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas)) {
            ProfileScreen(ProfileUiState.Loading, {}, {}, {}, {})
        }
    }
}

@Preview(name = "Error · Light", showBackground = true)
@Preview(name = "Error · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileScreenErrorPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas)) {
            ProfileScreen(ProfileUiState.Failure("The network is unreachable."), {}, {}, {}, {})
        }
    }
}

@Preview(name = "Success · Light", showBackground = true)
@Preview(name = "Success · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun ProfileScreenSuccessPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas)) {
            ProfileScreen(ProfileUiState.Success(sampleProfile()), {}, {}, {}, {})
        }
    }
}
