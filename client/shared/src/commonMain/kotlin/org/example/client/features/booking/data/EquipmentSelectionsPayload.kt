package org.example.client.features.booking.data

import kotlinx.serialization.Serializable

@Serializable
data class EquipmentSelectionsPayload(
    val shoes: EquipmentSelectionPayload,
    val harness: EquipmentSelectionPayload,
)
