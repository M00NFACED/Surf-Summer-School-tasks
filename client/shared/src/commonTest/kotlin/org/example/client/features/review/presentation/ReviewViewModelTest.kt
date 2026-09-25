package org.example.client.features.review.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.domain.PaymentMethod
import org.example.client.features.booking.domain.testSlotItem
import org.example.client.features.my_bookings.domain.MyBooking
import org.example.client.features.my_bookings.domain.MyBookingEquipment
import org.example.client.features.review.domain.ReviewGateway
import org.example.client.features.review.domain.SubmitReviewUseCase

class ReviewViewModelTest {
    @Test
    fun submitIsBlockedWithoutStars() {
        val viewModel = reviewViewModel()

        viewModel.submit(completedBooking())

        assertFalse(viewModel.state.value.isSubmitting)
        assertEquals("Выберите оценку от 1 до 5 звёзд", viewModel.state.value.errorMessage)
    }

    @Test
    fun successfulSubmitMarksCompletion() {
        val viewModel = reviewViewModel()

        viewModel.selectScore(4)
        viewModel.submit(completedBooking())

        assertTrue(viewModel.state.value.isCompleted)
        assertEquals(4, viewModel.state.value.score)
        viewModel.close()
    }

    @Test
    fun networkFailureKeepsSelectedScoreForSafeRetry() {
        val gateway = FailingGateway()
        val viewModel = ReviewViewModel(SubmitReviewUseCase(gateway), Dispatchers.Unconfined)

        viewModel.selectScore(3)
        viewModel.submit(completedBooking())

        assertFalse(viewModel.state.value.isCompleted)
        assertTrue(viewModel.state.value.isOffline)
        assertEquals(3, viewModel.state.value.score)
        viewModel.close()
    }

    @Test
    fun cannotSubmitWhileRequestIsActive() {
        val gate = CompletableDeferred<Unit>()
        val gateway = GatedGateway(gate)
        val viewModel = ReviewViewModel(SubmitReviewUseCase(gateway), Dispatchers.Unconfined)

        viewModel.selectScore(5)
        viewModel.submit(completedBooking())
        assertTrue(viewModel.state.value.isSubmitting)

        viewModel.submit(completedBooking())
        gate.complete(Unit)

        assertTrue(viewModel.state.value.isCompleted)
        assertEquals(5, viewModel.state.value.score)
        assertEquals(1, gateway.calls)
        viewModel.close()
    }
}

private fun reviewViewModel() = ReviewViewModel(SubmitReviewUseCase(SuccessGateway()), Dispatchers.Unconfined)

private fun completedBooking() = MyBooking(
    id = "22222222-2222-4222-8222-222222222222",
    slot = testSlotItem(),
    status = BookingStatus.COMPLETED,
    equipment = MyBookingEquipment(EquipmentSelection.Own, EquipmentSelection.Own),
    paymentMethod = PaymentMethod.ON_SITE,
    cancelDeadline = Instant.parse("2026-09-25T06:00:00Z"),
)

private class SuccessGateway : ReviewGateway {
    override suspend fun submitRating(bookingId: String, score: Int): Result<Int> = Result.success(score)
}

private class FailingGateway : ReviewGateway {
    override suspend fun submitRating(bookingId: String, score: Int): Result<Int> =
        Result.failure(error("offline"))
}

private class GatedGateway(private val gate: CompletableDeferred<Unit>) : ReviewGateway {
    var calls = 0
        private set

    override suspend fun submitRating(bookingId: String, score: Int): Result<Int> {
        calls += 1
        gate.await()
        return Result.success(score)
    }
}
