package org.example.client.features.my_bookings.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.example.client.core.validation.UuidValidator
import org.example.client.features.booking.domain.BookingStatus

class CancelBookingUseCase(
    private val gateway: MyBookingsGateway,
    private val now: () -> Instant = { Clock.System.now() },
) {
    suspend operator fun invoke(booking: MyBooking): Result<MyBooking> {
        if (booking.status != BookingStatus.CONFIRMED) {
            return Result.failure(IllegalStateException("Отменить можно только подтверждённую запись"))
        }
        if (now() > booking.cancelDeadline) {
            return Result.failure(IllegalStateException("Отмена доступна не позднее чем за 2 часа до начала"))
        }
        val bookingId = UuidValidator.normalize(booking.id)
            ?: return Result.failure(IllegalArgumentException("Некорректный идентификатор брони"))
        return gateway.cancelBooking(bookingId)
    }
}
