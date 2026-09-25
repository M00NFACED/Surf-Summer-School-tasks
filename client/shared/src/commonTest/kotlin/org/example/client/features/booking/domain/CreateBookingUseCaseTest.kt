package org.example.client.features.booking.domain

import kotlinx.coroutines.runBlocking
import org.example.client.features.schedule.domain.SlotStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateBookingUseCaseTest {
    @Test
    fun fixesOnSiteAndDoesNotAddParticipantQuantity() = runBlocking {
        val gateway = FakeGateway()
        val useCase = CreateBookingUseCase(gateway)

        val result = useCase(
            slotId = "11111111-1111-4111-8111-111111111111",
            slotStatus = SlotStatus.AVAILABLE,
            availablePlaces = 2,
            shoes = EquipmentSelection.Own,
            harness = EquipmentSelection.Own,
        )

        assertTrue(result.isSuccess)
        assertEquals(PaymentMethod.ON_SITE, gateway.intent?.paymentMethod)
        assertEquals("11111111-1111-4111-8111-111111111111", gateway.intent?.slotId)
    }

    @Test
    fun rejectsMissingOrInvalidEquipmentSelections() = runBlocking {
        val useCase = CreateBookingUseCase(FakeGateway())

        assertTrue(
            useCase(
                slotId = "11111111-1111-4111-8111-111111111111",
                slotStatus = SlotStatus.AVAILABLE,
                availablePlaces = 2,
                shoes = null,
                harness = EquipmentSelection.Own,
            ).isFailure,
        )
        assertTrue(
            useCase(
                slotId = "11111111-1111-4111-8111-111111111111",
                slotStatus = SlotStatus.AVAILABLE,
                availablePlaces = 2,
                shoes = EquipmentSelection.Rental("not-a-uuid"),
                harness = EquipmentSelection.Own,
            ).isFailure,
        )
        assertTrue(
            useCase(
                slotId = "11111111-1111-4111-8111-111111111111",
                slotStatus = SlotStatus.AVAILABLE,
                availablePlaces = 2,
                shoes = EquipmentSelection.Rental("33333333-3333-4333-8333-333333333333"),
                harness = EquipmentSelection.Own,
                availableShoeOptionIds = emptySet(),
            ).isFailure,
        )
        assertTrue(
            useCase(
                slotId = "11111111-1111-4111-8111-111111111111",
                slotStatus = SlotStatus.AVAILABLE,
                availablePlaces = 2,
                shoes = EquipmentSelection.Rental("33333333-3333-4333-8333-333333333333"),
                harness = EquipmentSelection.Own,
                availableShoeOptionIds = setOf("33333333-3333-4333-8333-333333333333"),
            ).isSuccess,
        )
    }

    @Test
    fun rejectsUnavailableOrFullSlot() = runBlocking {
        val useCase = CreateBookingUseCase(FakeGateway())

        assertTrue(
            useCase(
                slotId = "11111111-1111-4111-8111-111111111111",
                slotStatus = SlotStatus.CANCELLED,
                availablePlaces = 2,
                shoes = EquipmentSelection.Own,
                harness = EquipmentSelection.Own,
            ).isFailure,
        )
        assertTrue(
            useCase(
                slotId = "11111111-1111-4111-8111-111111111111",
                slotStatus = SlotStatus.AVAILABLE,
                availablePlaces = 0,
                shoes = EquipmentSelection.Own,
                harness = EquipmentSelection.Own,
            ).isFailure,
        )
    }
}

private class FakeGateway : BookingGateway {
    var intent: BookingIntent? = null

    override suspend fun getSlotDetails(slotId: String): Result<SlotDetailsItem> = Result.success(testSlotDetails())

    override suspend fun createBooking(intent: BookingIntent): Result<BookingConfirmation> {
        this.intent = intent
        return Result.success(testConfirmation())
    }
}
