package org.example.client.features.booking.data

import kotlinx.coroutines.runBlocking
import org.example.client.core.storage.InMemoryTokenStorage
import org.example.client.features.booking.domain.DuplicateBookingException
import org.example.client.features.booking.domain.SlotFullException
import org.example.client.features.schedule.data.InstructorSummary
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

    @Test
    fun mapsExistingBookingConflictToDuplicateState() = runBlocking {
        val storage = InMemoryTokenStorage().apply { write("token") }
        val repository = BookingRepositoryImpl(FakeRemote("BOOKING_EXISTS"), storage)

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

        assertTrue(result.exceptionOrNull() is DuplicateBookingException)
    }
}

private class FakeRemote(private val conflictCode: String = "SLOT_FULL") : BookingRemoteDataSource {
    override suspend fun getSlotDetails(token: String, slotId: String): Result<TrainingSlotDetails> =
        Result.failure(BookingApiException(404, "SLOT_NOT_FOUND", "Слот не найден"))

    override suspend fun createBooking(token: String, request: CreateBookingRequest): Result<BookingResponse> =
        Result.failure(BookingApiException(409, conflictCode, "Свободных мест больше нет"))

    override suspend fun getMyBookings(token: String): Result<MyBookingsResponse> =
        Result.success(MyBookingsResponse(emptyList(), emptyList()))

    override suspend fun cancelBooking(token: String, bookingId: String): Result<BookingResponse> =
        Result.failure(BookingApiException(404, "BOOKING_NOT_FOUND", "Бронь не найдена"))

    override suspend fun rateBooking(
        token: String,
        bookingId: String,
        request: RatingRequest,
    ): Result<RatingDto> = Result.success(
        RatingDto(
            id = "66666666-6666-4666-8666-666666666666",
            bookingId = bookingId,
            instructor = InstructorSummary(
                id = "22222222-2222-4222-8222-222222222222",
                fullName = "Анна Петрова",
                isActive = true,
            ),
            score = request.score,
            createdAt = "2026-09-25T10:00:00Z",
        ),
    )
}
