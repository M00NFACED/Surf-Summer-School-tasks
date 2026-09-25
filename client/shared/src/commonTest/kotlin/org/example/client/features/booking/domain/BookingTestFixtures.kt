package org.example.client.features.booking.domain

import kotlinx.datetime.Instant
import org.example.client.features.schedule.domain.Instructor
import org.example.client.features.schedule.domain.SlotStatus
import org.example.client.features.schedule.domain.TrainingFormat
import org.example.client.features.schedule.domain.TrainingSlotItem

fun testSlotItem(availablePlaces: Int = 3, status: SlotStatus = SlotStatus.AVAILABLE) = TrainingSlotItem(
    id = "11111111-1111-4111-8111-111111111111",
    startsAt = Instant.parse("2026-09-25T10:00:00Z"),
    endsAt = Instant.parse("2026-09-25T12:00:00Z"),
    format = TrainingFormat.NOVICE_BOULDERING,
    zone = "Зона A",
    address = "Москва, ул. Вертикальная, 1",
    instructor = Instructor("22222222-2222-4222-8222-222222222222", "Анна Петрова", true),
    capacity = 8,
    availablePlaces = availablePlaces,
    status = status,
)

fun testSlotDetails() = SlotDetailsItem(
    slot = testSlotItem(),
    equipmentOptions = listOf(
        EquipmentOption(
            id = "33333333-3333-4333-8333-333333333333",
            type = EquipmentType.CLIMBING_SHOES,
            name = "Скальники",
            size = "36",
            price = 300.0,
            currency = "RUB",
            totalQuantity = 8,
            availableQuantity = 8,
            isActive = true,
        ),
        EquipmentOption(
            id = "44444444-4444-4444-8444-444444444444",
            type = EquipmentType.HARNESS_SYSTEM,
            name = "Страховочная система",
            size = "M",
            price = 200.0,
            currency = "RUB",
            totalQuantity = 6,
            availableQuantity = 6,
            isActive = true,
        ),
    ),
)

fun testConfirmation() = BookingConfirmation(
    id = "55555555-5555-4555-8555-555555555555",
    slot = testSlotItem(),
    status = BookingStatus.CONFIRMED,
    paymentMethod = PaymentMethod.ON_SITE,
    createdAt = Instant.parse("2026-09-20T12:00:00Z"),
)
