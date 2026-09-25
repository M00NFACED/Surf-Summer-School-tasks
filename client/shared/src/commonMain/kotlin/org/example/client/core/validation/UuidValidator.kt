package org.example.client.core.validation

object UuidValidator {
    private val canonicalPattern = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    fun normalize(value: String): String? = value.trim().takeIf { canonicalPattern.matches(it) }

    fun isValid(value: String): Boolean = normalize(value) != null
}
