package org.example.client.features.schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import org.example.client.core.theme.wave
import org.example.client.core.ui.WaveFilterChip
import org.example.client.core.ui.WaveSectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleDateFilterSection(
    from: Instant?,
    to: Instant?,
    onPeriodSelected: (Instant?, Instant?) -> Unit,
) {
    var fromText by remember(from) { mutableStateOf(from?.let(::formatScheduleDate).orEmpty()) }
    var toText by remember(to) { mutableStateOf(to?.let(::formatScheduleDate).orEmpty()) }
    var pickerTarget by remember { mutableStateOf<DateTarget?>(null) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WaveSectionTitle("Дата старта")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WaveFilterChip("Сегодня", selected = false) {
                onPeriodSelected(today.atStartOfDay(), today.plus(1, DateTimeUnit.DAY).atStartOfDay())
            }
            WaveFilterChip("Эта неделя", selected = false) { onPeriodSelected(today.atStartOfDay(), today.plus(7, DateTimeUnit.DAY).atStartOfDay()) }
            WaveFilterChip("Выходные", selected = false) {
                val saturday = today.nextSaturday()
                onPeriodSelected(saturday.atStartOfDay(), saturday.plus(2, DateTimeUnit.DAY).atStartOfDay())
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScheduleDateField(
                value = fromText,
                label = "Дата начала",
                onTextChange = { text ->
                    fromText = text
                    onPeriodSelected(parseScheduleDate(text), parseScheduleDate(toText))
                },
                onCalendarClick = { pickerTarget = DateTarget.FROM },
            )
            ScheduleDateField(
                value = toText,
                label = "Дата окончания",
                onTextChange = { text ->
                    toText = text
                    onPeriodSelected(parseScheduleDate(fromText), parseScheduleDate(text))
                },
                onCalendarClick = { pickerTarget = DateTarget.TO },
            )
        }
    }

    val target = pickerTarget
    if (target != null) {
        val initial = (if (target == DateTarget.FROM) from else to)?.toEpochMilliseconds()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { pickerTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    val picked = pickerState.selectedDateMillis?.let { Instant.fromEpochMilliseconds(it) }
                    if (picked != null) {
                        if (target == DateTarget.FROM) {
                            fromText = formatScheduleDate(picked)
                            onPeriodSelected(picked.startOfDay(), parseScheduleDate(toText))
                        } else {
                            toText = formatScheduleDate(picked)
                            onPeriodSelected(parseScheduleDate(fromText), picked.startOfDay())
                        }
                    }
                    pickerTarget = null
                }) { Text("Готово", color = MaterialTheme.wave.accent) }
            },
            dismissButton = { TextButton(onClick = { pickerTarget = null }) { Text("Отмена") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.ScheduleDateField(
    value: String,
    label: String,
    onTextChange: (String) -> Unit,
    onCalendarClick: () -> Unit,
) {
    val colors = MaterialTheme.wave
    OutlinedTextField(
        value = value,
        onValueChange = onTextChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.border,
            focusedContainerColor = colors.cardInner,
            unfocusedContainerColor = colors.cardInner,
        ),
        trailingIcon = {
            IconButton(onClick = onCalendarClick) {
                Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату", tint = colors.accent)
            }
        },
        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
    )
}

private enum class DateTarget { FROM, TO }

private fun LocalDate.nextSaturday(): LocalDate {
    val offset = (6 - dayOfWeek.ordinal + 7) % 7
    return if (offset == 0) this else plus(offset, DateTimeUnit.DAY)
}

private fun LocalDate.atStartOfDay(): Instant = atStartOfDayIn(TimeZone.currentSystemDefault())

private fun Instant.startOfDay(): Instant =
    toLocalDateTime(TimeZone.currentSystemDefault()).date.atStartOfDayIn(TimeZone.currentSystemDefault())
