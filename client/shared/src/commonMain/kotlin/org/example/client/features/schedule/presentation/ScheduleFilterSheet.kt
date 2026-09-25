package org.example.client.features.schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WavePrimaryButton
import org.example.client.features.schedule.domain.Instructor
import org.example.client.features.schedule.domain.ScheduleFilter
import org.example.client.features.schedule.domain.TrainingFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleFilterSheet(
    applied: ScheduleFilter,
    instructors: List<Instructor>,
    onDismiss: () -> Unit,
    onApply: (ScheduleFilter) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var draft by remember(applied) { mutableStateOf(applied) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Фильтры", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.wave.textPrimary)
                TextButton(onClick = { draft = ScheduleFilter() }) {
                    Text("Сбросить", color = MaterialTheme.wave.accent)
                }
            }
            ScheduleDateFilterSection(
                from = draft.from,
                to = draft.to,
                onPeriodSelected = { from, to -> draft = draft.copy(from = from, to = to) },
            )
            FormatFilterSection(
                formats = TrainingFormat.values().toList(),
                selected = draft.format,
                onSelect = { format -> draft = draft.copy(format = format) },
            )
            InstructorFilterSection(
                instructors = instructors,
                selectedId = draft.instructorId,
                onSelect = { id -> draft = draft.copy(instructorId = id) },
            )
            OnlyAvailableSwitch(
                checked = draft.onlyAvailable,
                onCheckedChange = { checked -> draft = draft.copy(onlyAvailable = checked) },
            )
            WavePrimaryButton(
                text = "Применить",
                onClick = { onApply(draft) },
                enabled = draft != applied,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
    }
}
