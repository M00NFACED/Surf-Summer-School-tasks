package org.example.client.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave

internal const val SupportPhone = "+7 (999) 000-00-00"
internal const val SupportTelegram = "@vertical_climb"
internal val AppVersionLabel = "Скалодром Вертикаль v1.0.0 (Surf Summer School 2026)"

@Composable
fun SupportDialog(onDismiss: () -> Unit) {
    val colors = MaterialTheme.wave
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Поддержка", color = colors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ContactRow(icon = Icons.Default.Call, label = "Телефон", value = SupportPhone)
                ContactRow(icon = Icons.AutoMirrored.Filled.Send, label = "Telegram", value = SupportTelegram)
                Text(
                    text = "Ежедневно с 10:00 до 22:00, ответ в течение 15 минут.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Понятно", color = colors.accent) }
        },
    )
}

@Composable
fun VersionDialog(onDismiss: () -> Unit) {
    val colors = MaterialTheme.wave
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Версия приложения", color = colors.textPrimary) },
        text = { Text(AppVersionLabel, color = colors.textPrimary) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = colors.accent) }
        },
    )
}

@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    val colors = MaterialTheme.wave
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.chip).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.accent)
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        }
    }
}
