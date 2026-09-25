package org.example.client.features.booking.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.client.features.schedule.data.InstructorSummary

@Serializable
data class RatingDto(
    @SerialName("id") val id: String,
    @SerialName("booking_id") val bookingId: String,
    @SerialName("instructor") val instructor: InstructorSummary,
    @SerialName("score") val score: Int,
    @SerialName("created_at") val createdAt: String,
)
