package org.example.client.features.booking.data

import org.example.client.core.storage.TokenStorage
import org.example.client.features.booking.domain.DuplicateBookingException
import org.example.client.features.booking.domain.SlotFullException

class BookingRepositoryImpl(
    private val remote: BookingRemoteDataSource,
    private val tokenStorage: TokenStorage,
) : BookingRepository {
    override suspend fun getSlotDetails(slotId: String): Result<TrainingSlotDetails> = withToken { token ->
        remote.getSlotDetails(token, slotId)
    }

    override suspend fun createBooking(request: CreateBookingRequest): Result<BookingResponse> = withToken { token ->
        remote.createBooking(token, request).fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                if (error is BookingApiException && error.statusCode == 409) {
                    Result.failure(
                        if (error.errorCode == "BOOKING_EXISTS") {
                            DuplicateBookingException(error.errorCode, error.message)
                        } else {
                            SlotFullException(error.errorCode, error.message)
                        },
                    )
                } else {
                    Result.failure(error)
                }
            },
        )
    }

    override suspend fun getMyBookings(): Result<MyBookingsResponse> = withToken { token ->
        remote.getMyBookings(token)
    }

    override suspend fun cancelBooking(bookingId: String): Result<BookingResponse> = withToken { token ->
        remote.cancelBooking(token, bookingId)
    }

    private suspend fun <T> withToken(block: suspend (String) -> Result<T>): Result<T> {
        val token = tokenStorage.read()
        return if (token.isNullOrBlank()) {
            Result.failure(BookingApiException(401, "UNAUTHORIZED", "Сессия истекла"))
        } else {
            block(token)
        }
    }
}
