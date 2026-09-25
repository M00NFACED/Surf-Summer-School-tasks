package org.example.client.features.booking.domain

import kotlinx.datetime.Instant
import org.example.client.features.schedule.domain.TrainingSlotItem

data class SlotDetailsItem(
    val slot: TrainingSlotItem,
    val equipmentOptions: List<EquipmentOption>,
) {
    val id: String get() = slot.id
    val availablePlaces: Int get() = slot.availablePlaces
    val isAvailable: Boolean get() = slot.isAvailable
}
