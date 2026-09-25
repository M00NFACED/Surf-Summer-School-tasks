package org.example.client.features.booking.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EquipmentOption(
    @SerialName("id") val id: String,
    @SerialName("slot_id") val slotId: String? = null,
    @SerialName("type") val type: String,
    @SerialName("name") val name: String,
    @SerialName("size") val size: String? = null,
    @SerialName("price") val price: Double,
    @SerialName("currency") val currency: String = "RUB",
    @SerialName("total_quantity") val totalQuantity: Int,
    @SerialName("available_quantity") val availableQuantity: Int,
    @SerialName("is_active") val isActive: Boolean,
)
