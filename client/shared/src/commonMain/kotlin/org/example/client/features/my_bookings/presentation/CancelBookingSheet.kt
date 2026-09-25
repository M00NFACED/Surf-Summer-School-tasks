package org.example.client.features.my_bookings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.example.client.core.ui.WaveOutlineButton
import org.example.client.core.ui.WavePrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelBookingSheet(
    isCancelling: Boolean,
    cancellationError: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.wave
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = { if (!isCancelling) onDismiss() },
        sheetState = sheetState,
        containerColor = colors.pill,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Отменить запись?",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Отменить можно не позднее чем за 2 часа до начала тренировки. " +
                    "Место сразу освободится для других клиентов.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
            )
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.chip).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Ранняя отмена", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                Text(
                    text = "Отмена до дедлайна проходит без штрафа.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            if (cancellationError != null) {
                Text(cancellationError, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            WavePrimaryButton(
                text = "Оставить запись",
                onClick = onDismiss,
                enabled = !isCancelling,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            WaveOutlineButton(
                text = "Подтвердить отмену",
                onClick = onConfirm,
                enabled = !isCancelling,
            )
        }
    }
}
