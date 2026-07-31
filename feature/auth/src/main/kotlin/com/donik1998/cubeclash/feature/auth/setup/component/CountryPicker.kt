package com.donik1998.cubeclash.feature.auth.setup.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.CountryNames

/**
 * The common country picks, shown as one-tap chips. This is the whole picker on Android: the
 * flag beside a name is a display concern, and the ten below cover the overwhelming majority of
 * cubers. Anyone outside them can add a country later from Settings — country is optional here,
 * so no one is boxed out by not being on this list.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CountryPicker(
    selected: String?,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        for (code in CommonCountries) {
            CubeChip(
                label = "${flagEmoji(code)} ${CountryNames.displayName(code) ?: code}",
                selected = selected == code,
                onClick = { onToggle(code) },
            )
        }
        // A country picked elsewhere that is not one of the common ten still shows here, selected.
        if (selected != null && selected !in CommonCountries) {
            CubeChip(
                label = "${flagEmoji(selected)} ${CountryNames.displayName(selected) ?: selected}",
                selected = true,
                onClick = { onToggle(selected) },
            )
        }
    }
}

/** The ISO alpha-2 codes shown as chips, in the same order the other clients use. */
private val CommonCountries = listOf("GB", "US", "DE", "FR", "BR", "JP", "CN", "IN", "UZ", "AU")

/**
 * Turns an ISO 3166-1 alpha-2 code into its flag emoji by mapping each letter to its regional
 * indicator symbol. A malformed code yields the white flag rather than mojibake.
 */
internal fun flagEmoji(code: String): String {
    val upper = code.trim().uppercase()
    if (upper.length != 2 || upper.any { it !in 'A'..'Z' }) return "🏳️"
    val base = 0x1F1E6
    val first = base + (upper[0] - 'A')
    val second = base + (upper[1] - 'A')
    return String(Character.toChars(first)) + String(Character.toChars(second))
}
