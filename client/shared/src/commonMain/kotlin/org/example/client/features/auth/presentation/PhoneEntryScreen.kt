package org.example.client.features.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WavePrimaryButton
import org.example.client.features.auth.domain.PhoneNumberValidator

@Composable
fun PhoneEntryScreen(
    phone: String,
    isLoading: Boolean,
    errorMessage: String?,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val validator = remember { PhoneNumberValidator() }
    val visualTransformation = remember { PhoneNumberVisualTransformation(validator) }
    val colors = MaterialTheme.wave

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Вход",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Войдите по номеру телефона, чтобы записаться на тренировку",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 28.dp),
        )
        Text("Телефон", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
        OutlinedTextField(
            value = validator.nationalDigits(phone),
            onValueChange = { value -> onPhoneChange(validator.nationalDigits(value)) },
            placeholder = { Text("+7 999 999-99-99", color = colors.iconMuted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = visualTransformation,
            isError = errorMessage != null,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.cardInner,
                unfocusedContainerColor = colors.cardInner,
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Text(
            text = "Нажимая «Получить код», вы соглашаетесь с условиями сервиса",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            WavePrimaryButton(
                text = "Получить код",
                onClick = onSubmit,
                enabled = validator.isCompleteNational(phone) && !isLoading,
                loading = isLoading,
            )
        }
    }
}
