package org.example.client.features.my_bookings.presentation

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
import org.example.client.features.booking.domain.BookingError
import org.example.client.features.my_bookings.domain.CancelBookingUseCase
import org.example.client.features.my_bookings.domain.GetMyBookingsUseCase
import org.example.client.features.my_bookings.domain.MyBooking

class MyBookingsViewModel(
    private val getMyBookings: GetMyBookingsUseCase,
    private val cancelBooking: CancelBookingUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutableState = MutableStateFlow(MyBookingsState())
    private var loadJob: Job? = null
    private var cancelJob: Job? = null

    val state: StateFlow<MyBookingsState> = mutableState.asStateFlow()

    fun load() {
        if (loadJob?.isActive == true) return
        loadJob = scope.launch {
            mutableState.value = mutableState.value.copy(
                isLoading = true,
                errorMessage = null,
                isOffline = false,
                isForbidden = false,
            )
            try {
                getMyBookings()
                    .onSuccess { snapshot -> mutableState.value = MyBookingsState(snapshot = snapshot) }
                    .onFailure { error -> mutableState.value = error.toState(mutableState.value) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                mutableState.value = mutableState.value.copy(
                    isLoading = false,
                    errorMessage = NetworkErrorMessage,
                    isOffline = mutableState.value.snapshot != null,
                )
            }
        }
    }

    fun cancel(booking: MyBooking) {
        if (cancelJob?.isActive == true) return
        cancelJob = scope.launch {
            mutableState.value = mutableState.value.copy(isCancelling = true, cancellationError = null)
            try {
                cancelBooking(booking)
                    .onSuccess { updated ->
                        mutableState.value = replaceBooking(updated)
                        refreshSnapshot()
                    }
                    .onFailure { error ->
                        mutableState.value = mutableState.value.copy(
                            isCancelling = false,
                            cancellationError = error.message ?: "Не удалось отменить запись",
                        )
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                mutableState.value = mutableState.value.copy(
                    isCancelling = false,
                    cancellationError = NetworkErrorMessage,
                )
            }
        }
    }

    private suspend fun refreshSnapshot() {
        getMyBookings()
            .onSuccess { snapshot -> mutableState.value = mutableState.value.copy(snapshot = snapshot) }
    }

    fun close() {
        loadJob?.cancel()
        cancelJob?.cancel()
        scope.cancel()
    }

    fun applyReview(bookingId: String, score: Int) {
        val snapshot = mutableState.value.snapshot ?: return
        mutableState.value = mutableState.value.copy(
            snapshot = snapshot.copy(
                active = snapshot.active.map { it.withRating(bookingId, score) },
                history = snapshot.history.map { it.withRating(bookingId, score) },
            ),
        )
    }

    private fun MyBooking.withRating(bookingId: String, score: Int): MyBooking =
        if (id == bookingId) {
            copy(status = org.example.client.features.booking.domain.BookingStatus.RATED, ratingScore = score)
        } else {
            this
        }

    private fun replaceBooking(updated: MyBooking): MyBookingsState {
        val current = mutableState.value
        val snapshot = current.snapshot
        return current.copy(
            isCancelling = false,
            cancellationError = null,
            snapshot = snapshot?.let {
                it.copy(
                    active = it.active.map { booking -> if (booking.id == updated.id) updated else booking },
                    history = it.history.map { booking -> if (booking.id == updated.id) updated else booking },
                )
            },
        )
    }

    private fun Throwable.toState(current: MyBookingsState): MyBookingsState = when (this) {
        is BookingError -> if (statusCode == 401) {
            current.copy(isLoading = false, isForbidden = true, errorMessage = message)
        } else {
            current.copy(isLoading = false, errorMessage = message, isOffline = current.snapshot != null)
        }
        else -> current.copy(
            isLoading = false,
            errorMessage = NetworkErrorMessage,
            isOffline = current.snapshot != null,
        )
    }
}
