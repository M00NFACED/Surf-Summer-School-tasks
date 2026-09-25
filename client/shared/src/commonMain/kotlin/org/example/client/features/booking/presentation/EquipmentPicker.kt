package org.example.client.features.booking.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.features.booking.domain.EquipmentOption
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.domain.EquipmentType

@Composable
fun EquipmentPicker(
    title: String,
    type: EquipmentType,
    options: List<EquipmentOption>,
    selection: EquipmentSelection?,
    onSelection: (EquipmentSelection) -> Unit,
) {
    val available = options.filter { it.type == type && it.isAvailable }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, modifier = Modifier.padding(horizontal = 16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selection == EquipmentSelection.Own,
                onClick = { onSelection(EquipmentSelection.Own) },
                label = { Text("Свои") },
            )
            FilterChip(
                selected = selection is EquipmentSelection.Rental,
                onClick = { available.firstOrNull()?.let { onSelection(EquipmentSelection.Rental(it.id)) } },
                label = { Text("Прокат") },
                enabled = available.isNotEmpty(),
            )
        }
        if (available.isEmpty()) {
            Text("Прокат недоступен", modifier = Modifier.padding(horizontal = 16.dp))
        } else {
            available.forEach { option ->
                FilterChip(
                    selected = (selection as? EquipmentSelection.Rental)?.optionId == option.id,
                    onClick = { onSelection(EquipmentSelection.Rental(option.id)) },
                    label = { Text("${option.name} ${option.size.orEmpty()} · ${option.price.toInt()} ${option.currency}") },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
