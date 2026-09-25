package org.example.client.features.booking.presentation

import org.example.client.features.booking.domain.BookingConfirmation
import org.example.client.features.booking.domain.SlotDetailsItem

sealed interface BookingState {
    data object Initial : BookingState
    data object Loading : BookingState
    data class DetailsLoaded(val details: SlotDetailsItem) : BookingState
    data object Submitting : BookingState
    data class Success(val confirmation: BookingConfirmation) : BookingState
    data class ConflictError(val message: String) : BookingState
    data class DuplicateBooking(val message: String) : BookingState
    data class NetworkError(val message: String) : BookingState
    data class ValidationError(val message: String) : BookingState
    data object Forbidden : BookingState
}
