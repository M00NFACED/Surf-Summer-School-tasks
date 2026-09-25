package org.example.client.features.booking.domain

import org.example.client.core.validation.UuidValidator

class GetSlotDetailsUseCase(
    private val gateway: BookingGateway,
) {
    suspend operator fun invoke(slotId: String): Result<SlotDetailsItem> {
        val normalizedSlotId = UuidValidator.normalize(slotId)
            ?: return Result.failure(IllegalArgumentException("Некорректный идентификатор слота"))
        return gateway.getSlotDetails(normalizedSlotId)
    }
}
