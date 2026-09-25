package org.example.client.features.auth.presentation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import org.example.client.features.auth.domain.PhoneNumberValidator

class PhoneNumberVisualTransformation(
    private val validator: PhoneNumberValidator = PhoneNumberValidator(),
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = validator.nationalDigits(text.text)
        return TransformedText(
            text = AnnotatedString(validator.format(digits)),
            offsetMapping = PhoneNumberOffsetMapping(text.text),
        )
    }
}

private class PhoneNumberOffsetMapping(
    private val original: String,
) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        if (original.isEmpty()) return 2
        val digitCount = original.take(offset.coerceIn(0, original.length)).count(Char::isDigit)
        return when {
            digitCount <= 3 -> 4 + digitCount
            digitCount <= 6 -> digitCount + 6
            else -> digitCount + 7
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        val digitCount = when {
            offset <= 4 -> 0
            offset <= 7 -> (offset - 4).coerceAtMost(3)
            offset <= 9 -> 3
            offset <= 12 -> 3 + (offset - 9).coerceAtMost(3)
            offset <= 15 -> 6 + (offset - 12).coerceAtMost(3)
            else -> 8 + (offset - 15).coerceAtMost(2)
        }
        return digitCount.coerceIn(0, original.length)
    }
}
