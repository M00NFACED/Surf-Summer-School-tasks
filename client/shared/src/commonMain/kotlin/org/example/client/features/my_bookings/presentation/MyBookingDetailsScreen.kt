package org.example.client.features.my_bookings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.presentation.formatBookingDateTime
import org.example.client.features.my_bookings.domain.MyBooking

@Composable
fun MyBookingDetailsScreen(
    viewModel: MyBookingsViewModel,
    booking: MyBooking,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showConfirmation by remember { mutableStateOf(false) }
    val canCancel = booking.status == BookingStatus.CONFIRMED && Clock.System.now() <= booking.cancelDeadline

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("Назад") }
        Text("Детали записи", style = MaterialTheme.typography.headlineSmall)
        Text(formatBookingDateTime(booking.slot.startsAt), style = MaterialTheme.typography.titleMedium)
        Text("${formatBookingDateTime(booking.slot.startsAt)} — ${formatBookingDateTime(booking.slot.endsAt)}")
        Text(booking.slot.format.displayName)
        Text("Инструктор: ${booking.slot.instructor.fullName}")
        Text("Адрес: ${booking.slot.address}")
        Text("Статус: ${booking.status.label()}")
        booking.cancellationReason?.let { Text("Причина: $it", color = MaterialTheme.colorScheme.error) }
        Text("Скальники: ${booking.equipment.shoes.label()}")
        Text("Страховочная система: ${booking.equipment.harness.label()}")
        Text("Оплата на месте")
        if (state.cancellationError != null) {
            Text(state.cancellationError!!, color = MaterialTheme.colorScheme.error)
        }
        if (canCancel) {
            Button(
                onClick = { showConfirmation = true },
                enabled = !state.isCancelling,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.isCancelling) "Отменяем…" else "Отменить запись")
            }
        } else if (booking.status == BookingStatus.CONFIRMED) {
            Text("Отмена доступна не позднее чем за 2 часа до начала тренировки", color = MaterialTheme.colorScheme.error)
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Вернуться к списку") }
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { if (!state.isCancelling) showConfirmation = false },
            title = { Text("Отменить запись?") },
            text = { Text("Отменить бронь на ${formatBookingDateTime(booking.slot.startsAt)}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmation = false
                        viewModel.cancel(booking)
                    },
                    enabled = !state.isCancelling,
                ) { Text("Отменить") }
            },
            dismissButton = { TextButton(onClick = { showConfirmation = false }) { Text("Оставить") } },
        )
    }
}

private fun BookingStatus.label(): String = when (this) {
    BookingStatus.CONFIRMED -> "Подтверждена"
    BookingStatus.CANCELLED_BY_CLIENT -> "Отменена клиентом"
    BookingStatus.CANCELLED_BY_VENUE -> "Отменена скалодромом"
    BookingStatus.COMPLETED -> "Завершена"
    BookingStatus.RATED -> "Оценена"
}

private fun EquipmentSelection.label(): String = when (this) {
    EquipmentSelection.Own -> "Свои"
    is EquipmentSelection.Rental -> "Прокат"
}
