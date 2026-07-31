package com.donik1998.cubeclash.feature.race.tournament.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.donik1998.cubeclash.core.designsystem.component.CubePrimaryButton
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.feature.race.tournament.RegisterButtonState

/**
 * The register affordance, driven entirely by a pre-derived [RegisterButtonState] (the derivation
 * itself is a pure, unit-tested function on the model — this component only renders it). The four
 * states are visually distinct on purpose: a done "Registered" must never read as a locked-out
 * "Full".
 *
 * [registering] shows an in-flight spinner on the primary action; [registerError] surfaces a
 * rejection under the button without collapsing the loaded detail.
 */
@Composable
fun RegisterButton(
    state: RegisterButtonState,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
    registering: Boolean = false,
    registerError: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        when (state) {
            RegisterButtonState.REGISTER -> CubePrimaryButton(
                text = "Register",
                onClick = onRegister,
                loading = registering,
                modifier = Modifier.fillMaxWidth(),
            )

            RegisterButtonState.REGISTERED -> CubeSecondaryButton(
                text = "Registered ✓",
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )

            RegisterButtonState.FULL -> CubeSecondaryButton(
                text = "Bracket full",
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )

            RegisterButtonState.FINISHED -> CubeSecondaryButton(
                text = "Tournament finished",
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )

            // No affordance for an inert / unrecognised card — render nothing.
            RegisterButtonState.UNAVAILABLE -> Unit
        }

        registerError?.let {
            Text(
                text = it,
                style = CubeClashTheme.typography.small,
                color = CubeClashTheme.colors.danger,
            )
        }
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Register states · Light", showBackground = true)
@Preview(name = "Register states · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun RegisterButtonPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                RegisterButton(RegisterButtonState.REGISTER, {})
                RegisterButton(RegisterButtonState.REGISTERED, {})
                RegisterButton(RegisterButtonState.FULL, {})
                RegisterButton(RegisterButtonState.FINISHED, {})
                RegisterButton(
                    RegisterButtonState.REGISTER,
                    {},
                    registerError = "This bracket is full.",
                )
            }
        }
    }
}
