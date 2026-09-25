package org.example.client.features.review.domain

import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.my_bookings.domain.MyBooking

internal val MinReviewScore = 1
internal val MaxReviewScore = 5

fun MyBooking.isReviewable(): Boolean = status == BookingStatus.COMPLETED && ratingScore == null

fun isValidReviewScore(score: Int): Boolean = score in MinReviewScore..MaxReviewScore

sealed interface ReviewRejection {
    data object NotCompleted : ReviewRejection
    data object AlreadyRated : ReviewRejection
    data object InvalidScore : ReviewRejection
    data object InvalidBookingId : ReviewRejection
}

class ReviewNotAllowedException(val rejection: ReviewRejection) : IllegalStateException(rejection.userMessage)

private val ReviewRejection.userMessage: String
    get() = when (this) {
        ReviewRejection.NotCompleted -> "Оценку можно оставить только для завершённой тренировки"
        ReviewRejection.AlreadyRated -> "Оценка уже отправлена"
        ReviewRejection.InvalidScore -> "Выберите оценку от 1 до 5 звёзд"
        ReviewRejection.InvalidBookingId -> "Некорректный идентификатор брони"
    }
