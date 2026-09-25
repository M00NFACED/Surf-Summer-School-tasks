package org.example.client.features.schedule.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.example.client.core.validation.UuidValidator
import org.example.client.features.schedule.domain.Instructor
import org.example.client.features.schedule.domain.ScheduleFilter
import org.example.client.features.schedule.domain.TrainingFormat

@Composable
fun ScheduleFiltersView(
    filter: ScheduleFilter,
    instructors: List<Instructor>,
    onFormatSelected: (TrainingFormat?) -> Unit,
    onInstructorSelected: (String?) -> Unit,
    onPeriodSelected: (Instant, Instant) -> Unit,
    onReset: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedInstructor = instructors.firstOrNull { it.id == filter.instructorId }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Фильтры", modifier = Modifier.padding(horizontal = 16.dp))
        ScheduleDateFilterView(filter, onPeriodSelected)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = filter.format == null,
                onClick = { onFormatSelected(null) },
                label = { Text("Все") },
            )
            TrainingFormat.values().forEach { format ->
                FilterChip(
                    selected = filter.format == format,
                    onClick = { onFormatSelected(format) },
                    label = { Text(format.displayName) },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = { expanded = true }) {
                Text(selectedInstructor?.fullName ?: "Все инструкторы")
            }
            TextButton(
                onClick = onReset,
                enabled = filter.format != null || filter.instructorId != null,
            ) {
                Text("Сбросить")
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Все инструкторы") },
                onClick = { expanded = false; onInstructorSelected(null) },
            )
            instructors.forEach { instructor ->
                DropdownMenuItem(
                    text = { Text(instructor.fullName) },
                    onClick = {
                        expanded = false
                        UuidValidator.normalize(instructor.id)?.let(onInstructorSelected)
                    },
                )
            }
        }
    }
}
