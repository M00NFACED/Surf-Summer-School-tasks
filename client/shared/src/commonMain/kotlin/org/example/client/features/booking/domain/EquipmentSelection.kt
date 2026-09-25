package org.example.client.features.booking.domain

sealed interface EquipmentSelection {
    data object Own : EquipmentSelection
    data class Rental(val optionId: String) : EquipmentSelection
}
