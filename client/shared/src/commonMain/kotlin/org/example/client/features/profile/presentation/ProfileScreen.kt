package org.example.client.features.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.features.auth.domain.Client
import org.example.client.features.auth.domain.PhoneNumberValidator

@Composable
fun ProfileScreen(
    client: Client?,
    onLogout: () -> Unit,
) {
    val validator = remember { PhoneNumberValidator() }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Профиль", style = MaterialTheme.typography.headlineSmall)
        Text("Телефон: ${client?.phone?.let(validator::format) ?: "не указан"}")
        Text("Раздел профиля доступен после подключения сервиса клиента.")
        Button(onClick = onLogout) { Text("Выйти") }
    }
}
