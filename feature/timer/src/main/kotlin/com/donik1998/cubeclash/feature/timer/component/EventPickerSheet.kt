package com.donik1998.cubeclash.feature.timer.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.donik1998.cubeclash.core.designsystem.component.CubeTextField
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.EventGroup
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.ui.EventIcon

/**
 * Grouped, searchable on the short names cubers actually type, and **nothing is disabled** —
 * an event without a scrambler is still fully timeable, so it lists normally. A greyed-out row
 * would say *"you cannot use this"*, which isn't true.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPickerSheet(
    selected: WcaEvent,
    onSelect: (WcaEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CubeClashTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            CubeTextField(
                value = query,
                onValueChange = { query = it },
                label = "Event",
                placeholder = "3x3, OH, mega…",
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                EventGroup.entries.forEach { group ->
                    val events = WcaEvent.entries.filter { it.group == group && it.matches(query) }
                    if (events.isEmpty()) return@forEach

                    item(key = "header-${group.name}") {
                        SectionHeader(group.label, modifier = Modifier.padding(top = Spacing.sm))
                    }
                    items(events, key = { it.id }) { event ->
                        EventRow(event = event, selected = event == selected, onClick = { onSelect(event) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: WcaEvent, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        EventIcon(event = event, active = selected)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.displayName,
                style = CubeClashTheme.typography.bodyStrong,
                color = if (selected) CubeClashTheme.colors.brandPrimary else CubeClashTheme.colors.textPrimary,
            )
            Text(
                text = "${event.format.label} · ${event.aliases.first()}",
                style = CubeClashTheme.typography.caption,
                color = CubeClashTheme.colors.textMuted,
            )
        }
    }
}
