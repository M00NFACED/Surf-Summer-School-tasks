package org.example.client.features.booking.data

interface BookingRepository {
    suspend fun getSlotDetails(slotId: String): Result<TrainingSlotDetails>
    suspend fun createBooking(request: CreateBookingRequest): Result<BookingResponse>
}
