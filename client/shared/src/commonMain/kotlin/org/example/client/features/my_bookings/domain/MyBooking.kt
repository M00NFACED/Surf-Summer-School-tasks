package org.example.client.features.my_bookings.domain

import kotlinx.datetime.Instant
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.booking.domain.PaymentMethod
import org.example.client.features.schedule.domain.TrainingSlotItem

data class MyBooking(
    val id: String,
    val slot: TrainingSlotItem,
    val status: BookingStatus,
    val equipment: MyBookingEquipment,
    val paymentMethod: PaymentMethod,
    val cancelDeadline: Instant,
    val cancellationReason: String? = null,
    val cancelledAt: Instant? = null,
    val ratingScore: Int? = null,
)
