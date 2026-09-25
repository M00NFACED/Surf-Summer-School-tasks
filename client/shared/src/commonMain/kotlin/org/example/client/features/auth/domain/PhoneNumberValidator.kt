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

    fun format(raw: String): String {
        val digits = raw.filter { it.isDigit() }.let {
            if (it.startsWith("7") || it.startsWith("8")) it.drop(1) else it
        }.take(10)
        if (digits.isEmpty()) return ""
        val sb = StringBuilder("+7 (")
        val p1 = digits.substring(0, minOf(3, digits.length))
        sb.append(p1)
        if (digits.length > 3) {
            sb.append(") ")
            val p2 = digits.substring(3, minOf(6, digits.length))
            sb.append(p2)
        }
        if (digits.length > 6) {
            sb.append("-")
            val p3 = digits.substring(6, minOf(8, digits.length))
            sb.append(p3)
        }
        if (digits.length > 8) {
            sb.append("-")
            val p4 = digits.substring(8, minOf(10, digits.length))
            sb.append(p4)
        }
        return sb.toString()
    }
}
