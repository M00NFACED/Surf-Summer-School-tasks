package org.example.client.features.booking.domain

enum class BookingStatus(val apiValue: String) {
    CONFIRMED("confirmed"),
    CANCELLED_BY_CLIENT("cancelled_by_client"),
    CANCELLED_BY_VENUE("cancelled_by_venue"),
    COMPLETED("completed"),
    RATED("rated"),
    ;

    companion object {
        fun fromApi(value: String): BookingStatus? = values().firstOrNull { it.apiValue == value }
    }
}
