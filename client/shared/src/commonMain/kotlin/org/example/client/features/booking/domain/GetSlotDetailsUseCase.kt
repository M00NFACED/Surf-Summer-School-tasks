package org.example.client.features.booking.domain

class GetSlotDetailsUseCase(
    private val gateway: BookingGateway,
) {
    suspend operator fun invoke(slotId: String): Result<SlotDetailsItem> {
        if (!uuidPattern.matches(slotId)) {
            return Result.failure(IllegalArgumentException("Некорректный идентификатор слота"))
        }
        return gateway.getSlotDetails(slotId)
    }

    private companion object {
        val uuidPattern = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")
    }
}
