package org.example.client.features.my_bookings.data

import kotlinx.datetime.Instant
import org.example.client.features.booking.data.BookingResponse
import org.example.client.features.booking.data.EquipmentSelectionPayload
import org.example.client.features.booking.data.MyBookingsResponse
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.domain.PaymentMethod
import org.example.client.features.my_bookings.domain.MyBooking
import org.example.client.features.my_bookings.domain.MyBookingEquipment
import org.example.client.features.my_bookings.domain.MyBookingsSnapshot
import org.example.client.features.schedule.data.ScheduleMapper

object MyBookingsMapper {
    fun toDomain(response: MyBookingsResponse): MyBookingsSnapshot = MyBookingsSnapshot(
        active = response.active.map(::toDomain),
        history = response.history.map(::toDomain),
    )

    fun toDomain(response: BookingResponse): MyBooking = MyBooking(
        id = response.id,
        slot = ScheduleMapper.toDomain(response.slot),
        status = BookingStatus.fromApi(response.status) ?: error("Unknown booking status"),
        equipment = MyBookingEquipment(
            shoes = response.equipment.shoes.toDomain(),
            harness = response.equipment.harness.toDomain(),
        ),
        paymentMethod = PaymentMethod.ON_SITE,
        cancelDeadline = Instant.parse(response.cancelDeadline),
        cancellationReason = response.cancellation?.reason,
        cancelledAt = response.cancelledAt?.let(Instant::parse),
        ratingScore = response.rating?.score,
    )

    private fun EquipmentSelectionPayload.toDomain(): EquipmentSelection = when (source) {
        "own" -> EquipmentSelection.Own
        "rental" -> EquipmentSelection.Rental(optionId ?: error("Rental option is missing"))
        else -> error("Unknown equipment source")
    }
}
