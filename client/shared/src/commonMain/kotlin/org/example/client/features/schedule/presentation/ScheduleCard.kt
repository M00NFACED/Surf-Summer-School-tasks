package org.example.client.features.schedule.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import org.example.client.core.ui.WaveBadgeTone
import org.example.client.core.ui.WaveSlotImage
import org.example.client.core.validation.UuidValidator
import org.example.client.features.schedule.domain.SlotStatus
import org.example.client.features.schedule.domain.TrainingFormat
import org.example.client.features.schedule.domain.TrainingSlotItem

@Composable
fun ScheduleCard(item: TrainingSlotItem, onClick: (String) -> Unit) {
    val colors = MaterialTheme.wave
    val blocked = !item.isAvailable || item.status == SlotStatus.CANCELLED
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.card)
            .clickable(enabled = !blocked) { UuidValidator.normalize(item.id)?.let(onClick) },
    ) {
        WaveSlotImage(
            style = item.format.artworkStyle(),
            seed = item.artworkSeed(),
            modifier = Modifier.padding(8.dp),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            WaveBadgeRow {
                WaveBadge(item.format.displayName, item.format.badgeTone())
                if (item.status == SlotStatus.CANCELLED) {
                    WaveBadge("Отменена скалодромом", WaveBadgeTone.YELLOW)
                }
            }
            Text(
                text = formatScheduleDateTime(item.startsAt),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
            Text(
                text = "Инструктор: ${item.instructor.fullName}",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
            )
            PlacesStrip(item)
        }
    }
}

@Composable
internal fun PlacesStrip(item: TrainingSlotItem) {
    val colors = MaterialTheme.wave
    val full = item.availablePlaces == 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (full) colors.chip else colors.cardInner)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (full) "Мест нет" else "Свободно мест",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
        )
        if (!full) {
            Text(
                text = "${item.availablePlaces} из ${item.capacity}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
            )
        }
    }
}

internal fun TrainingFormat.badgeTone(): WaveBadgeTone = when (this) {
    TrainingFormat.NOVICE_BOULDERING -> WaveBadgeTone.GREEN
    TrainingFormat.ROPE_ROUTES -> WaveBadgeTone.YELLOW
}
