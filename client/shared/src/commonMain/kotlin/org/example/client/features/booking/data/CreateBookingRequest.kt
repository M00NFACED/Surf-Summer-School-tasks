package org.example.client.features.booking.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBookingRequest(
    @SerialName("slot_id") val slotId: String,
    @SerialName("equipment") val equipment: EquipmentSelectionsPayload,
    @SerialName("payment_method") val paymentMethod: String,
)
