package org.example.client.features.schedule.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrainingSlotSummary(
    @SerialName("id") val id: String,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    @SerialName("format") val format: String,
    @SerialName("zone") val zone: String,
    @SerialName("address") val address: String,
    @SerialName("instructor") val instructor: InstructorSummary,
    @SerialName("capacity") val capacity: Int,
    @SerialName("available_places") val availablePlaces: Int,
    @SerialName("status") val status: String,
    @SerialName("cancellation_reason") val cancellationReason: String? = null,
)
