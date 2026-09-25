package org.example.client.features.my_bookings.domain

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.domain.testSlotItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MyBookingsUseCasesTest {
    @Test
    fun blocksCancellationAfterDeadline() = runBlocking {
        val booking = testBooking(Instant.parse("2026-09-25T10:00:00Z"))
        val gateway = RecordingGateway()
        val useCase = CancelBookingUseCase(gateway, now = { Instant.parse("2026-09-25T11:00:00Z") })

        val result = useCase(booking)

        assertTrue(result.isFailure)
        assertEquals(null, gateway.cancelledId)
    }

    @Test
    fun sendsCancellationBeforeDeadline() = runBlocking {
        val booking = testBooking(Instant.parse("2026-09-25T12:00:00Z"))
        val gateway = RecordingGateway()
        val useCase = CancelBookingUseCase(gateway, now = { Instant.parse("2026-09-25T09:00:00Z") })

        val result = useCase(booking)

        assertTrue(result.isSuccess)
        assertEquals(booking.id, gateway.cancelledId)
    }
}

private fun testBooking(cancelDeadline: Instant) = MyBooking(
    id = "00000000-0000-0000-0000-000000000010",
    slot = testSlotItem(),
    status = BookingStatus.CONFIRMED,
    equipment = MyBookingEquipment(EquipmentSelection.Own, EquipmentSelection.Own),
    paymentMethod = org.example.client.features.booking.domain.PaymentMethod.ON_SITE,
    cancelDeadline = cancelDeadline,
)

private class RecordingGateway : MyBookingsGateway {
    var cancelledId: String? = null

    override suspend fun getMyBookings(): Result<MyBookingsSnapshot> = Result.success(MyBookingsSnapshot(emptyList(), emptyList()))

    override suspend fun cancelBooking(bookingId: String): Result<MyBooking> {
        cancelledId = bookingId
        return Result.success(
            testBooking(Instant.parse("2026-09-25T12:00:00Z")).copy(id = bookingId),
        )
    }
}
