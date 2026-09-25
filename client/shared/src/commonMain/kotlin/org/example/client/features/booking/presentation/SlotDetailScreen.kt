package org.example.client.features.booking.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.features.booking.domain.BookingConfirmation
import org.example.client.features.booking.domain.EquipmentType
import org.example.client.features.booking.domain.SlotDetailsItem
import org.example.client.features.schedule.domain.SlotStatus

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Карточка тренировки") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            when (val current = state) {
                BookingState.Initial, BookingState.Loading -> BookingSkeleton()
                is BookingState.DetailsLoaded -> SlotDetailsContent(current.details, onBook)
                is BookingState.NetworkError -> DetailError(current.message) { viewModel.loadSlot(slotId) }
                is BookingState.ValidationError -> DetailError(current.message) { viewModel.loadSlot(slotId) }
                BookingState.Forbidden -> DetailError("Сессия истекла", onBack)
                is BookingState.ConflictError -> details?.let { SlotDetailsContent(it, onBook) } ?: DetailError(current.message, onBack)
                is BookingState.Submitting -> details?.let { SlotDetailsContent(it, onBook, true) } ?: BookingSkeleton()
                is BookingState.Success -> details?.let { SlotDetailsContent(it, onBook) } ?: BookingSkeleton()
            }
        }
    }
}

@Composable
private fun SlotDetailsContent(details: SlotDetailsItem, onBook: () -> Unit, submitting: Boolean = false) {
    val slot = details.slot
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(formatBookingDateTime(slot.startsAt), style = MaterialTheme.typography.titleLarge)
        Text("${formatBookingDateTime(slot.startsAt)} — ${formatBookingDateTime(slot.endsAt)}")
        Text(slot.format.displayName, style = MaterialTheme.typography.titleMedium)
        Text("Инструктор: ${slot.instructor.fullName}")
        Text("Адрес: ${slot.address}")
        Text("Свободно мест: ${slot.availablePlaces} из ${slot.capacity}")
        if (slot.status == SlotStatus.CANCELLED) {
            Text("Отменена скалодромом", color = MaterialTheme.colorScheme.error)
            slot.cancellationReason?.let { Text(it) }
        } else if (slot.availablePlaces == 0) {
            Text("Мест нет", color = MaterialTheme.colorScheme.error)
        }
        Text("Доступный прокат", style = MaterialTheme.typography.titleMedium)
        details.equipmentOptions.filter { it.isAvailable }.forEach { option ->
            Text("${option.name} ${option.size.orEmpty()} · ${option.price.toInt()} ${option.currency} · осталось ${option.availableQuantity}")
        }
        Button(
            onClick = onBook,
            enabled = details.isAvailable && !submitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (submitting) CircularProgressIndicator() else Text("Записаться")
        }
    }
}

@Composable
private fun DetailError(message: String, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message)
        Button(onClick = onRetry) { Text("Повторить") }
    }
}
