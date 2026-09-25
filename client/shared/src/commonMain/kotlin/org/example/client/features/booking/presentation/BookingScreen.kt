package org.example.client.features.booking.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WavePrimaryButton
import org.example.client.core.ui.WaveTopBar
import org.example.client.features.booking.domain.EquipmentType

@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onMyBookings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val details by viewModel.details.collectAsState()
    val submitting = state is BookingState.Submitting

    if (state is BookingState.Success) {
        BookingSuccessView(
            onMyBookings = onMyBookings,
            onDone = onBack,
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WaveTopBar(title = "Оформление записи", onBack = onBack)
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (val current = state) {
                is BookingState.DetailsLoaded -> BookingFormBody(current.details, viewModel, submitting, blocked = false, errorText = null)
                is BookingState.Submitting -> details?.let { BookingFormBody(it, viewModel, submitting, blocked = true, errorText = null) }
                is BookingState.NetworkError -> {
                    ErrorText(current.message)
                    details?.let { BookingFormBody(it, viewModel, submitting, blocked = true, errorText = current.message) }
                }
                is BookingState.ValidationError -> {
                    ErrorText(current.message)
                    details?.let { BookingFormBody(it, viewModel, submitting, blocked = false, errorText = current.message) }
                }
                is BookingState.ConflictError -> {
                    ErrorText(current.message)
                    details?.let { BookingFormBody(it, viewModel, submitting, blocked = false, errorText = null) }
                }
                is BookingState.DuplicateBooking -> DuplicateBookingBlock(onMyBookings)
                BookingState.Initial, BookingState.Loading -> BookingSkeleton()
                BookingState.Forbidden -> ErrorText("Сессия истекла. Войдите снова")
                is BookingState.Success -> Unit
            }
        }
    }

    if (state is BookingState.ConflictError && details != null) {
        SlotTakenDialog(onDismiss = onBack, onRefresh = { details?.let { viewModel.loadSlot(it.id) } })
    }
}

@Composable
private fun BookingFormBody(
    details: org.example.client.features.booking.domain.SlotDetailsItem,
    viewModel: BookingViewModel,
    submitting: Boolean,
    blocked: Boolean,
    errorText: String?,
) {    val colors = MaterialTheme.wave
    val shoes by viewModel.shoes.collectAsState()
    val harness by viewModel.harness.collectAsState()
    BookingSummaryCard(details)
    EquipmentRow(
        title = "Скальники",
        type = EquipmentType.CLIMBING_SHOES,
        details = details,
        selection = shoes,
        onSelection = { selected -> selected?.let(viewModel::selectShoes) },
    )
    EquipmentRow(
        title = "Страховочная система",
        type = EquipmentType.HARNESS_SYSTEM,
        details = details,
        selection = harness,
        onSelection = { selected -> selected?.let(viewModel::selectHarness) },
    )
    Text(
        text = errorText ?: "Одна бронь — один человек. Оплата на месте: наличные или перевод.",
        style = MaterialTheme.typography.bodySmall,
        color = colors.textSecondary,
    )
    WavePrimaryButton(
        text = "Записаться",
        onClick = viewModel::submit,
        enabled = details.isAvailable && shoes != null && harness != null && !submitting && !blocked,
        loading = submitting,
    )
}

@Composable
private fun DuplicateBookingBlock(onMyBookings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Вы уже записаны на эту тренировку",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.wave.textPrimary,
        )
        WavePrimaryButton(text = "Перейти в Мои записи", onClick = onMyBookings)
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(text = message, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun SlotTakenDialog(onDismiss: () -> Unit, onRefresh: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Место занято") },
        text = { Text("Место только что занято другим клиентом. Остаток обновлён.") },
        confirmButton = { TextButton(onClick = onRefresh) { Text("Обновить", color = MaterialTheme.wave.accent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Назад") } },
    )
}
