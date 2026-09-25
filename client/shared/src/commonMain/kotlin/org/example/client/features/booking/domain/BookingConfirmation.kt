package org.example.client.features.booking.domain

import kotlinx.datetime.Instant
import org.example.client.features.schedule.domain.TrainingSlotItem

data class BookingConfirmation(
    val id: String,
    val slot: TrainingSlotItem,
    val status: BookingStatus,
    val paymentMethod: PaymentMethod,
    val createdAt: Instant,
)
