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
        val formatted = validator.format(text.text)
        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = PhoneNumberOffsetMapping(text.text, formatted, validator),
        )
    }
}

private class PhoneNumberOffsetMapping(
    private val original: String,
    private val formatted: String,
    private val validator: PhoneNumberValidator,
) : OffsetMapping {
    private val originalDigitEnds: List<Int> = buildList {
        var previousCount = 0
        original.forEachIndexed { index, _ ->
            val count = validator.nationalDigits(original.substring(0, index + 1)).length
            if (count > previousCount) {
                add(index + 1)
                previousCount = count
            }
        }
    }

    override fun originalToTransformed(offset: Int): Int {
        val safeOffset = offset.coerceIn(0, original.length)
        val digitCount = validator.nationalDigits(original.take(safeOffset)).length
        return when {
            formatted.isEmpty() -> 0
            digitCount <= 0 -> 4
            digitCount <= 3 -> 4 + digitCount
            digitCount <= 6 -> digitCount + 6
            digitCount <= 8 -> digitCount + 7
            else -> digitCount + 8
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        val safeOffset = offset.coerceIn(0, formatted.length)
        val digitCount = if (formatted.isEmpty() || safeOffset <= 4) {
            0
        } else {
            formatted.substring(4, safeOffset).count(Char::isDigit)
        }
        return if (digitCount == 0) 0 else originalDigitEnds.getOrNull(digitCount - 1) ?: original.length
    }
}
