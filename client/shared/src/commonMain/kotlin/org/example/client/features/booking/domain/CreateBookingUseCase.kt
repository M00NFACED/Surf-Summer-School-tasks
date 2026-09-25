package org.example.client.features.booking.domain

import org.example.client.core.validation.UuidValidator
import org.example.client.features.schedule.domain.SlotStatus

class CreateBookingUseCase(
    private val gateway: BookingGateway,
) {
    suspend operator fun invoke(
        slotId: String,
        slotStatus: SlotStatus,
        availablePlaces: Int,
        shoes: EquipmentSelection?,
        harness: EquipmentSelection?,
        availableShoeOptionIds: Set<String> = emptySet(),
        availableHarnessOptionIds: Set<String> = emptySet(),
    ): Result<BookingConfirmation> {
        val normalizedSlotId = UuidValidator.normalize(slotId)
            ?: return Result.failure(IllegalArgumentException("Некорректный идентификатор слота"))
        if (slotStatus != SlotStatus.AVAILABLE || availablePlaces <= 0) {
            return Result.failure(IllegalStateException("Слот недоступен для бронирования"))
        }
        if (shoes == null || harness == null || !isValid(shoes, availableShoeOptionIds) || !isValid(harness, availableHarnessOptionIds)) {
            return Result.failure(IllegalArgumentException("Выберите снаряжение для скальников и системы"))
        }
        val intent = BookingIntent(normalizedSlotId, shoes, harness)
        return gateway.createBooking(intent)
    }

    private fun isValid(selection: EquipmentSelection, availableOptionIds: Set<String>): Boolean = when (selection) {
        EquipmentSelection.Own -> true
        is EquipmentSelection.Rental -> UuidValidator.isValid(selection.optionId) && selection.optionId in availableOptionIds
    }
}
