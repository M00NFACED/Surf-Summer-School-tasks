package org.example.client.features.booking.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyBookingsResponse(
    @SerialName("active") val active: List<BookingResponse>,
    @SerialName("history") val history: List<BookingResponse>,
)
