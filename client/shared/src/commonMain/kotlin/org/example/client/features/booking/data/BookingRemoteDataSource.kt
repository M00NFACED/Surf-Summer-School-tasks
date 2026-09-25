package org.example.client.features.booking.data

interface BookingRemoteDataSource {
    suspend fun getSlotDetails(token: String, slotId: String): Result<TrainingSlotDetails>
    suspend fun createBooking(token: String, request: CreateBookingRequest): Result<BookingResponse>
    suspend fun getMyBookings(token: String): Result<MyBookingsResponse>
    suspend fun cancelBooking(token: String, bookingId: String): Result<BookingResponse>
}
