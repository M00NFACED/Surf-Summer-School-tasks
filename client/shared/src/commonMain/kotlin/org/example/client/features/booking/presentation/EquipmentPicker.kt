package org.example.client.features.booking.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var rentalMode by remember(type) { mutableStateOf(selection is EquipmentSelection.Rental) }
    val selectedOptionId = (selection as? EquipmentSelection.Rental)?.optionId

    LaunchedEffect(selection) {
        rentalMode = selection is EquipmentSelection.Rental
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, modifier = Modifier.padding(horizontal = 16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = !rentalMode,
                onClick = {
                    rentalMode = false
                    onSelection(EquipmentSelection.Own)
                },
                label = { Text("Свои") },
                modifier = Modifier.heightIn(min = 48.dp),
            )
            FilterChip(
                selected = rentalMode,
                onClick = { rentalMode = true },
                label = { Text("Прокат") },
                enabled = available.isNotEmpty(),
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
        if (rentalMode) {
            if (available.isEmpty()) {
                Text("Прокат недоступен", modifier = Modifier.padding(horizontal = 16.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    available.forEach { option ->
                        FilterChip(
                            selected = selectedOptionId == option.id,
                            onClick = { onSelection(EquipmentSelection.Rental(option.id)) },
                            label = { Text("${option.size ?: "—"} · ${option.availableQuantity} шт.") },
                            modifier = Modifier.heightIn(min = 48.dp),
                        )
                    }
                }
            }
        }
    }
}
