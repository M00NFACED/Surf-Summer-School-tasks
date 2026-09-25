package org.example.client.features.my_bookings.domain

interface MyBookingsGateway {
    suspend fun getMyBookings(): Result<MyBookingsSnapshot>

    suspend fun cancelBooking(bookingId: String): Result<MyBooking>
}
