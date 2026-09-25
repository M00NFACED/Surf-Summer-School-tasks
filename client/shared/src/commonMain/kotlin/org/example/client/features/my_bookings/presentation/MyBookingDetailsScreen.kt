package org.example.client.features.my_bookings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import org.example.client.core.theme.wave
import org.example.client.core.ui.WaveBadge
import org.example.client.core.ui.WaveBadgeRow
import org.example.client.core.ui.WaveCardColumn
import org.example.client.core.ui.WavePrimaryButton
import org.example.client.core.ui.WaveSlotImage
import org.example.client.core.ui.WaveTopBar
import org.example.client.core.ui.artworkSeed
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.my_bookings.domain.MyBooking
import org.example.client.features.schedule.presentation.artworkStyle
import org.example.client.features.schedule.presentation.badgeTone
import org.example.client.features.schedule.presentation.formatSlotCardDate
import org.example.client.features.schedule.presentation.formatSlotTimeRange

@Composable
fun MyBookingDetailsScreen(
    viewModel: MyBookingsViewModel,
    booking: MyBooking,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val colors = MaterialTheme.wave
    val now = remember { Clock.System.now() }
    val canCancel = booking.status == BookingStatus.CONFIRMED && now <= booking.cancelDeadline
    var sheetVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        WaveTopBar(title = "Детали записи", onBack = onBack)
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box {
                WaveSlotImage(
                    style = booking.slot.format.artworkStyle(),
                    seed = artworkSeed(booking.slot.id, booking.slot.instructor.id),
                )
                BookingStatusBadge(
                    booking = booking,
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                )
            }
            WaveBadgeRow {
                WaveBadge(booking.slot.format.displayName, booking.slot.format.badgeTone())
            }
            Text(
                text = formatSlotCardDate(booking.slot.startsAt),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
            Text(
                text = formatSlotTimeRange(booking.slot.startsAt, booking.slot.endsAt),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            Text("Инструктор: ${booking.slot.instructor.fullName}", style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
            Text("Адрес: ${booking.slot.address}", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            DetailsCard(booking)
            Text(
                text = "Оплата на месте: наличные или перевод",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            when {
                canCancel -> WavePrimaryButton(
                    text = "Отменить",
                    onClick = { sheetVisible = true },
                    loading = state.isCancelling,
                )
                booking.status == BookingStatus.CONFIRMED -> Text(
                    text = "Отмена доступна не позднее чем за 2 часа до начала тренировки",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    if (sheetVisible) {
        CancelBookingSheet(
            isCancelling = state.isCancelling,
            cancellationError = state.cancellationError,
            onConfirm = {
                viewModel.cancel(booking)
                sheetVisible = false
            },
            onDismiss = { sheetVisible = false },
        )
    }
}

@Composable
private fun DetailsCard(booking: MyBooking) {
    val colors = MaterialTheme.wave
    WaveCardColumn {
        DetailsRow("Скальники", booking.equipment.shoes.label())
        DetailsRow("Страховочная система", booking.equipment.harness.label())
        DetailsRow("Оплата", "На месте")
        booking.cancellationReason?.let { DetailsRow("Причина отмены", it) }
    }
}

@Composable
private fun DetailsRow(label: String, value: String) {
    val colors = MaterialTheme.wave
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.cardInner).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
    }
}

private fun EquipmentSelection.label(): String = when (this) {
    EquipmentSelection.Own -> "Своё"
    is EquipmentSelection.Rental -> "Прокат"
}
