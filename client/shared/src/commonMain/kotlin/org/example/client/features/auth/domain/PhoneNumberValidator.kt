package org.example.client.features.auth.domain

class PhoneNumberValidator {
    private val pattern = Regex("^\\+7[0-9]{10}$")

    fun isValid(value: String): Boolean = pattern.matches(value)

    fun normalize(value: String): String {
        val digits = value.filter(Char::isDigit)
        if (digits.isEmpty()) return ""
        val national = digits.removePrefix("7")
        if (national.isEmpty()) return "+7"
        return "+7${national.take(10)}"
    }

    fun format(value: String): String {
        val normalized = normalize(value)
        if (normalized.length < 2) return "+7"
        val digits = normalized.drop(1)
        return when {
            digits.length <= 3 -> "+7 (${digits}"
            digits.length <= 6 -> "+7 (${digits.take(3)}) ${digits.drop(3)}"
            else -> "+7 (${digits.take(3)}) ${digits.substring(3, 6)}-${digits.substring(6, 8)}-${digits.substring(8, 10)}"
        }
    }
}
