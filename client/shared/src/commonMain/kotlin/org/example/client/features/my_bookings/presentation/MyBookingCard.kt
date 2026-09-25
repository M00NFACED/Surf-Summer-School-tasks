package org.example.client.features.my_bookings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WaveBadge
import org.example.client.core.ui.WaveBadgeRow
import org.example.client.core.ui.WaveBadgeTone
import org.example.client.core.ui.WaveSlotImage
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.my_bookings.domain.MyBooking
import org.example.client.features.schedule.presentation.badgeTone
import org.example.client.features.schedule.presentation.formatSlotCardDate

@Composable
fun MyBookingCard(booking: MyBooking, onClick: (String) -> Unit) {
    val colors = MaterialTheme.wave
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.card)
            .clickable { onClick(booking.id) },
    ) {
        Box {
            WaveSlotImage(modifier = Modifier.fillMaxWidth().height(168.dp).padding(8.dp))
            BookingStatusBadge(
                booking = booking,
                modifier = Modifier.align(Alignment.TopStart).padding(18.dp),
            )        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        ) {
            WaveBadgeRow {
                WaveBadge(booking.slot.format.displayName, booking.slot.format.badgeTone())
            }
            Text(
                text = formatSlotCardDate(booking.slot.startsAt),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
            Text(
                text = "Инструктор: ${booking.slot.instructor.fullName}",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
internal fun BookingStatusBadge(booking: MyBooking, modifier: Modifier = Modifier) {
    WaveBadge(
        text = booking.status.badgeText(),
        tone = booking.status.badgeTone(),
        modifier = modifier,
    )
}

internal fun BookingStatus.badgeText(): String = when (this) {
    BookingStatus.CONFIRMED -> "Активна"
    BookingStatus.CANCELLED_BY_CLIENT -> "Отменена"
    BookingStatus.CANCELLED_BY_VENUE -> "Отменена залом"
    BookingStatus.COMPLETED -> "Завершена"
    BookingStatus.RATED -> "Оценена"
}

internal fun BookingStatus.badgeTone(): WaveBadgeTone = when (this) {
    BookingStatus.CONFIRMED -> WaveBadgeTone.GREEN
    BookingStatus.RATED, BookingStatus.COMPLETED -> WaveBadgeTone.ACCENT
    BookingStatus.CANCELLED_BY_CLIENT, BookingStatus.CANCELLED_BY_VENUE -> WaveBadgeTone.YELLOW
}
