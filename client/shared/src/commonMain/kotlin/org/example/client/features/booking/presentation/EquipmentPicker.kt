package org.example.client.features.booking.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WaveFilterChip
import org.example.client.features.booking.domain.EquipmentOption
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.domain.EquipmentType

@Composable
fun EquipmentPicker(
    type: EquipmentType,
    options: List<EquipmentOption>,
    selection: EquipmentSelection?,
    onSelection: (EquipmentSelection?) -> Unit,
) {
    val colors = MaterialTheme.wave
    val available = options.filter { it.type == type && it.isAvailable }
    var rentalMode by remember(type) { mutableStateOf(selection is EquipmentSelection.Rental) }
    val selectedOptionId = (selection as? EquipmentSelection.Rental)?.optionId

    LaunchedEffect(selection) { rentalMode = selection is EquipmentSelection.Rental }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WaveFilterChip("Своя", selected = !rentalMode) {
                rentalMode = false
                onSelection(EquipmentSelection.Own)
            }
            WaveFilterChip("Прокат", selected = rentalMode) { rentalMode = true }
        }
        if (rentalMode) {
            if (available.isEmpty()) {
                Text(
                    text = "Прокат сейчас недоступен",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    available.forEach { option ->
                        WaveFilterChip(
                            text = "${option.size ?: "—"} · ${option.availableQuantity} шт.",
                            selected = selectedOptionId == option.id,
                        ) { onSelection(EquipmentSelection.Rental(option.id)) }
                    }
                }
            }
        }
    }
}
