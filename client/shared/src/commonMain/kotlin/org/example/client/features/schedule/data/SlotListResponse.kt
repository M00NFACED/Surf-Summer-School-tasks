package org.example.client.features.schedule.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SlotListResponse(
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    @SerialName("items") val items: List<TrainingSlotSummary>,
)
