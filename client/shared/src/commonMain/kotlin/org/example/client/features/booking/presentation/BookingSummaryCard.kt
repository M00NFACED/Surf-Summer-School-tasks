package org.example.client.features.booking.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WaveBadge
import org.example.client.core.ui.WaveBadgeRow
import org.example.client.core.ui.WaveCardColumn
import org.example.client.features.booking.domain.EquipmentType
import org.example.client.features.booking.domain.SlotDetailsItem
import org.example.client.features.schedule.presentation.badgeTone
import org.example.client.features.schedule.presentation.formatSlotCardDate
import org.example.client.features.schedule.presentation.formatSlotTimeRange

@Composable
internal fun BookingSummaryCard(details: SlotDetailsItem) {
    val colors = MaterialTheme.wave
    val slot = details.slot
    WaveCardColumn {
        Text(
            text = formatSlotCardDate(slot.startsAt),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        WaveBadgeRow {
            WaveBadge(slot.format.displayName, slot.format.badgeTone())
            Text(
                text = "Инструктор: ${slot.instructor.fullName}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
        Text(
            text = formatSlotTimeRange(slot.startsAt, slot.endsAt),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
}

@Composable
internal fun EquipmentRow(
    title: String,
    type: EquipmentType,
    details: SlotDetailsItem,
    selection: org.example.client.features.booking.domain.EquipmentSelection?,
    onSelection: (org.example.client.features.booking.domain.EquipmentSelection?) -> Unit,
) {
    val colors = MaterialTheme.wave
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Text(
                text = if (selection == null) "Не выбрано" else "Выбрано",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
        EquipmentPicker(
            type = type,
            options = details.equipmentOptions,
            selection = selection,
            onSelection = onSelection,
        )
    }
}
