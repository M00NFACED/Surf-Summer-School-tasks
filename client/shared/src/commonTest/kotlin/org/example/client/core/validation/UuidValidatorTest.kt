package org.example.client.core.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UuidValidatorTest {
    @Test
    fun acceptsCanonicalUuidRegardlessOfVersionOrVariant() {
        val value = "00000000-0000-0000-0000-000000000001"

        assertEquals(value, UuidValidator.normalize(value))
        assertTrue(UuidValidator.isValid(value))
    }

    @Test
    fun trimsOuterWhitespaceAndRejectsMalformedValues() {
        val value = "01234567-89ab-cdef-0123-456789abcdef"

        assertEquals(value, UuidValidator.normalize("  $value  "))
        assertNull(UuidValidator.normalize("\"$value\""))
        assertFalse(UuidValidator.isValid("not-a-uuid"))
    }
}
