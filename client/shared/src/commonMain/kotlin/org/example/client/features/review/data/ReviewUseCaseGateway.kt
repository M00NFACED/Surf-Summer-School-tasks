package org.example.client.features.review.data

import org.example.client.features.booking.data.BookingRepository
import org.example.client.features.review.domain.ReviewGateway

class ReviewUseCaseGateway(
    private val repository: BookingRepository,
) : ReviewGateway {
    override suspend fun submitRating(bookingId: String, score: Int): Result<Int> =
        repository.rateBooking(bookingId, score).map { it.score }
}
