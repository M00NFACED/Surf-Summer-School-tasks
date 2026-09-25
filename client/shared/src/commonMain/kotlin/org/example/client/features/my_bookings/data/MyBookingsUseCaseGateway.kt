package org.example.client.features.my_bookings.data

import org.example.client.features.booking.data.BookingRepository
import org.example.client.features.my_bookings.domain.MyBooking
import org.example.client.features.my_bookings.domain.MyBookingsGateway
import org.example.client.features.my_bookings.domain.MyBookingsSnapshot

class MyBookingsUseCaseGateway(
    private val repository: BookingRepository,
) : MyBookingsGateway {
    override suspend fun getMyBookings(): Result<MyBookingsSnapshot> =
        repository.getMyBookings().map(MyBookingsMapper::toDomain)

    override suspend fun cancelBooking(bookingId: String): Result<MyBooking> =
        repository.cancelBooking(bookingId).map(MyBookingsMapper::toDomain)
}
