package org.example.client.features.my_bookings.data

import org.example.client.features.booking.data.BookingResponse
import org.example.client.features.booking.data.CancellationInfoDto
import org.example.client.features.booking.data.EquipmentSelectionPayload
import org.example.client.features.booking.data.EquipmentSelectionsPayload
import org.example.client.features.booking.data.MyBookingsResponse
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.schedule.data.InstructorSummary
import org.example.client.features.schedule.data.TrainingSlotSummary
import kotlin.test.Test
import kotlin.test.assertEquals

class MyBookingsMapperTest {
    @Test
    fun splitsActiveAndHistoryAndMapsRentalOptions() {
        val response = MyBookingsResponse(
            active = listOf(bookingResponse(status = "confirmed", optionId = "33333333-3333-4333-8333-333333333333")),
            history = listOf(bookingResponse(status = "cancelled_by_client", optionId = null)),
        )

        val snapshot = MyBookingsMapper.toDomain(response)

        assertEquals(1, snapshot.active.size)
        assertEquals(1, snapshot.history.size)
        assertEquals(BookingStatus.CONFIRMED, snapshot.active.first().status)
        assertEquals(
            EquipmentSelection.Rental("33333333-3333-4333-8333-333333333333"),
            snapshot.active.first().equipment.shoes,
        )
        assertEquals(EquipmentSelection.Own, snapshot.history.first().equipment.harness)
        assertEquals(BookingStatus.CANCELLED_BY_CLIENT, snapshot.history.first().status)
        assertEquals("Передумал", snapshot.history.first().cancellationReason)
    }

    @Test
    fun exposesBookingThroughSnapshotLookup() {
        val response = MyBookingsResponse(
            active = listOf(bookingResponse(status = "confirmed", optionId = null)),
            history = emptyList(),
        )

        val snapshot = MyBookingsMapper.toDomain(response)

        assertEquals("22222222-2222-4222-8222-222222222222", snapshot.find("22222222-2222-4222-8222-222222222222")?.id)
        assertEquals(null, snapshot.find("99999999-9999-4999-8999-999999999999"))
    }
}

private fun bookingResponse(status: String, optionId: String?) = BookingResponse(
    id = "22222222-2222-4222-8222-222222222222",
    slot = TrainingSlotSummary(
        id = "11111111-1111-4111-8111-111111111111",
        startsAt = "2026-09-26T08:00:00Z",
        endsAt = "2026-09-26T10:00:00Z",
        format = "novice_bouldering",
        zone = "zone_a",
        address = "Москва, Волна",
        instructor = InstructorSummary("55555555-5555-4555-8555-555555555555", "Иван Иванов", true),
        capacity = 8,
        availablePlaces = 3,
        status = "available",
    ),
    status = status,
    equipment = EquipmentSelectionsPayload(
        shoes = EquipmentSelectionPayload(source = if (optionId == null) "own" else "rental", optionId = optionId),
        harness = EquipmentSelectionPayload(source = "own", optionId = null),
    ),
    paymentMethod = "on_site",
    cancelDeadline = "2026-09-26T06:00:00Z",
    cancellation = if (status == "cancelled_by_client") CancellationInfoDto("client", "Передумал") else null,
    createdAt = "2026-09-20T10:00:00Z",
    cancelledAt = if (status == "cancelled_by_client") "2026-09-21T10:00:00Z" else null,
    rating = null,
)
