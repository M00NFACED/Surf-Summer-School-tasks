package org.example.client.features.auth.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthUseCasesTest {
    private val requestCode = RequestCodeUseCase()
    private val verifyCode = VerifyCodeUseCase()

    @Test
    fun requestCodeAcceptsValidPhone() {
        assertEquals(Result.success("+79991234567"), requestCode("+79991234567"))
    }

    @Test
    fun requestCodeRejectsEmptyPhone() {
        assertTrue(requestCode("").isFailure)
    }

    @Test
    fun verifyCodeAcceptsSixDigits() {
        assertEquals(Result.success("+79991234567" to "123456"), verifyCode("+79991234567", "123456"))
    }

    @Test
    fun verifyCodeRejectsShortOrNonNumericCode() {
        assertTrue(verifyCode("+79991234567", "12345").isFailure)
        assertTrue(verifyCode("+79991234567", "12a456").isFailure)
    }
}
