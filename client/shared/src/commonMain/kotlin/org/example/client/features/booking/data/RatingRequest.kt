package org.example.client.features.booking.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatingRequest(
    @SerialName("score") val score: Int,
)
