package org.example.client.features.my_bookings.presentation

import org.example.client.features.my_bookings.domain.MyBookingsSnapshot

data class MyBookingsState(
    val isLoading: Boolean = false,
    val snapshot: MyBookingsSnapshot? = null,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val isForbidden: Boolean = false,
    val isCancelling: Boolean = false,
    val cancellationError: String? = null,
) {
    val isEmpty: Boolean
        get() = snapshot != null && snapshot.active.isEmpty() && snapshot.history.isEmpty()
}
