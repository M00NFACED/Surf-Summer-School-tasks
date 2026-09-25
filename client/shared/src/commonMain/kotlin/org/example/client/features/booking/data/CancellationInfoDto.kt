package org.example.client.features.booking.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CancellationInfoDto(
    @SerialName("status") val status: String,
    @SerialName("reason") val reason: String? = null,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
)
