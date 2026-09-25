package org.example.client.features.navigation.domain

enum class MainTab { SCHEDULE, MY_BOOKINGS, PROFILE }

data class AuthenticatedNavigation(
    val tab: MainTab = MainTab.SCHEDULE,
    val slotId: String? = null,
    val bookingOpen: Boolean = false,
    val bookingId: String? = null,
) {
    val isScheduleRoot: Boolean
        get() = tab == MainTab.SCHEDULE && slotId == null && !bookingOpen

    val isRootScreen: Boolean
        get() = when (tab) {
            MainTab.SCHEDULE -> slotId == null
            MainTab.MY_BOOKINGS -> bookingId == null
            MainTab.PROFILE -> true
        }

    val consumesSystemBack: Boolean
        get() = !isScheduleRoot

    fun onSystemBack(): AuthenticatedNavigation = when {
        tab == MainTab.SCHEDULE && bookingOpen -> copy(bookingOpen = false, slotId = null)
        tab == MainTab.SCHEDULE -> copy(slotId = null, bookingOpen = false)
        tab == MainTab.MY_BOOKINGS && bookingId != null -> copy(bookingId = null)
        else -> copy(tab = MainTab.SCHEDULE, slotId = null, bookingOpen = false, bookingId = null)
    }

    fun selectTab(next: MainTab): AuthenticatedNavigation =
        copy(tab = next, slotId = null, bookingOpen = false, bookingId = null)

    fun openSlot(id: String): AuthenticatedNavigation = copy(slotId = id, bookingOpen = false)

    fun openBooking(): AuthenticatedNavigation = copy(bookingOpen = true)

    fun closeBooking(): AuthenticatedNavigation = copy(bookingOpen = false)

    fun openMyBooking(id: String): AuthenticatedNavigation = copy(bookingId = id)
}
