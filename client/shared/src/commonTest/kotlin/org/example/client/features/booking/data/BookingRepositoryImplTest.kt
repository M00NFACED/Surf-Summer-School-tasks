package org.example.client.features.booking.data

import kotlinx.coroutines.runBlocking
import org.example.client.core.storage.InMemoryTokenStorage
import org.example.client.features.booking.domain.SlotFullException
import kotlin.test.Test
import kotlin.test.assertTrue

class BookingRepositoryImplTest {
    @Test
    fun mapsConflictToSlotFullWithoutCreatingBooking() = runBlocking {
        val storage = InMemoryTokenStorage().apply { write("token") }
        val remote = FakeRemote()
        val repository = BookingRepositoryImpl(remote, storage)

        val result = repository.createBooking(
            CreateBookingRequest(
                slotId = "11111111-1111-4111-8111-111111111111",
                equipment = EquipmentSelectionsPayload(
                    shoes = EquipmentSelectionPayload("own"),
                    harness = EquipmentSelectionPayload("own"),
                ),
                paymentMethod = "on_site",
            ),
        )

        assertTrue(result.exceptionOrNull() is SlotFullException)
    }
}

private class FakeRemote : BookingRemoteDataSource {
    override suspend fun getSlotDetails(token: String, slotId: String): Result<TrainingSlotDetails> =
        Result.failure(BookingApiException(404, "SLOT_NOT_FOUND", "Слот не найден"))

    override suspend fun createBooking(token: String, request: CreateBookingRequest): Result<BookingResponse> =
        Result.failure(BookingApiException(409, "SLOT_FULL", "Свободных мест больше нет"))
}
