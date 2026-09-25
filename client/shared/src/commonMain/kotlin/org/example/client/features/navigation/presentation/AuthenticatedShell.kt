package org.example.client.features.navigation.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.example.client.core.navigation.PlatformBackHandler
import org.example.client.features.auth.domain.Client
import org.example.client.features.booking.presentation.BookingScreen
import org.example.client.features.booking.presentation.BookingViewModel
import org.example.client.features.booking.presentation.SlotDetailScreen
import org.example.client.features.my_bookings.presentation.MyBookingDetailsScreen
import org.example.client.features.my_bookings.presentation.MyBookingsScreen
import org.example.client.features.my_bookings.presentation.MyBookingsViewModel
import org.example.client.features.navigation.domain.AuthenticatedNavigation
import org.example.client.features.navigation.domain.MainTab
import org.example.client.features.profile.presentation.ProfileScreen
import org.example.client.features.review.presentation.ReviewSheet
import org.example.client.features.review.presentation.ReviewViewModel
import org.example.client.features.schedule.presentation.ScheduleScreen
import org.example.client.features.schedule.presentation.ScheduleViewModel

@Composable
fun AuthenticatedShell(
    scheduleViewModel: ScheduleViewModel,
    bookingViewModel: BookingViewModel,
    myBookingsViewModel: MyBookingsViewModel,
    reviewViewModel: ReviewViewModel,
    client: Client?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var reviewFeedback by remember { mutableStateOf<String?>(null) }
    var tabName by rememberSaveable { mutableStateOf(MainTab.SCHEDULE.name) }
    var slotId by rememberSaveable { mutableStateOf<String?>(null) }
    var bookingOpen by rememberSaveable { mutableStateOf(false) }
    var bookingId by rememberSaveable { mutableStateOf<String?>(null) }
    var reviewBookingId by rememberSaveable { mutableStateOf<String?>(null) }
    val navigation = AuthenticatedNavigation(
        tab = MainTab.valueOf(tabName),
        slotId = slotId,
        bookingOpen = bookingOpen,
        bookingId = bookingId,
        reviewBookingId = reviewBookingId,
    )

    fun apply(next: AuthenticatedNavigation) {
        tabName = next.tab.name
        slotId = next.slotId
        bookingOpen = next.bookingOpen
        bookingId = next.bookingId
        reviewBookingId = next.reviewBookingId
    }

    PlatformBackHandler(
        enabled = navigation.consumesSystemBack,
        onBack = { apply(navigation.onSystemBack()) },
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (navigation.isRootScreen) {
                WaveBottomBar(navigation.tab) { tab -> apply(navigation.selectTab(tab)) }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (navigation.tab) {
                MainTab.SCHEDULE -> when {
                    navigation.slotId == null -> ScheduleScreen(
                        viewModel = scheduleViewModel,
                        onSlotClick = { apply(navigation.openSlot(it)) },
                    )
                    navigation.bookingOpen -> BookingScreen(
                        viewModel = bookingViewModel,
                        onBack = { apply(navigation.closeBooking()) },
                        onMyBookings = { apply(navigation.selectTab(MainTab.MY_BOOKINGS)) },
                    )
                    else -> SlotDetailScreen(
                        viewModel = bookingViewModel,
                        slotId = navigation.slotId!!,
                        onBack = { apply(navigation.onSystemBack()) },
                        onBook = { apply(navigation.openBooking()) },
                    )
                }
                MainTab.MY_BOOKINGS -> {
                    val state by myBookingsViewModel.state.collectAsState()
                    val booking = navigation.bookingId?.let { id -> state.snapshot?.find(id) }
                    val reviewBooking = navigation.reviewBookingId?.let { id -> state.snapshot?.find(id) }
                    if (booking != null) {
                        MyBookingDetailsScreen(
                            viewModel = myBookingsViewModel,
                            booking = booking,
                            onBack = { apply(navigation.onSystemBack()) },
                            onReviewClick = { apply(navigation.openReview(booking.id)) },
                        )
                    } else {
                        MyBookingsScreen(
                            viewModel = myBookingsViewModel,
                            onBookingClick = { apply(navigation.openMyBooking(it)) },
                            onOpenSchedule = { apply(navigation.selectTab(MainTab.SCHEDULE)) },
                            onReviewClick = { apply(navigation.openReview(it)) },
                            feedback = reviewFeedback,
                        )
                    }
                    reviewBooking?.let { target ->
                        val reviewState by reviewViewModel.state.collectAsState()
                        ReviewSheet(
                            booking = target,
                            state = reviewState,
                            onScoreSelected = reviewViewModel::selectScore,
                            onSubmit = {
                                reviewViewModel.submit(target)
                                myBookingsViewModel.applyReview(target.id, reviewState.score)
                            },
                            onDismiss = { apply(navigation.closeReview()) },
                        )
                        if (reviewState.isCompleted) {
                            LaunchedEffect(reviewState.isCompleted) {
                                reviewFeedback = "Спасибо за отзыв!"
                                apply(navigation.closeReview())
                            }
                        }
                    }
                }
                MainTab.PROFILE -> ProfileScreen(client = client, onLogout = onLogout)
            }
        }
    }
}
