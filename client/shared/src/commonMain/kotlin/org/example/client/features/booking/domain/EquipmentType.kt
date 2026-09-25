package org.example.client.features.booking.domain

enum class EquipmentType(val apiValue: String) {
    CLIMBING_SHOES("climbing_shoes"),
    HARNESS_SYSTEM("harness_system"),
    ;

    companion object {
        fun fromApi(value: String): EquipmentType? = values().firstOrNull { it.apiValue == value }
    }
}
