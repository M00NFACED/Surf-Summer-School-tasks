package org.example.client.features.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OtpEntryScreen(
    phone: String,
    code: String,
    retryAfterSeconds: Int,
    isLoading: Boolean,
    errorMessage: String?,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Введите код из SMS", style = MaterialTheme.typography.titleLarge)
            Text(maskedPhone(phone), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(6) { index ->
                    val digit = code.getOrNull(index)?.toString().orEmpty()
                    OutlinedTextField(
                        value = digit,
                        onValueChange = { value ->
                            val input = value.filter(Char::isDigit).lastOrNull()?.toString().orEmpty()
                            val characters = code.padEnd(6, ' ').toCharArray()
                            characters[index] = if (input.isEmpty()) ' ' else input.single()
                            onCodeChange(characters.joinToString("").filterNot(Char::isWhitespace))
                            if (input.isNotEmpty()) focusManager.moveFocus(FocusDirection.Next) else focusManager.moveFocus(FocusDirection.Previous)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier.width(40.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (errorMessage != null) Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Button(
                onClick = onVerify,
                enabled = code.length == 6 && !isLoading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                if (isLoading) CircularProgressIndicator() else Text("Подтвердить")
            }
            TextButton(
                onClick = onResend,
                enabled = retryAfterSeconds == 0 && !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (retryAfterSeconds > 0) "Повторить через $retryAfterSeconds сек." else "Отправить код ещё раз")
            }
        }
    }
}

private fun maskedPhone(phone: String): String {
    val digits = phone.filter(Char::isDigit).drop(1)
    return "+7 ••• •••-${digits.takeLast(2).padStart(2, '•')}"
}
