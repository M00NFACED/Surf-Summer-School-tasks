package org.example.client.features.booking.data

interface BookingRepository {
    suspend fun getSlotDetails(slotId: String): Result<TrainingSlotDetails>
    suspend fun createBooking(request: CreateBookingRequest): Result<BookingResponse>
    suspend fun getMyBookings(): Result<MyBookingsResponse>
    suspend fun cancelBooking(bookingId: String): Result<BookingResponse>
    suspend fun rateBooking(bookingId: String, score: Int): Result<RatingDto>
}
