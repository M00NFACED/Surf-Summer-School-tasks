package org.example.client.features.review.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.domain.PaymentMethod
import org.example.client.features.booking.domain.testSlotItem
import org.example.client.features.my_bookings.domain.MyBooking
import org.example.client.features.my_bookings.domain.MyBookingEquipment

class SubmitReviewUseCaseTest {
    @Test
    fun rejectsZeroStars() = runBlocking {
        val gateway = RecordingGateway()
        val useCase = SubmitReviewUseCase(gateway)

        val result = useCase(completedBooking(), score = 0)

        assertRejection(ReviewRejection.InvalidScore, result)
        assertEquals(null, gateway.submitted)
    }

    @Test
    fun rejectsScoreAboveFive() = runBlocking {
        val gateway = RecordingGateway()
        val useCase = SubmitReviewUseCase(gateway)

        val result = useCase(completedBooking(), score = 6)

        assertRejection(ReviewRejection.InvalidScore, result)
        assertEquals(null, gateway.submitted)
    }

    @Test
    fun rejectsNotCompletedBooking() = runBlocking {
        val gateway = RecordingGateway()
        val useCase = SubmitReviewUseCase(gateway)

        val result = useCase(confirmedBooking(), score = 5)

        assertRejection(ReviewRejection.NotCompleted, result)
        assertEquals(null, gateway.submitted)
    }

    @Test
    fun rejectsCancelledBooking() = runBlocking {
        val gateway = RecordingGateway()
        val useCase = SubmitReviewUseCase(gateway)

        val result = useCase(cancelledBooking(), score = 4)

        assertRejection(ReviewRejection.NotCompleted, result)
        assertEquals(null, gateway.submitted)
    }

    @Test
    fun rejectsSecondRatingForSameBooking() = runBlocking {
        val gateway = RecordingGateway()
        val useCase = SubmitReviewUseCase(gateway)

        val result = useCase(ratedBooking(), score = 5)

        assertRejection(ReviewRejection.AlreadyRated, result)
        assertEquals(null, gateway.submitted)
    }

    @Test
    fun rejectsMalformedBookingId() = runBlocking {
        val gateway = RecordingGateway()
        val useCase = SubmitReviewUseCase(gateway)

        val result = useCase(completedBooking(id = "not-a-uuid"), score = 3)

        assertRejection(ReviewRejection.InvalidBookingId, result)
        assertEquals(null, gateway.submitted)
    }

    @Test
    fun sendsSingleRatingForCompletedBooking() = runBlocking {
        val gateway = RecordingGateway()
        val useCase = SubmitReviewUseCase(gateway)

        val result = useCase(completedBooking(), score = 5)

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrThrow())
        assertEquals("22222222-2222-4222-8222-222222222222", gateway.submitted?.first)
        assertEquals(5, gateway.submitted?.second)
    }

    @Test
    fun scoreRangeMatchesContractBoundaries() {
        assertFalse(isValidReviewScore(0))
        assertTrue(isValidReviewScore(1))
        assertTrue(isValidReviewScore(5))
        assertFalse(isValidReviewScore(6))
    }

    @Test
    fun reviewableOnlyForCompletedAndUnrated() {
        assertTrue(completedBooking().isReviewable())
        assertFalse(confirmedBooking().isReviewable())
        assertFalse(cancelledBooking().isReviewable())
        assertFalse(ratedBooking().isReviewable())
    }
}

private fun assertRejection(expected: ReviewRejection, result: Result<Int>) {
    val error = assertIs<ReviewNotAllowedException>(result.exceptionOrNull())
    assertEquals(expected, error.rejection)
}

private fun completedBooking(
    id: String = "22222222-2222-4222-8222-222222222222",
    ratingScore: Int? = null,
) = myBooking(
    id = id,
    status = BookingStatus.COMPLETED,
    ratingScore = ratingScore,
)

private fun confirmedBooking(id: String = "22222222-2222-4222-8222-222222222222") =
    myBooking(id = id, status = BookingStatus.CONFIRMED)

private fun cancelledBooking(id: String = "22222222-2222-4222-8222-222222222222") =
    myBooking(id = id, status = BookingStatus.CANCELLED_BY_VENUE)

private fun ratedBooking(id: String = "22222222-2222-4222-8222-222222222222") =
    myBooking(id = id, status = BookingStatus.RATED, ratingScore = 4)

private fun myBooking(id: String, status: BookingStatus, ratingScore: Int? = null) = MyBooking(
    id = id,
    slot = testSlotItem(),
    status = status,
    equipment = MyBookingEquipment(EquipmentSelection.Own, EquipmentSelection.Own),
    paymentMethod = PaymentMethod.ON_SITE,
    cancelDeadline = kotlinx.datetime.Instant.parse("2026-09-25T06:00:00Z"),
    ratingScore = ratingScore,
)

private class RecordingGateway : ReviewGateway {
    var submitted: Pair<String, Int>? = null

    override suspend fun submitRating(bookingId: String, score: Int): Result<Int> {
        submitted = bookingId to score
        return Result.success(score)
    }
}
