package com.donik1998.cubeclash.feature.profile.friends.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.component.CubeCard
import com.donik1998.cubeclash.core.designsystem.component.CubePrimaryButton
import com.donik1998.cubeclash.core.designsystem.component.CubeTextField
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing

/**
 * The "Add a friend" card — a name/email field plus a Send-invite button. It owns only the field's
 * draft text (local UI state, not app state), and clears it once a submit succeeds so the row the
 * fake appends is visibly the friend just invited, not a name still sitting in the box.
 *
 * [onInvite] resolves server-side to a user by handle or email; validation (e.g. an empty query)
 * comes back as a transient message the screen surfaces, so this card does no validation itself.
 */
@Composable
fun InviteCard(
    onInvite: (String) -> Unit,
    submitting: Boolean,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    val submit = {
        onInvite(query)
        query = ""
    }

    CubeCard(modifier = modifier.fillMaxWidth()) {
        CubeTextField(
            value = query,
            onValueChange = { query = it },
            label = "Add a friend",
            placeholder = "Name or email",
            keyboardType = KeyboardType.Email,
        )
        CubePrimaryButton(
            text = "Send invite",
            onClick = submit,
            loading = submitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.md),
        )
    }
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Invite card · Light", showBackground = true)
@Preview(name = "Invite card · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun InviteCardPreview() {
    CubeClashTheme {
        Column(
            modifier = Modifier
                .background(CubeClashTheme.colors.canvas)
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            InviteCard(onInvite = {}, submitting = false)
        }
    }
}
