package org.example.client.features.booking.domain

data class EquipmentOption(
    val id: String,
    val type: EquipmentType,
    val name: String,
    val size: String?,
    val price: Double,
    val currency: String,
    val totalQuantity: Int,
    val availableQuantity: Int,
    val isActive: Boolean,
) {
    val isAvailable: Boolean
        get() = isActive && availableQuantity > 0
}
