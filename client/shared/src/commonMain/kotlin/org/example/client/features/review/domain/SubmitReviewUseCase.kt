package org.example.client.features.review.domain

import org.example.client.core.validation.UuidValidator
import org.example.client.features.my_bookings.domain.MyBooking

class SubmitReviewUseCase(
    private val gateway: ReviewGateway,
) {
    suspend operator fun invoke(booking: MyBooking, score: Int): Result<Int> {
        val rejection = when {
            !isValidReviewScore(score) -> ReviewRejection.InvalidScore
            !booking.isReviewable() -> rejectionFor(booking)
            UuidValidator.normalize(booking.id) == null -> ReviewRejection.InvalidBookingId
            else -> null
        }
        if (rejection != null) return Result.failure(ReviewNotAllowedException(rejection))
        val bookingId = UuidValidator.normalize(booking.id) ?: booking.id
        return gateway.submitRating(bookingId, score)
    }

    private fun rejectionFor(booking: MyBooking): ReviewRejection =
        if (booking.ratingScore != null) ReviewRejection.AlreadyRated else ReviewRejection.NotCompleted
}
