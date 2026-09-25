package org.example.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.example.client.core.network.NetworkConfig
import org.example.client.core.network.createHttpClient
import org.example.client.core.storage.InMemoryTokenStorage
import org.example.client.core.storage.TokenStorage
import org.example.client.features.auth.data.AuthRepositoryImpl
import org.example.client.features.auth.presentation.AuthScreen
import org.example.client.features.auth.presentation.AuthState
import org.example.client.features.auth.presentation.AuthViewModel
import org.example.client.features.auth.presentation.OtpVerificationScreen
import org.example.client.features.auth.presentation.PhoneEntryScreen
import org.example.client.features.booking.data.BookingRepositoryImpl
import org.example.client.features.booking.data.BookingUseCaseGateway
import org.example.client.features.booking.data.KtorBookingRemoteDataSource
import org.example.client.features.booking.domain.CreateBookingUseCase
import org.example.client.features.booking.domain.GetSlotDetailsUseCase
import org.example.client.features.booking.presentation.BookingScreen
import org.example.client.features.booking.presentation.BookingState
import org.example.client.features.booking.presentation.BookingViewModel
import org.example.client.features.booking.presentation.SlotDetailScreen
import org.example.client.features.schedule.data.InMemoryScheduleCache
import org.example.client.features.schedule.data.KtorScheduleRemoteDataSource
import org.example.client.features.schedule.data.ScheduleRepositoryImpl
import org.example.client.features.schedule.domain.GetScheduleUseCase
import org.example.client.features.schedule.presentation.ScheduleScreen
import org.example.client.features.schedule.presentation.ScheduleState
import org.example.client.features.schedule.presentation.ScheduleViewModel

@Composable
fun App(
    tokenStorage: TokenStorage = InMemoryTokenStorage(),
    config: NetworkConfig = NetworkConfig(),
) {
    val stableConfig = remember { config }
    val client = remember(stableConfig) { createHttpClient(stableConfig) }
    val storage = remember { tokenStorage }
    val authRepository = remember(client, stableConfig.baseUrl, storage) {
        AuthRepositoryImpl(client, stableConfig.baseUrl, storage)
    }
    val authViewModel = remember { AuthViewModel(authRepository, storage) }
    val scheduleRepository = remember(client, stableConfig.baseUrl, storage) {
        ScheduleRepositoryImpl(
            remote = KtorScheduleRemoteDataSource(client, stableConfig.baseUrl),
            tokenStorage = storage,
            cache = InMemoryScheduleCache(),
        )
    }
    val getSchedule = remember(scheduleRepository) { GetScheduleUseCase(scheduleRepository::getSchedule) }
    val scheduleViewModel = remember(scheduleRepository) { ScheduleViewModel(getSchedule, scheduleRepository) }
    val bookingRepository = remember(client, stableConfig.baseUrl, storage) {
        BookingRepositoryImpl(
            remote = KtorBookingRemoteDataSource(client, stableConfig.baseUrl),
            tokenStorage = storage,
        )
    }
    val bookingGateway = remember(bookingRepository) { BookingUseCaseGateway(bookingRepository) }
    val getSlotDetails = remember(bookingGateway) { GetSlotDetailsUseCase(bookingGateway) }
    val createBooking = remember(bookingGateway) { CreateBookingUseCase(bookingGateway) }
    val bookingViewModel = remember(getSlotDetails, createBooking) {
        BookingViewModel(getSlotDetails, createBooking)
    }
    val appScope = rememberCoroutineScope()
    var selectedSlotId by remember { mutableStateOf<String?>(null) }
    var bookingOpen by remember { mutableStateOf(false) }

    val authState by authViewModel.state.collectAsState()
    val scheduleState by scheduleViewModel.state.collectAsState()
    val bookingState by bookingViewModel.state.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val code by authViewModel.code.collectAsState()
    val retryAfter by authViewModel.retryAfter.collectAsState()

    LaunchedEffect(Unit) { authViewModel.restoreSession() }
    DisposableEffect(authViewModel) { onDispose(authViewModel::close) }
    DisposableEffect(scheduleViewModel) { onDispose(scheduleViewModel::close) }
    DisposableEffect(bookingViewModel) { onDispose(bookingViewModel::close) }
    LaunchedEffect(authState) {
        if (authState !is AuthState.Authorized) {
            selectedSlotId = null
            bookingOpen = false
        }
    }
    LaunchedEffect(scheduleState) {
        if (scheduleState is ScheduleState.Forbidden) {
            scheduleViewModel.clearCache()
            authViewModel.logout()
        }
    }
    LaunchedEffect(bookingState) {
        if (bookingState is BookingState.Forbidden) authViewModel.logout()
    }

    MaterialTheme {
        when (val currentState = authState) {
            is AuthState.Authorized -> {
                val slotId = selectedSlotId
                when {
                    slotId == null -> ScheduleScreen(
                        viewModel = scheduleViewModel,
                        onLogout = {
                            appScope.launch {
                                scheduleViewModel.clearCache()
                                authViewModel.logout()
                            }
                        },
                        onSlotClick = {
                            selectedSlotId = it
                            bookingOpen = false
                        },
                    )
                    bookingOpen -> BookingScreen(
                        viewModel = bookingViewModel,
                        onBack = { bookingOpen = false },
                        onSchedule = {
                            bookingOpen = false
                            selectedSlotId = null
                            scheduleViewModel.refresh()
                        },
                    )
                    else -> SlotDetailScreen(
                        viewModel = bookingViewModel,
                        slotId = slotId,
                        onBack = { selectedSlotId = null },
                        onBook = { bookingOpen = true },
                    )
                }
            }
            is AuthState.CodeSent -> OtpVerificationScreen(
                phone = currentState.phone,
                code = code,
                retryAfterSeconds = retryAfter,
                isLoading = false,
                errorMessage = null,
                onCodeChange = authViewModel::updateCode,
                onVerify = authViewModel::verifyCode,
                onResend = authViewModel::resendCode,
            )
            is AuthState.Loading -> if (currentState.screen == AuthScreen.OTP) {
                OtpVerificationScreen(
                    phone = phone,
                    code = code,
                    retryAfterSeconds = retryAfter,
                    isLoading = true,
                    errorMessage = null,
                    onCodeChange = authViewModel::updateCode,
                    onVerify = authViewModel::verifyCode,
                    onResend = authViewModel::resendCode,
                )
            } else {
                PhoneEntryScreen(phone, true, null, authViewModel::updatePhone, authViewModel::requestCode)
            }
            is AuthState.Error -> if (currentState.screen == AuthScreen.OTP) {
                OtpVerificationScreen(
                    phone = phone,
                    code = code,
                    retryAfterSeconds = retryAfter,
                    isLoading = false,
                    errorMessage = currentState.message,
                    onCodeChange = authViewModel::updateCode,
                    onVerify = authViewModel::verifyCode,
                    onResend = authViewModel::resendCode,
                )
            } else {
                PhoneEntryScreen(phone, false, currentState.message, authViewModel::updatePhone, authViewModel::requestCode)
            }
            AuthState.Initial -> PhoneEntryScreen(phone, false, null, authViewModel::updatePhone, authViewModel::requestCode)
        }
    }
}
