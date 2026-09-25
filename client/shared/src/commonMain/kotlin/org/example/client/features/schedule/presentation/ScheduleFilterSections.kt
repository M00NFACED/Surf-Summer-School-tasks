package org.example.client.features.schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.core.ui.WaveFilterChip
import org.example.client.core.ui.WaveSectionTitle
import org.example.client.core.ui.WaveSwitchRow
import org.example.client.features.schedule.domain.Instructor
import org.example.client.features.schedule.domain.TrainingFormat

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FormatFilterSection(
    formats: List<TrainingFormat>,
    selected: TrainingFormat?,
    onSelect: (TrainingFormat?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WaveSectionTitle("Формат тренировки")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            formats.forEach { format ->
                WaveFilterChip(format.displayName, selected = format == selected) { onSelect(format) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InstructorFilterSection(
    instructors: List<Instructor>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WaveSectionTitle("Инструктор")
        if (instructors.isEmpty()) {
            WaveFilterChip("Загрузка инструкторов…", selected = false, onClick = {})
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                instructors.forEach { instructor ->
                    WaveFilterChip(instructor.fullName, selected = instructor.id == selectedId) {
                        onSelect(if (instructor.id == selectedId) null else instructor.id)
                    }
                }
            }
        }
    }
}

@Composable
internal fun OnlyAvailableSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    WaveSwitchRow(
        title = "Только со свободными местами",
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}
