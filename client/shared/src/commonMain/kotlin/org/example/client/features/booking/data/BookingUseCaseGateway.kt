package org.example.client.features.booking.data

import org.example.client.features.booking.domain.BookingConfirmation
import org.example.client.features.booking.domain.BookingGateway
import org.example.client.features.booking.domain.BookingIntent
import org.example.client.features.booking.domain.SlotDetailsItem

class BookingUseCaseGateway(
    private val repository: BookingRepository,
) : BookingGateway {
    override suspend fun getSlotDetails(slotId: String): Result<SlotDetailsItem> =
        repository.getSlotDetails(slotId).map(BookingMapper::toDomain)

    override suspend fun createBooking(intent: BookingIntent): Result<BookingConfirmation> =
        repository.createBooking(BookingMapper.toRequest(intent)).map(BookingMapper::toDomain)
}
