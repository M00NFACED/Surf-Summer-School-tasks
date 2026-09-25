package org.example.client.features.schedule.domain

import kotlinx.datetime.Instant

data class TrainingSlotItem(
    val id: String,
    val startsAt: Instant,
    val endsAt: Instant,
    val format: TrainingFormat,
    val zone: String,
    val address: String,
    val instructor: Instructor,
    val capacity: Int,
    val availablePlaces: Int,
    val status: SlotStatus,
    val cancellationReason: String? = null,
) {
    val isAvailable: Boolean
        get() = status == SlotStatus.AVAILABLE && availablePlaces > 0
}
