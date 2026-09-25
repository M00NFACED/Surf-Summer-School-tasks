package org.example.client.features.booking.data

import kotlinx.datetime.Instant
import org.example.client.features.booking.domain.BookingConfirmation
import org.example.client.features.booking.domain.BookingIntent
import org.example.client.features.booking.domain.BookingStatus
import org.example.client.features.booking.domain.EquipmentOption as DomainEquipmentOption
import org.example.client.features.booking.domain.EquipmentSelection
import org.example.client.features.booking.domain.EquipmentType
import org.example.client.features.booking.domain.PaymentMethod
import org.example.client.features.booking.domain.SlotDetailsItem
import org.example.client.features.schedule.domain.Instructor
import org.example.client.features.schedule.domain.SlotStatus
import org.example.client.features.schedule.domain.TrainingFormat
import org.example.client.features.schedule.domain.TrainingSlotItem
import org.example.client.features.schedule.data.ScheduleMapper

object BookingMapper {
    fun toDomain(details: TrainingSlotDetails): SlotDetailsItem = SlotDetailsItem(
        slot = TrainingSlotItem(
            id = details.id,
            startsAt = Instant.parse(details.startsAt),
            endsAt = Instant.parse(details.endsAt),
            format = TrainingFormat.fromApi(details.format) ?: error("Unknown training format"),
            zone = details.zone,
            address = details.address,
            instructor = Instructor(details.instructor.id, details.instructor.fullName, details.instructor.isActive),
            capacity = details.capacity,
            availablePlaces = details.availablePlaces,
            status = SlotStatus.fromApi(details.status) ?: error("Unknown slot status"),
            cancellationReason = details.cancellationReason,
        ),
        equipmentOptions = details.equipmentOptions.map { option -> toDomain(option) },
    )

    fun toDomain(option: EquipmentOption): DomainEquipmentOption = DomainEquipmentOption(
        id = option.id,
        type = EquipmentType.fromApi(option.type) ?: error("Unknown equipment type"),
        name = option.name,
        size = option.size,
        price = option.price,
        currency = option.currency,
        totalQuantity = option.totalQuantity,
        availableQuantity = option.availableQuantity,
        isActive = option.isActive,
    )

    fun toDomain(response: BookingResponse): BookingConfirmation = BookingConfirmation(
        id = response.id,
        slot = ScheduleMapper.toDomain(response.slot),
        status = BookingStatus.fromApi(response.status) ?: error("Unknown booking status"),
        paymentMethod = PaymentMethod.ON_SITE,
        createdAt = Instant.parse(response.createdAt),
    )

    fun toRequest(intent: BookingIntent): CreateBookingRequest = CreateBookingRequest(
        slotId = intent.slotId,
        equipment = EquipmentSelectionsPayload(
            shoes = intent.shoes.toPayload(),
            harness = intent.harness.toPayload(),
        ),
        paymentMethod = intent.paymentMethod.apiValue,
    )

    private fun EquipmentSelection.toPayload(): EquipmentSelectionPayload = when (this) {
        EquipmentSelection.Own -> EquipmentSelectionPayload("own")
        is EquipmentSelection.Rental -> EquipmentSelectionPayload("rental", optionId)
    }
}
