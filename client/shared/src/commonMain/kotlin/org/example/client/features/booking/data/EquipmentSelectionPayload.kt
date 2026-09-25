package org.example.client.features.booking.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EquipmentSelectionPayload(
    @SerialName("source") val source: String,
    @SerialName("option_id") val optionId: String? = null,
)
