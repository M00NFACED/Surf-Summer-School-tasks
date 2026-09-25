package org.example.client.features.auth.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneNumberValidatorTest {
    private val validator = PhoneNumberValidator()

    @Test
    fun acceptsOnlyFullE164Number() {
        assertTrue(validator.isValid("+79991234567"))
        assertFalse(validator.isValid("+7999123456"))
        assertFalse(validator.isValid("79991234567"))
    }

    @Test
    fun normalizesPartialInputWithoutPadding() {
        assertEquals("+7999", validator.normalize("+7 (999)"))
        assertEquals("+79991234567", validator.normalize("+7 (999) 123-45-67"))
    }
}
