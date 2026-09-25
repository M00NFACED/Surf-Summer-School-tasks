package org.example.client.features.my_bookings.domain

class GetMyBookingsUseCase(
    private val gateway: MyBookingsGateway,
) {
    suspend operator fun invoke(): Result<MyBookingsSnapshot> = gateway.getMyBookings()
}
