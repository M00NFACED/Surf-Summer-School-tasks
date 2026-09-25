package org.example.client.features.booking.presentation

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
import org.example.client.features.booking.domain.BookingConfirmation
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.domain.EquipmentType
import org.example.client.features.booking.domain.GetSlotDetailsUseCase
import org.example.client.features.booking.domain.CreateBookingUseCase
import org.example.client.features.booking.domain.SlotDetailsItem
import org.example.client.features.booking.domain.SlotFullException

class BookingViewModel(
    private val getSlotDetails: GetSlotDetailsUseCase,
    private val createBooking: CreateBookingUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutableState = MutableStateFlow<BookingState>(BookingState.Initial)
    private val mutableDetails = MutableStateFlow<SlotDetailsItem?>(null)
    private val mutableShoes = MutableStateFlow<EquipmentSelection?>(null)
    private val mutableHarness = MutableStateFlow<EquipmentSelection?>(null)
    private var detailsJob: Job? = null
    private var submitJob: Job? = null

    val state: StateFlow<BookingState> = mutableState.asStateFlow()
    val details: StateFlow<SlotDetailsItem?> = mutableDetails.asStateFlow()
    val shoes: StateFlow<EquipmentSelection?> = mutableShoes.asStateFlow()
    val harness: StateFlow<EquipmentSelection?> = mutableHarness.asStateFlow()

    fun loadDetails(slotId: String) {
        detailsJob?.cancel()
        mutableShoes.value = null
        mutableHarness.value = null
        mutableState.value = BookingState.Loading
        detailsJob = scope.launch {
            try {
                getSlotDetails(slotId)
                    .onSuccess { details ->
                        mutableDetails.value = details
                        mutableState.value = BookingState.DetailsLoaded(details)
                    }
                    .onFailure { error -> mutableState.value = error.toBookingState() }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                mutableState.value = BookingState.NetworkError(NetworkErrorMessage)
            }
        }
    }

    fun selectShoes(selection: EquipmentSelection) {
        mutableShoes.value = selection
    }

    fun selectHarness(selection: EquipmentSelection) {
        mutableHarness.value = selection
    }

    fun submit() {
        val details = mutableDetails.value ?: return
        if (submitJob?.isActive == true) return
        submitJob = scope.launch {
            mutableState.value = BookingState.Submitting
            try {
                createBooking(
                    slotId = details.slot.id,
                    slotStatus = details.slot.status,
                    availablePlaces = details.availablePlaces,
                    shoes = mutableShoes.value,
                    harness = mutableHarness.value,
                    availableShoeOptionIds = details.equipmentOptions
                        .filter { it.type == EquipmentType.CLIMBING_SHOES && it.isAvailable }
                        .map { it.id }
                        .toSet(),
                    availableHarnessOptionIds = details.equipmentOptions
                        .filter { it.type == EquipmentType.HARNESS_SYSTEM && it.isAvailable }
                        .map { it.id }
                        .toSet(),
                ).onSuccess { confirmation: BookingConfirmation ->
                    mutableState.value = BookingState.Success(confirmation)
                }.onFailure { error ->
                    if (error is SlotFullException) {
                        val refreshed = getSlotDetails(details.slot.id).getOrNull()
                        if (refreshed != null) mutableDetails.value = refreshed
                        mutableState.value = BookingState.ConflictError(error.message)
                    } else {
                        mutableState.value = error.toBookingState()
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                mutableState.value = BookingState.NetworkError(NetworkErrorMessage)
            }
        }
    }

    fun close() {
        detailsJob?.cancel()
        submitJob?.cancel()
        scope.cancel()
    }

    private fun Throwable.toBookingState(): BookingState = when (this) {
        is BookingError -> when (statusCode) {
            401 -> BookingState.Forbidden
            400 -> BookingState.ValidationError(message ?: "Проверьте данные брони")
            else -> BookingState.NetworkError(NetworkErrorMessage)
        }
        is IllegalArgumentException, is IllegalStateException ->
            BookingState.ValidationError(message ?: "Проверьте данные брони")
        else -> BookingState.NetworkError(NetworkErrorMessage)
    }
}
