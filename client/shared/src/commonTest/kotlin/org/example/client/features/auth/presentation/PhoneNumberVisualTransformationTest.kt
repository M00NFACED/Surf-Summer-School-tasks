package org.example.client.features.auth.presentation

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals

class PhoneNumberVisualTransformationTest {
    private val transformation = PhoneNumberVisualTransformation()

    @Test
    fun formatsNationalDigitsWithoutCountryDuplication() {
        val result = transformation.filter(AnnotatedString("9991234567"))

        assertEquals("+7 (999) 123-45-67", result.text.text)
    }

    @Test
    fun mapsCursorToFormattedPosition() {
        val result = transformation.filter(AnnotatedString("9991234567"))

        assertEquals(5, result.offsetMapping.originalToTransformed(1))
        assertEquals(17, result.offsetMapping.originalToTransformed(10))
        assertEquals(2, transformation.filter(AnnotatedString("")).offsetMapping.originalToTransformed(0))
    }
}
