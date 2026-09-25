package org.example.client.features.schedule.domain

import kotlinx.datetime.Instant

data class ScheduleFilter(
    val from: Instant? = null,
    val to: Instant? = null,
    val format: TrainingFormat? = null,
    val instructorId: String? = null,
)
