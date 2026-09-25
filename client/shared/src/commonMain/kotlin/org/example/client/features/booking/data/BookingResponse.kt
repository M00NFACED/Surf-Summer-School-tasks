package org.example.client.features.booking.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.client.features.schedule.data.TrainingSlotSummary

@Serializable
data class BookingResponse(
    @SerialName("id") val id: String,
    @SerialName("slot") val slot: TrainingSlotSummary,
    @SerialName("status") val status: String,
    @SerialName("equipment") val equipment: EquipmentSelectionsPayload,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("cancel_deadline") val cancelDeadline: String,
    @SerialName("cancellation") val cancellation: CancellationInfoDto? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
    @SerialName("rating") val rating: RatingDto? = null,
)
