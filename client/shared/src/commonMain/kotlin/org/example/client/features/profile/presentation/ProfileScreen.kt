package org.example.client.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WaveOutlineButton
import org.example.client.core.ui.WaveTopBar
import org.example.client.features.auth.domain.Client
import org.example.client.features.auth.domain.PhoneNumberValidator

private enum class ProfileDialog { RULES, SUPPORT, VERSION }

@Composable
fun ProfileScreen(
    client: Client?,
    onLogout: () -> Unit,
) {
    val colors = MaterialTheme.wave
    val validator = remember { PhoneNumberValidator() }
    val phone = client?.phone?.let(validator::format) ?: "не указан"
    var dialog by remember { mutableStateOf<ProfileDialog?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        WaveTopBar(title = "Профиль")
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileCard(label = "Телефон", value = phone)
            Column(modifier = Modifier.fillMaxWidth()) {
                ProfileRow("Правила клуба", onClick = { dialog = ProfileDialog.RULES })
                HorizontalDivider(color = colors.border)
                ProfileRow("Поддержка", onClick = { dialog = ProfileDialog.SUPPORT })
                HorizontalDivider(color = colors.border)
                ProfileRow("Версия приложения", value = "1.0.0", onClick = { dialog = ProfileDialog.VERSION })
            }
            Column(modifier = Modifier.weight(1f)) {}
            WaveOutlineButton(text = "Выйти", onClick = onLogout)
        }
    }

    when (dialog) {
        ProfileDialog.RULES -> ClubRulesSheet(onDismiss = { dialog = null })
        ProfileDialog.SUPPORT -> SupportDialog(onDismiss = { dialog = null })
        ProfileDialog.VERSION -> VersionDialog(onDismiss = { dialog = null })
        null -> Unit
    }
}

@Composable
private fun ProfileCard(label: String, value: String) {
    val colors = MaterialTheme.wave
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.card).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        }
    }
}

@Composable
private fun ProfileRow(
    title: String,
    value: String? = null,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.wave
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            value?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary) }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.iconMuted,
            )
        }
    }
}
