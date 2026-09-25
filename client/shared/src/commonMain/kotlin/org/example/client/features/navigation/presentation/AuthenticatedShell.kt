package org.example.client.features.navigation.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.example.client.features.auth.domain.Client
import org.example.client.features.booking.presentation.BookingScreen
import org.example.client.features.booking.presentation.BookingViewModel
import org.example.client.features.booking.presentation.SlotDetailScreen
import org.example.client.features.my_bookings.presentation.MyBookingDetailsScreen
import org.example.client.features.my_bookings.presentation.MyBookingsScreen
import org.example.client.features.my_bookings.presentation.MyBookingsViewModel
import org.example.client.features.profile.presentation.ProfileScreen
import org.example.client.features.schedule.presentation.ScheduleScreen
import org.example.client.features.schedule.presentation.ScheduleViewModel

@Composable
fun AuthenticatedShell(
    scheduleViewModel: ScheduleViewModel,
    bookingViewModel: BookingViewModel,
    myBookingsViewModel: MyBookingsViewModel,
    client: Client?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabName by rememberSaveable { mutableStateOf(MainTab.SCHEDULE.name) }
    var selectedSlotId by rememberSaveable { mutableStateOf<String?>(null) }
    var bookingOpen by rememberSaveable { mutableStateOf(false) }
    var selectedBookingId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedTab = MainTab.valueOf(selectedTabName)
    val onRootScreen = when (selectedTab) {
        MainTab.SCHEDULE -> selectedSlotId == null
        MainTab.MY_BOOKINGS -> selectedBookingId == null
        MainTab.PROFILE -> true
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (onRootScreen) {
                WaveBottomBar(selectedTab) { tab ->
                    selectedTabName = tab.name
                    selectedSlotId = null
                    bookingOpen = false
                    selectedBookingId = null
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                MainTab.SCHEDULE -> when {
                    selectedSlotId == null -> ScheduleScreen(
                        viewModel = scheduleViewModel,
                        onSlotClick = {
                            selectedSlotId = it
                            bookingOpen = false
                        },
                    )
                    bookingOpen -> BookingScreen(
                        viewModel = bookingViewModel,
                        onBack = { bookingOpen = false },
                        onMyBookings = {
                            selectedTabName = MainTab.MY_BOOKINGS.name
                            selectedBookingId = null
                        },
                    )
                    else -> SlotDetailScreen(
                        viewModel = bookingViewModel,
                        slotId = selectedSlotId!!,
                        onBack = { selectedSlotId = null },
                        onBook = { bookingOpen = true },
                    )
                }
                MainTab.MY_BOOKINGS -> {
                    val myBookingsState by myBookingsViewModel.state.collectAsState()
                    val booking = selectedBookingId?.let { id -> myBookingsState.snapshot?.find(id) }
                    if (booking != null) {
                        MyBookingDetailsScreen(
                            viewModel = myBookingsViewModel,
                            booking = booking,
                            onBack = { selectedBookingId = null },
                        )
                    } else {
                        MyBookingsScreen(
                            viewModel = myBookingsViewModel,
                            onBookingClick = { selectedBookingId = it },
                            onOpenSchedule = { selectedTabName = MainTab.SCHEDULE.name },
                        )
                    }
                }
                MainTab.PROFILE -> ProfileScreen(client = client, onLogout = onLogout)
            }
        }
    }
}
