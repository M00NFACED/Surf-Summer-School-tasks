package org.example.client.features.auth.presentation

import androidx.compose.ui.text.AnnotatedString
import org.example.client.features.auth.domain.PhoneNumberValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhoneNumberVisualTransformationTest {
    private val validator = PhoneNumberValidator()
    private val transformation = PhoneNumberVisualTransformation(validator)

    @Test
    fun formatsEveryPartialLengthWithoutThrowing() {
        for (length in 0..10) {
            val raw = "9991234567".take(length)
            val result = transformation.filter(AnnotatedString(raw))

            assertEquals(validator.format(raw), result.text.text)
            for (offset in -1..(raw.length + 1)) {
                val transformedOffset = result.offsetMapping.originalToTransformed(offset)
                assertTrue(transformedOffset in 0..result.text.text.length)
            }
            for (offset in -1..(result.text.text.length + 1)) {
                val originalOffset = result.offsetMapping.transformedToOriginal(offset)
                assertTrue(originalOffset in 0..raw.length)
            }
        }
    }

    @Test
    fun mapsCursorToFormattedPosition() {
        val result = transformation.filter(AnnotatedString("9991234567"))

        assertEquals(5, result.offsetMapping.originalToTransformed(1))
        assertEquals(18, result.offsetMapping.originalToTransformed(10))
        assertEquals(0, transformation.filter(AnnotatedString("")).offsetMapping.originalToTransformed(0))
    }
}
