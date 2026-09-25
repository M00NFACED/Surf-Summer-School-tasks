package org.example.client.features.schedule.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.features.schedule.domain.TrainingSlotItem

@Composable
fun ScheduleCard(item: TrainingSlotItem, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.isAvailable) { onClick(item.id) },
        colors = CardDefaults.cardColors(
            containerColor = if (item.isAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(formatScheduleDate(item.startsAt), style = MaterialTheme.typography.titleMedium)
            Text("${formatScheduleDateTime(item.startsAt)} — ${formatScheduleDateTime(item.endsAt)}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormatBadge(item)
                if (item.status == org.example.client.features.schedule.domain.SlotStatus.CANCELLED) {
                    Text("Отменена скалодромом", color = MaterialTheme.colorScheme.error)
                } else if (item.availablePlaces == 0) {
                    Text("Мест нет", color = MaterialTheme.colorScheme.error)
                }
            }
            Text(item.instructor.fullName, style = MaterialTheme.typography.bodyLarge)
            Text(item.address, style = MaterialTheme.typography.bodySmall)
            Text("Свободно мест: ${item.availablePlaces} из ${item.capacity}")
        }
    }
}

@Composable
private fun FormatBadge(item: TrainingSlotItem) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(item.format.displayName, style = MaterialTheme.typography.labelMedium)
        }
    }
}
