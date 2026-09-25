package org.example.client.features.booking.domain

data class BookingIntent(
    val slotId: String,
    val shoes: EquipmentSelection,
    val harness: EquipmentSelection,
    val paymentMethod: PaymentMethod = PaymentMethod.ON_SITE,
)
