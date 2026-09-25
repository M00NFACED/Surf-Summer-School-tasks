package org.example.client.features.schedule.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import org.example.client.features.schedule.domain.ScheduleFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDateFilterView(
    filter: ScheduleFilter,
    onPeriodSelected: (Instant?, Instant?) -> Unit,
) {
    var fromText by remember(filter.from) { mutableStateOf(filter.from?.let(::formatScheduleDate).orEmpty()) }
    var toText by remember(filter.to) { mutableStateOf(filter.to?.let(::formatScheduleDate).orEmpty()) }
    var pickerTarget by remember { mutableStateOf<DatePickerTarget?>(null) }
    val initialPickerMillis = when (pickerTarget) {
        DatePickerTarget.FROM -> filter.from?.toEpochMilliseconds()
        DatePickerTarget.TO -> filter.to?.toEpochMilliseconds()
        null -> null
    }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialPickerMillis)

    LaunchedEffect(pickerTarget) {
        pickerState.selectedDateMillis = initialPickerMillis
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fromText,
                onValueChange = {},
                label = { Text("С") },
                placeholder = { Text("ДД.ММ.ГГГГ") },
                singleLine = true,
                readOnly = true,
                modifier = Modifier.weight(1f).clickable { pickerTarget = DatePickerTarget.FROM },
            )
            OutlinedTextField(
                value = toText,
                onValueChange = {},
                label = { Text("По") },
                placeholder = { Text("ДД.ММ.ГГГГ") },
                singleLine = true,
                readOnly = true,
                modifier = Modifier.weight(1f).clickable { pickerTarget = DatePickerTarget.TO },
            )
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = false,
                onClick = { quickPeriod(QuickPeriod.TODAY).let { (from, to) -> onPeriodSelected(from, to) } },
                label = { Text("Сегодня") },
            )
            FilterChip(
                selected = false,
                onClick = { quickPeriod(QuickPeriod.WEEK).let { (from, to) -> onPeriodSelected(from, to) } },
                label = { Text("Эта неделя") },
            )
            FilterChip(
                selected = false,
                onClick = { quickPeriod(QuickPeriod.WEEKEND).let { (from, to) -> onPeriodSelected(from, to) } },
                label = { Text("Выходные") },
            )
        }
        TextButton(
            onClick = {
                val from = fromText.takeIf(String::isNotBlank)?.let(::parseDate)
                val to = toText.takeIf(String::isNotBlank)?.let(::parseDate)
                if (from != null || to != null) onPeriodSelected(from, to)
            },
            enabled = fromText.isNotBlank() || toText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Применить период")
        }
    }

    if (pickerTarget != null) {
        DatePickerDialog(
            onDismissRequest = { pickerTarget = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.fromEpochMilliseconds(millis)
                            when (pickerTarget) {
                                DatePickerTarget.FROM -> fromText = formatScheduleDate(selected)
                                DatePickerTarget.TO -> toText = formatScheduleDate(selected)
                                null -> Unit
                            }
                        }
                        pickerTarget = null
                    },
                ) { Text("Применить") }
            },
            dismissButton = { TextButton(onClick = { pickerTarget = null }) { Text("Отмена") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private enum class DatePickerTarget { FROM, TO }

private enum class QuickPeriod { TODAY, WEEK, WEEKEND }

private fun parseDate(value: String): Instant? {
    val parts = value.split('.')
    if (parts.size != 3) return null
    return runCatching {
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()
        startOfDay(LocalDate(year, month, day), TimeZone.currentSystemDefault())
    }.getOrNull()
}

private fun quickPeriod(kind: QuickPeriod): Pair<Instant, Instant> {
    val zone = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(zone).date
    val start = when (kind) {
        QuickPeriod.TODAY -> today
        QuickPeriod.WEEK -> today.minus(today.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal, DateTimeUnit.DAY)
        QuickPeriod.WEEKEND -> today.plus(
            (DayOfWeek.SATURDAY.ordinal - today.dayOfWeek.ordinal + 7) % 7,
            DateTimeUnit.DAY,
        )
    }
    val length = when (kind) {
        QuickPeriod.TODAY -> 1
        QuickPeriod.WEEK -> 7
        QuickPeriod.WEEKEND -> 2
    }
    return startOfDay(start, zone) to startOfDay(start.plus(length, DateTimeUnit.DAY), zone)
}

private fun startOfDay(date: LocalDate, zone: TimeZone): Instant =
    LocalDateTime(date, LocalTime(0, 0)).toInstant(zone)
