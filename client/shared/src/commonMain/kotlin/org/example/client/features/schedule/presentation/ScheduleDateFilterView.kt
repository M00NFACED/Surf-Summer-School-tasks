package org.example.client.features.schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.example.client.features.schedule.domain.ScheduleFilter

@Composable
fun ScheduleDateFilterView(
    filter: ScheduleFilter,
    onPeriodSelected: (Instant, Instant) -> Unit,
) {
    var fromText by remember(filter.from) { mutableStateOf(filter.from?.toString()?.take(10).orEmpty()) }
    var toText by remember(filter.to) { mutableStateOf(filter.to?.toString()?.take(10).orEmpty()) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fromText,
                onValueChange = { fromText = it.take(10) },
                label = { Text("С") },
                placeholder = { Text("ГГГГ-ММ-ДД") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = toText,
                onValueChange = { toText = it.take(10) },
                label = { Text("По") },
                placeholder = { Text("ГГГГ-ММ-ДД") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Button(
            onClick = {
                val from = parseDate(fromText, endOfDay = false)
                val to = parseDate(toText, endOfDay = true)
                if (from != null && to != null && to > from) onPeriodSelected(from, to)
            },
            enabled = fromText.length == 10 && toText.length == 10,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Применить период")
        }
    }
}

private fun parseDate(value: String, endOfDay: Boolean): Instant? = runCatching {
    Instant.parse("${value}T${if (endOfDay) "23:59:59" else "00:00:00"}Z")
}.getOrNull()
