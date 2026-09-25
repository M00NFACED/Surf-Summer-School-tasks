package org.example.client.features.schedule.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant

@Composable
fun ScheduleOfflineBanner(cachedAtEpochMillis: Long? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        val cacheLabel = cachedAtEpochMillis?.let {
            ", сохранено ${formatScheduleDateTime(Instant.fromEpochMilliseconds(it))}"
        }.orEmpty()
        Text(
            text = "Данные могут быть неактуальны (оффлайн-режим)$cacheLabel",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
