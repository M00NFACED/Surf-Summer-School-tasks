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
import org.example.client.features.auth.presentation.OtpEntryScreen
import org.example.client.features.auth.presentation.PhoneEntryScreen
import org.example.client.features.schedule.data.InMemoryScheduleCache
import org.example.client.features.schedule.data.KtorScheduleRemoteDataSource
import org.example.client.features.schedule.data.ScheduleRepositoryImpl
import org.example.client.features.schedule.domain.GetScheduleUseCase
import org.example.client.features.schedule.presentation.ScheduleScreen
import org.example.client.features.schedule.presentation.ScheduleSlotPlaceholderScreen
import org.example.client.features.schedule.presentation.ScheduleState
import org.example.client.features.schedule.presentation.ScheduleViewModel

@Composable
fun App(tokenStorage: TokenStorage = InMemoryTokenStorage()) {
    val config = remember { NetworkConfig() }
    val client = remember { createHttpClient(config) }
    val storage = remember(tokenStorage) { tokenStorage }
    val authRepository = remember { AuthRepositoryImpl(client, config.baseUrl, storage) }
    val authViewModel = remember { AuthViewModel(authRepository, storage) }
    val scheduleRepository = remember {
        ScheduleRepositoryImpl(
            remote = KtorScheduleRemoteDataSource(client, config.baseUrl),
            tokenStorage = storage,
            cache = InMemoryScheduleCache(),
        )
    }
    val getSchedule = remember(scheduleRepository) { GetScheduleUseCase(scheduleRepository::getSchedule) }
    val scheduleViewModel = remember(scheduleRepository) { ScheduleViewModel(getSchedule, scheduleRepository) }
    val appScope = rememberCoroutineScope()
    var selectedSlotId by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.state.collectAsState()
    val scheduleState by scheduleViewModel.state.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val code by authViewModel.code.collectAsState()
    val retryAfter by authViewModel.retryAfter.collectAsState()

    LaunchedEffect(Unit) { authViewModel.restoreSession() }
    DisposableEffect(authViewModel) { onDispose(authViewModel::close) }
    DisposableEffect(scheduleViewModel) { onDispose(scheduleViewModel::close) }
    LaunchedEffect(authState) {
        if (authState !is AuthState.Authorized) selectedSlotId = null
    }
    LaunchedEffect(scheduleState) {
        if (scheduleState is ScheduleState.Forbidden) {
            scheduleViewModel.clearCache()
            authViewModel.logout()
        }
    }

    MaterialTheme {
        when (val currentState = authState) {
            is AuthState.Authorized -> {
                val slotId = selectedSlotId
                if (slotId == null) {
                    ScheduleScreen(
                        viewModel = scheduleViewModel,
                        onLogout = {
                            appScope.launch {
                                scheduleViewModel.clearCache()
                                authViewModel.logout()
                            }
                        },
                        onSlotClick = { selectedSlotId = it },
                    )
                } else {
                    ScheduleSlotPlaceholderScreen(slotId = slotId, onBack = { selectedSlotId = null })
                }
            }
            is AuthState.CodeSent -> OtpEntryScreen(
                phone = phone,
                code = code,
                retryAfterSeconds = retryAfter,
                isLoading = false,
                errorMessage = null,
                onCodeChange = authViewModel::updateCode,
                onVerify = authViewModel::verifyCode,
                onResend = authViewModel::resendCode,
            )
            is AuthState.Loading -> if (currentState.screen == AuthScreen.OTP) {
                OtpEntryScreen(phone, code, retryAfter, true, null, authViewModel::updateCode, authViewModel::verifyCode, authViewModel::resendCode)
            } else {
                PhoneEntryScreen(phone, true, null, authViewModel::updatePhone, authViewModel::requestCode)
            }
            is AuthState.Error -> if (currentState.screen == AuthScreen.OTP) {
                OtpEntryScreen(phone, code, retryAfter, false, currentState.message, authViewModel::updateCode, authViewModel::verifyCode, authViewModel::resendCode)
            } else {
                PhoneEntryScreen(phone, false, currentState.message, authViewModel::updatePhone, authViewModel::requestCode)
            }
            AuthState.Initial -> PhoneEntryScreen(phone, false, null, authViewModel::updatePhone, authViewModel::requestCode)
        }
    }
}
