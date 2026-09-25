package org.example.client.features.review.presentation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.client.core.network.NetworkErrorMessage
import org.example.client.features.booking.data.BookingApiException
import org.example.client.features.my_bookings.domain.MyBooking
import org.example.client.features.review.domain.ReviewNotAllowedException
import org.example.client.features.review.domain.SubmitReviewUseCase

class ReviewViewModel(
    private val submitReview: SubmitReviewUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutableState = MutableStateFlow(ReviewState())
    private var submitJob: Job? = null

    val state: StateFlow<ReviewState> = mutableState.asStateFlow()

    fun selectScore(score: Int) {
        if (mutableState.value.isSubmitting) return
        mutableState.value = mutableState.value.copy(score = score, errorMessage = null)
    }

    fun submit(booking: MyBooking) {
        if (submitJob?.isActive == true) return
        if (!mutableState.value.canSubmit) {
            mutableState.value = mutableState.value.copy(errorMessage = "Выберите оценку от 1 до 5 звёзд")
            return
        }
        submitJob = scope.launch {
            mutableState.value = mutableState.value.copy(isSubmitting = true, errorMessage = null)
            try {
                submitReview(booking, mutableState.value.score)
                    .onSuccess { score ->
                        mutableState.value = ReviewState(score = score, isCompleted = true)
                    }
                    .onFailure { error -> mutableState.value = mutableState.value.toErrorState(error) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                mutableState.value = mutableState.value.copy(
                    isSubmitting = false,
                    errorMessage = NetworkErrorMessage,
                    isOffline = true,
                )
            }
        }
    }

    fun close() {
        submitJob?.cancel()
        scope.cancel()
    }

    private fun ReviewState.toErrorState(error: Throwable): ReviewState = when (error) {
        is ReviewNotAllowedException -> copy(isSubmitting = false, errorMessage = error.message)
        is BookingApiException -> when (error.statusCode) {
            401 -> copy(isSubmitting = false, isForbidden = true, errorMessage = "Сессия истекла. Войдите снова")
            400 -> copy(isSubmitting = false, errorMessage = error.message)
            409 -> copy(isSubmitting = false, errorMessage = "Оценка уже отправлена")
            else -> copy(isSubmitting = false, errorMessage = error.message, isOffline = true)
        }
        else -> copy(isSubmitting = false, errorMessage = NetworkErrorMessage, isOffline = true)
    }
}
