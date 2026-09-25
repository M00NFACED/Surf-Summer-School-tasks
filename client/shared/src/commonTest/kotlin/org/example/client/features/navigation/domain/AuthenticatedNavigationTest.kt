package org.example.client.features.navigation.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthenticatedNavigationTest {
    @Test
    fun scheduleRootDoesNotConsumeSystemBack() {
        val navigation = AuthenticatedNavigation()

        assertTrue(navigation.isScheduleRoot)
        assertFalse(navigation.consumesSystemBack)
    }

    @Test
    fun systemBackFromSlotDetailsReturnsToSchedule() {
        val navigation = AuthenticatedNavigation().openSlot("slot-1")

        val result = navigation.onSystemBack()

        assertEquals(MainTab.SCHEDULE, result.tab)
        assertEquals(null, result.slotId)
        assertTrue(result.isScheduleRoot)
    }

    @Test
    fun systemBackFromBookingFormReturnsToSchedule() {
        val navigation = AuthenticatedNavigation().openSlot("slot-1").openBooking()

        val result = navigation.onSystemBack()

        assertEquals(MainTab.SCHEDULE, result.tab)
        assertEquals(null, result.slotId)
        assertFalse(result.bookingOpen)
        assertTrue(result.isScheduleRoot)
    }

    @Test
    fun systemBackFromMyBookingsReturnsToList() {
        val navigation = AuthenticatedNavigation().selectTab(MainTab.MY_BOOKINGS).openMyBooking("booking-1")

        val result = navigation.onSystemBack()

        assertEquals(MainTab.MY_BOOKINGS, result.tab)
        assertEquals(null, result.bookingId)
        assertTrue(result.isRootScreen)
    }

    @Test
    fun systemBackFromTabsSwitchesToSchedule() {
        val fromMyBookings = AuthenticatedNavigation().selectTab(MainTab.MY_BOOKINGS)
        val fromProfile = AuthenticatedNavigation().selectTab(MainTab.PROFILE)

        assertEquals(MainTab.SCHEDULE, fromMyBookings.onSystemBack().tab)
        assertEquals(MainTab.SCHEDULE, fromProfile.onSystemBack().tab)
    }

    @Test
    fun selectingTabResetsDeepScreens() {
        val navigation = AuthenticatedNavigation()
            .openSlot("slot-1")
            .openBooking()
            .openMyBooking("booking-1")

        val result = navigation.selectTab(MainTab.PROFILE)

        assertEquals(MainTab.PROFILE, result.tab)
        assertEquals(null, result.slotId)
        assertEquals(null, result.bookingId)
        assertFalse(result.bookingOpen)
    }

    @Test
    fun bookingDetailsHideBottomBar() {
        val list = AuthenticatedNavigation().selectTab(MainTab.MY_BOOKINGS)
        val details = list.openMyBooking("booking-1")

        assertTrue(list.isRootScreen)
        assertFalse(details.isRootScreen)
        assertTrue(details.consumesSystemBack)
    }

    @Test
    fun reviewSheetHidesBottomBarAndClosesOnSystemBack() {
        val list = AuthenticatedNavigation().selectTab(MainTab.MY_BOOKINGS)
        val withSheet = list.openReview("booking-1")

        assertFalse(withSheet.isRootScreen)
        val afterBack = withSheet.onSystemBack()

        assertEquals(MainTab.MY_BOOKINGS, afterBack.tab)
        assertEquals(null, afterBack.reviewBookingId)
        assertTrue(afterBack.isRootScreen)
    }

    @Test
    fun systemBackFromReviewSheetDoesNotCloseBookingDetails() {
        val details = AuthenticatedNavigation().selectTab(MainTab.MY_BOOKINGS).openMyBooking("booking-1")
        val withSheet = details.openReview("booking-1")

        val afterBack = withSheet.onSystemBack()

        assertEquals("booking-1", afterBack.bookingId)
        assertEquals(null, afterBack.reviewBookingId)
    }

    @Test
    fun selectingTabClosesReviewSheet() {
        val withSheet = AuthenticatedNavigation()
            .selectTab(MainTab.MY_BOOKINGS)
            .openReview("booking-1")

        val result = withSheet.selectTab(MainTab.PROFILE)

        assertEquals(null, result.reviewBookingId)
    }
}
