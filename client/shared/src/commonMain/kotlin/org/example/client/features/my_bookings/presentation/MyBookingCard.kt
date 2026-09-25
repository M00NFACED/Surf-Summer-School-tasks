package org.example.client.features.my_bookings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.features.booking.presentation.formatBookingDateTime
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.my_bookings.domain.MyBooking

@Composable
fun MyBookingCard(
    booking: MyBooking,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(formatBookingDateTime(booking.slot.startsAt), style = MaterialTheme.typography.titleMedium)
            Text(booking.slot.format.displayName)
            Text("Инструктор: ${booking.slot.instructor.fullName}")
            Text("Статус: ${booking.status.displayName()}")
            booking.cancellationReason?.let { Text("Причина: $it", color = MaterialTheme.colorScheme.error) }
            Surface(color = MaterialTheme.colorScheme.tertiaryContainer) {
                Text("Оплата на месте", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
        }
    }
}

private fun BookingStatus.displayName(): String = when (this) {
    BookingStatus.CONFIRMED -> "Подтверждена"
    BookingStatus.CANCELLED_BY_CLIENT -> "Отменена клиентом"
    BookingStatus.CANCELLED_BY_VENUE -> "Отменена скалодромом"
    BookingStatus.COMPLETED -> "Завершена"
    BookingStatus.RATED -> "Оценена"
}
