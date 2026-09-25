package org.example.client.features.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WavePrimaryButton
import org.example.client.core.ui.WaveTopBar
import org.example.client.features.auth.domain.PhoneNumberValidator

@Composable
fun OtpVerificationScreen(
    phone: String,
    code: String,
    retryAfterSeconds: Int,
    isLoading: Boolean,
    errorMessage: String?,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val validator = remember { PhoneNumberValidator() }
    val colors = MaterialTheme.wave

    Column(modifier = Modifier.fillMaxSize()) {
        WaveTopBar(title = "Подтверждение", onBack = onBack)
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Подтверждение",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Мы отправили код на ${validator.format(phone)}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 28.dp),
            )
            Text("Код из SMS", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                repeat(6) { index -> OtpDigitField(index, code, focusManager, onCodeChange) }
            }
            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = if (retryAfterSeconds > 0) "Отправить код повторно (00:${retryAfterSeconds.toString().padStart(2, '0')})"
                else "Отправить код повторно",
                style = MaterialTheme.typography.bodyMedium,
                color = if (retryAfterSeconds > 0) colors.iconMuted else colors.accent,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )
            WavePrimaryButton(
                text = "Подтвердить",
                onClick = onVerify,
                enabled = code.length == 6 && !isLoading,
                loading = isLoading,
            )
        }
    }
}

@Composable
private fun RowScope.OtpDigitField(
    index: Int,
    code: String,
    focusManager: androidx.compose.ui.focus.FocusManager,
    onCodeChange: (String) -> Unit,
) {
    val colors = MaterialTheme.wave
    val digit = code.getOrNull(index)?.toString().orEmpty()
    OutlinedTextField(
        value = digit,
        onValueChange = { value ->
            val input = value.filter(Char::isDigit).lastOrNull()?.toString().orEmpty()
            val characters = code.padEnd(6, ' ').toCharArray()
            if (input.isEmpty() && digit.isEmpty() && index > 0) {
                characters[index - 1] = ' '
            } else {
                characters[index] = if (input.isEmpty()) ' ' else input.single()
            }
            onCodeChange(characters.joinToString("").filterNot(Char::isWhitespace))
            if (input.isEmpty()) {
                focusManager.moveFocus(FocusDirection.Previous)
            } else if (index < 5) {
                focusManager.moveFocus(FocusDirection.Next)
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(textAlign = TextAlign.Center),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.accent,
            focusedContainerColor = colors.cardInner,
            unfocusedContainerColor = colors.cardInner,
        ),
        modifier = Modifier.weight(1f).width(48.dp).heightIn(min = 56.dp),
    )
}
