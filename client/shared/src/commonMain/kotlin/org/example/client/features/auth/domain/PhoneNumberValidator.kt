package org.example.client.features.auth.domain

class PhoneNumberValidator {
    private val pattern = Regex("^\\+7[0-9]{10}$")

    fun isValid(value: String): Boolean = pattern.matches(value)

    fun nationalDigits(value: String): String = value
        .filter(Char::isDigit)
        .dropWhile { it == '7' || it == '8' }
        .take(10)

    fun isCompleteNational(value: String): Boolean = nationalDigits(value).length == 10

    fun normalize(value: String): String {
        val national = nationalDigits(value)
        return if (national.isEmpty()) "" else "+7$national"
    }

    fun format(value: String): String {
        val digits = nationalDigits(value)
        return when {
            digits.isEmpty() -> "+7"
            digits.length <= 3 -> "+7 ($digits"
            digits.length <= 6 -> "+7 (${digits.take(3)}) ${digits.drop(3)}"
            digits.length <= 8 -> "+7 (${digits.take(3)}) ${digits.substring(3, 6)}-${digits.drop(6)}"
            else -> "+7 (${digits.take(3)}) ${digits.substring(3, 6)}-${digits.substring(6, 8)}-${digits.substring(8, 10)}"
        }
    }
}
