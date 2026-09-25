package org.example.client.features.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave

data class ClubRule(val title: String, val description: String)

internal val clubRules = listOf(
    ClubRule(
        title = "Техника безопасности",
        description = "Используйте страховочную систему на всех трассах и следуйте командам инструктора. " +
            "Запрещено прыгать на натянутые страховочные системы и оставлять их без присмотра.",
    ),
    ClubRule(
        title = "Сменная обувь",
        description = "На скалодроме используется только сменная обувь. Свои кроссовки и ботинки оставьте в раздевалке.",
    ),
    ClubRule(
        title = "Магнезия",
        description = "Перед началом тренировки очистите руки магнезией. Скалодром предоставляет мел бесплатно.",
    ),
    ClubRule(
        title = "Отмена записи",
        description = "Отменить бронь можно не позднее чем за 2 часа до начала тренировки, чтобы место успело освободиться.",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubRulesSheet(onDismiss: () -> Unit) {
    val colors = MaterialTheme.wave
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.pill,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Правила клуба",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            clubRules.forEach { rule ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = rule.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = rule.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
