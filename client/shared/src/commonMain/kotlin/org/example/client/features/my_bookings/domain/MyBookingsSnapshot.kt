package org.example.client.features.my_bookings.domain

data class MyBookingsSnapshot(
    val active: List<MyBooking>,
    val history: List<MyBooking>,
) {
    fun all(): List<MyBooking> = active + history

    fun find(bookingId: String): MyBooking? = all().firstOrNull { it.id == bookingId }
}
