package org.example.client.features.booking.domain

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetSlotDetailsUseCaseTest {
    @Test
    fun acceptsBackendCanonicalSlotIdAndTrimsInput() = runBlocking {
        val slotId = "00000000-0000-0000-0000-000000000010"
        val gateway = RecordingGateway()
        val result = GetSlotDetailsUseCase(gateway)(" $slotId ")

        assertTrue(result.isSuccess)
        assertEquals(slotId, gateway.requestedSlotId)
    }
}

private class RecordingGateway : BookingGateway {
    var requestedSlotId: String? = null

    override suspend fun getSlotDetails(slotId: String): Result<SlotDetailsItem> {
        requestedSlotId = slotId
        return Result.success(testSlotDetails())
    }

    override suspend fun createBooking(intent: BookingIntent): Result<BookingConfirmation> =
        Result.success(testConfirmation())
}
