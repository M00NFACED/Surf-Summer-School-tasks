package org.example.client.features.booking.data

import org.example.client.features.booking.domain.BookingError

class BookingApiException(
    statusCode: Int,
    errorCode: String,
    message: String,
) : BookingError(statusCode, errorCode, message)
