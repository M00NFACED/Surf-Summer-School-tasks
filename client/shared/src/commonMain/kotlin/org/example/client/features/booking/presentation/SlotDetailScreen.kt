package org.example.client.features.booking.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.core.ui.WaveTopBar

@Composable
fun SlotDetailScreen(
    viewModel: BookingViewModel,
    slotId: String,
    onBack: () -> Unit,
    onBook: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val details by viewModel.details.collectAsState()
    LaunchedEffect(slotId) { viewModel.loadSlot(slotId) }

    Column(modifier = Modifier.fillMaxSize()) {
        WaveTopBar(title = "Тренировка", onBack = onBack)
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            when (val current = state) {
                BookingState.Initial, BookingState.Loading -> BookingSkeleton()
                is BookingState.DetailsLoaded -> SlotDetailContent(current.details, onBook)
                is BookingState.NetworkError -> DetailError(current.message) { viewModel.loadSlot(slotId) }
                is BookingState.ValidationError -> DetailError(current.message) { viewModel.loadSlot(slotId) }
                BookingState.Forbidden -> DetailError("Сессия истекла", onBack)
                is BookingState.ConflictError -> details?.let { SlotDetailContent(it, onBook) }
                    ?: DetailError(current.message, onBack)
                is BookingState.DuplicateBooking -> DetailError(current.message, onBack)
                is BookingState.Submitting -> details?.let { SlotDetailContent(it, onBook, submitting = true) } ?: BookingSkeleton()
                is BookingState.Success -> details?.let { SlotDetailContent(it, onBook) } ?: BookingSkeleton()
            }
        }
    }
}
