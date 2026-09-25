package org.example.client.features.booking.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.features.booking.domain.BookingConfirmation
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.domain.EquipmentType
import org.example.client.features.booking.domain.SlotDetailsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onSchedule: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val details by viewModel.details.collectAsState()
    val shoes by viewModel.shoes.collectAsState()
    val harness by viewModel.harness.collectAsState()
    val isSubmitting = state is BookingState.Submitting

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оформление брони") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (val current = state) {
                is BookingState.Success -> BookingSuccess(current.confirmation, onSchedule)
                BookingState.Initial, BookingState.Loading -> BookingSkeleton()
                BookingState.Forbidden -> Text("Сессия истекла. Войдите снова")
                is BookingState.NetworkError -> {
                    Text(current.message, color = MaterialTheme.colorScheme.error)
                    details?.let { BookingForm(it, shoes, harness, isSubmitting, true, viewModel) }
                }
                is BookingState.ValidationError -> {
                    Text(current.message, color = MaterialTheme.colorScheme.error)
                    details?.let { BookingForm(it, shoes, harness, isSubmitting, false, viewModel) }
                }
                is BookingState.DetailsLoaded -> BookingForm(current.details, shoes, harness, isSubmitting, false, viewModel)
                is BookingState.ConflictError -> {
                    Text(current.message, color = MaterialTheme.colorScheme.error)
                    details?.let { BookingForm(it, shoes, harness, isSubmitting, false, viewModel) }
                }
                is BookingState.Submitting -> details?.let { BookingForm(it, shoes, harness, true, false, viewModel) } ?: BookingSkeleton()
            }
        }
    }

    if (state is BookingState.ConflictError && details != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Место занято") },
            text = { Text("Место только что занято другим клиентом. Остаток обновлён.") },
            confirmButton = { TextButton(onClick = { viewModel.loadSlot(details!!.id) }) { Text("Обновить") } },
            dismissButton = { TextButton(onClick = onBack) { Text("Назад") } },
        )
    }
}

@Composable
private fun BookingForm(
    details: SlotDetailsItem,
    shoes: EquipmentSelection?,
    harness: EquipmentSelection?,
    submitting: Boolean,
    blocked: Boolean,
    viewModel: BookingViewModel,
) {
    val slot = details.slot
    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(formatBookingDateTime(slot.startsAt), style = MaterialTheme.typography.titleMedium)
            Text("${formatBookingDateTime(slot.startsAt)} — ${formatBookingDateTime(slot.endsAt)}")
            Text("${slot.format.displayName} · ${slot.instructor.fullName}")
            Text("Одна бронь — один человек")
        }
    }
    EquipmentPicker(
        title = "Скальники",
        type = EquipmentType.CLIMBING_SHOES,
        options = details.equipmentOptions,
        selection = shoes,
        onSelection = viewModel::selectShoes,
    )
    EquipmentPicker(
        title = "Страховочная система",
        type = EquipmentType.HARNESS_SYSTEM,
        options = details.equipmentOptions,
        selection = harness,
        onSelection = viewModel::selectHarness,
    )
    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
        Text("Оплата на месте", modifier = Modifier.padding(16.dp))
    }
    Button(
        onClick = viewModel::submit,
        enabled = details.isAvailable && shoes != null && harness != null && !submitting && !blocked,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (submitting) CircularProgressIndicator() else Text("Подтвердить запись")
    }
}

@Composable
private fun BookingSuccess(confirmation: BookingConfirmation, onSchedule: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Бронь подтверждена", style = MaterialTheme.typography.headlineSmall)
        Text("Номер брони: ${confirmation.id}")
        Text("Статус: Подтверждена")
        Text("Оплата на месте")
        Button(onClick = onSchedule, modifier = Modifier.fillMaxWidth()) { Text("Вернуться в расписание") }
    }
}
