package org.example.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import org.example.client.core.network.NetworkConfig
import org.example.client.core.network.createHttpClient
import org.example.client.core.storage.InMemoryTokenStorage
import org.example.client.features.auth.data.AuthRepositoryImpl
import org.example.client.features.auth.presentation.AuthScreen
import org.example.client.features.auth.presentation.AuthState
import org.example.client.features.auth.presentation.AuthViewModel
import org.example.client.features.auth.presentation.OtpEntryScreen
import org.example.client.features.auth.presentation.PhoneEntryScreen
import org.example.client.features.schedule.presentation.ScheduleShellScreen

@Composable
fun App() {
    val client = remember { createHttpClient(NetworkConfig()) }
    val storage = remember { InMemoryTokenStorage() }
    val repository = remember { AuthRepositoryImpl(client, NetworkConfig().baseUrl, storage) }
    val viewModel = remember { AuthViewModel(repository, storage) }
    val state by viewModel.state.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val code by viewModel.code.collectAsState()
    val retryAfter by viewModel.retryAfter.collectAsState()

    LaunchedEffect(Unit) { viewModel.restoreSession() }
    DisposableEffect(viewModel) { onDispose(viewModel::close) }

    MaterialTheme {
        when (val currentState = state) {
            is AuthState.Authorized -> ScheduleShellScreen()
            is AuthState.CodeSent -> OtpEntryScreen(
                phone = phone,
                code = code,
                retryAfterSeconds = retryAfter,
                isLoading = false,
                errorMessage = null,
                onCodeChange = viewModel::updateCode,
                onVerify = viewModel::verifyCode,
                onResend = viewModel::resendCode,
            )
            is AuthState.Loading -> if (currentState.screen == AuthScreen.OTP) {
                OtpEntryScreen(phone, code, retryAfter, true, null, viewModel::updateCode, viewModel::verifyCode, viewModel::resendCode)
            } else {
                PhoneEntryScreen(phone, true, null, viewModel::updatePhone, viewModel::requestCode)
            }
            is AuthState.Error -> if (currentState.screen == AuthScreen.OTP) {
                OtpEntryScreen(phone, code, retryAfter, false, currentState.message, viewModel::updateCode, viewModel::verifyCode, viewModel::resendCode)
            } else {
                PhoneEntryScreen(phone, false, currentState.message, viewModel::updatePhone, viewModel::requestCode)
            }
            AuthState.Initial -> PhoneEntryScreen(phone, false, null, viewModel::updatePhone, viewModel::requestCode)
        }
    }
}
