package org.example.client.features.my_bookings.domain

import org.example.client.features.booking.domain.EquipmentSelection

data class MyBookingEquipment(
    val shoes: EquipmentSelection,
    val harness: EquipmentSelection,
)
