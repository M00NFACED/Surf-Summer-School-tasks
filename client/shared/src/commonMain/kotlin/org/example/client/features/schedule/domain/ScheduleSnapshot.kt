package org.example.client.features.schedule.domain

import kotlinx.datetime.Instant

data class ScheduleSnapshot(
    val from: Instant,
    val to: Instant,
    val items: List<TrainingSlotItem>,
    val isOffline: Boolean = false,
    val cachedAtEpochMillis: Long? = null,
)
