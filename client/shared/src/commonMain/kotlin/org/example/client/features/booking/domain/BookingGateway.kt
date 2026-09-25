package org.example.client.features.booking.domain

interface BookingGateway {
    suspend fun getSlotDetails(slotId: String): Result<SlotDetailsItem>
    suspend fun createBooking(intent: BookingIntent): Result<BookingConfirmation>
}
