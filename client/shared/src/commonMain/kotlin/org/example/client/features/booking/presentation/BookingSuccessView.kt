package org.example.client.features.booking.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WaveCardColumn
import org.example.client.core.ui.WaveOutlineButton
import org.example.client.core.ui.WavePrimaryButton
import org.example.client.core.ui.WaveTopBar

@Composable
fun BookingSuccessView(
    onMyBookings: () -> Unit,
    onDone: () -> Unit,
) {
    val colors = MaterialTheme.wave
    Column(modifier = Modifier.fillMaxSize()) {
        WaveTopBar(title = "Вы записаны")
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WaveCardColumn {
                Text(
                    text = "Запись подтверждена",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                )
                Text(
                    text = "Одна бронь — один человек. Оплата на месте: наличные или перевод.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {}
            WavePrimaryButton(text = "Мои бронирования", onClick = onMyBookings)
            WaveOutlineButton(text = "Готово", onClick = onDone)
        }
    }
}
