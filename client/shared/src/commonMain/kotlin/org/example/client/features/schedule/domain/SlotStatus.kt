package org.example.client.features.schedule.domain

enum class SlotStatus(val apiValue: String) {
    AVAILABLE("available"),
    CANCELLED("cancelled"),
    COMPLETED("completed"),
    ;

    companion object {
        fun fromApi(value: String): SlotStatus? = values().firstOrNull { it.apiValue == value }
    }
}
