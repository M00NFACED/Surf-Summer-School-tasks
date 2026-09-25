package org.example.client.features.booking.domain

open class BookingError(
    val statusCode: Int,
    val errorCode: String,
    override val message: String,
) : Exception(message)

class SlotFullException(
    errorCode: String = "SLOT_FULL",
    message: String = "Место только что занято другим клиентом",
) : BookingError(409, errorCode, message)

class DuplicateBookingException(
    errorCode: String = "BOOKING_EXISTS",
    message: String = "Вы уже записаны на эту тренировку",
) : BookingError(409, errorCode, message)
