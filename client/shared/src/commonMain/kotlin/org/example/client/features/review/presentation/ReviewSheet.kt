package org.example.client.features.review.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WavePrimaryButton
import org.example.client.core.ui.WaveRouteArtwork
import org.example.client.core.ui.WaveStarRating
import org.example.client.core.ui.artworkSeed
import org.example.client.features.my_bookings.domain.MyBooking
import org.example.client.features.schedule.presentation.artworkStyle
import org.example.client.features.schedule.presentation.formatSlotCardDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSheet(
    booking: MyBooking,
    state: ReviewState,
    onScoreSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.wave
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        sheetState = sheetState,
        containerColor = colors.pill,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WaveRouteArtwork(
                    style = booking.slot.format.artworkStyle(),
                    seed = artworkSeed(booking.slot.instructor.id, booking.slot.id),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colors.accentSoft),
                )
                Column {
                    Text(
                        text = "Оценить инструктора",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = booking.slot.instructor.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                }
            }
            Text(
                text = "${formatSlotCardDate(booking.slot.startsAt)} · ${booking.slot.format.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            WaveStarRating(
                score = state.score,
                onScoreSelected = onScoreSelected,
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = when {
                    state.score == 0 -> "Выберите от 1 до 5 звёзд"
                    else -> "Ваша оценка: ${state.score} из 5"
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.isOffline) {
                Text(
                    text = "Оценка отправится после восстановления сети",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            WavePrimaryButton(
                text = "Отправить отзыв",
                onClick = onSubmit,
                enabled = state.canSubmit && !state.isOffline,
                loading = state.isSubmitting,
            )
        }
    }
}
