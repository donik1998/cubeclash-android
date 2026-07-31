package com.donik1998.cubeclash.feature.auth.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.donik1998.cubeclash.core.designsystem.component.CubePrimaryButton
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.feature.auth.AuthMode
import com.donik1998.cubeclash.feature.auth.welcome.component.CubeMark
import com.donik1998.cubeclash.feature.auth.welcome.component.WelcomeHighlights

/**
 * Layer A: the landing screen holds no state, so the route is a straight pass-through to the pure
 * screen. Both actions carry the [AuthMode] to preselect on the auth form, so a "Create an account"
 * tap lands on Sign up and "I already have one" lands on Log in.
 */
@Composable
fun WelcomeRoute(
    onContinue: (AuthMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    WelcomeScreen(onContinue = onContinue, modifier = modifier)
}

/**
 * Layer B: pure and testable. One screen, not a carousel — the app is a timer and a race, and
 * three swipes of marketing before someone can time a solve is three swipes too many.
 */
@Composable
fun WelcomeScreen(
    onContinue: (AuthMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        CubeMark()
        Spacer(Modifier.height(Spacing.xxl))
        Text(
            text = "CubeClash",
            style = CubeClashTheme.typography.h1,
            color = CubeClashTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "A WCA timer that races. Same scramble, same moment, " +
                "someone else on the other end.",
            style = CubeClashTheme.typography.body,
            color = CubeClashTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.xxl))
        WelcomeHighlights()

        Spacer(Modifier.weight(1f))

        CubePrimaryButton(
            text = "Create an account",
            onClick = { onContinue(AuthMode.SIGN_UP) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.sm))
        CubeSecondaryButton(
            text = "I already have one",
            onClick = { onContinue(AuthMode.SIGN_IN) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Welcome · Light", showBackground = true)
@Preview(name = "Welcome · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun WelcomePreview() {
    CubeClashTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            WelcomeScreen(onContinue = {})
        }
    }
}
