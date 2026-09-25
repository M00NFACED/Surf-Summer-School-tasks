package org.example.client.features.booking.presentation

import kotlinx.coroutines.Dispatchers
import org.example.client.core.network.NetworkErrorMessage
import org.example.client.features.booking.domain.BookingConfirmation
import org.example.client.features.booking.domain.BookingGateway
import org.example.client.features.booking.domain.BookingIntent
import org.example.client.features.booking.domain.CreateBookingUseCase
import org.example.client.features.booking.domain.GetSlotDetailsUseCase
import org.example.client.features.booking.domain.SlotDetailsItem
import kotlin.test.Test
import kotlin.test.assertEquals

class BookingViewModelTest {
    @Test
    fun mapsThrownNetworkFailureToSafeErrorState() {
        val gateway = ThrowingBookingGateway()
        val viewModel = BookingViewModel(
            getSlotDetails = GetSlotDetailsUseCase(gateway),
            createBooking = CreateBookingUseCase(gateway),
            dispatcher = Dispatchers.Unconfined,
        )

        viewModel.loadDetails("11111111-1111-4111-8111-111111111111")

        assertEquals(BookingState.NetworkError(NetworkErrorMessage), viewModel.state.value)
        viewModel.close()
    }
}

private class ThrowingBookingGateway : BookingGateway {
    override suspend fun getSlotDetails(slotId: String): Result<SlotDetailsItem> = error("offline")

    override suspend fun createBooking(intent: BookingIntent): Result<BookingConfirmation> = error("offline")
}
