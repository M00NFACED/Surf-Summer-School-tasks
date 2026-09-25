package org.example.client.features.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    Scaffold { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Вертикаль", style = MaterialTheme.typography.headlineMedium)
                Text("Войти в приложение", style = MaterialTheme.typography.titleLarge)
                Text("Введите номер, на который придёт SMS-код", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = validator.nationalDigits(phone),
                    onValueChange = { value -> onPhoneChange(validator.nationalDigits(value)) },
                    label = { Text("Телефон") },
                    placeholder = { Text("+7 (XXX) XXX-XX-XX") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = visualTransformation,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                )
                if (errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = onSubmit,
                    enabled = validator.isCompleteNational(phone) && !isLoading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.heightIn(min = 24.dp))
                    } else {
                        Text("Получить код")
                    }
                }
            }
        }
    }
}
