package org.example.client.features.booking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WaveBadge
import org.example.client.core.ui.WaveBadgeRow
import org.example.client.core.ui.WavePrimaryButton
import org.example.client.core.ui.WaveSlotImage
import org.example.client.features.booking.domain.EquipmentType
import org.example.client.features.booking.domain.SlotDetailsItem
import org.example.client.features.schedule.presentation.badgeTone
import org.example.client.features.schedule.presentation.formatSlotCardDate
import org.example.client.features.schedule.presentation.formatSlotTimeRange

@Composable
internal fun SlotDetailContent(
    details: SlotDetailsItem,
    onBook: () -> Unit,
    submitting: Boolean = false,
) {
    val colors = MaterialTheme.wave
    val slot = details.slot
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        WaveSlotImage(modifier = Modifier.fillMaxWidth().height(200.dp))
        WaveBadgeRow {
            WaveBadge(slot.format.displayName, slot.format.badgeTone())
        }
        Text(
            text = formatSlotCardDate(slot.startsAt),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        Text(
            text = formatSlotTimeRange(slot.startsAt, slot.endsAt),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
        )
        Text("Инструктор: ${slot.instructor.fullName}", style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        Text("Адрес: ${slot.address}", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        InfoCard(details)
        Text(
            text = "Оплата на месте: наличные или перевод",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        WavePrimaryButton(
            text = "Записаться",
            onClick = onBook,
            enabled = details.isAvailable && !submitting,
            loading = submitting,
        )
    }
}

@Composable
private fun InfoCard(details: SlotDetailsItem) {
    val colors = MaterialTheme.wave
    val slot = details.slot
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.card).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        InfoRow("Свободно мест", "${slot.availablePlaces} из ${slot.capacity}")
        RentalRow(details)
        if (slot.status == org.example.client.features.schedule.domain.SlotStatus.CANCELLED) {
            InfoRow("Статус", "Отменена скалодромом")
        } else if (slot.availablePlaces == 0) {
            InfoRow("Мест нет", "")
        }
    }
}

@Composable
private fun RentalRow(details: SlotDetailsItem) {
    val available = details.equipmentOptions
        .filter { it.isAvailable && it.type == EquipmentType.CLIMBING_SHOES }
        .sumOf { it.availableQuantity }
    InfoRow(
        label = "Прокат скальников (доступно $available шт.)",
        value = if (available > 0) "Есть" else "Нет",
    )
}

@Composable
internal fun InfoRow(label: String, value: String) {
    val colors = MaterialTheme.wave
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
        if (value.isNotEmpty()) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        }
    }
}
