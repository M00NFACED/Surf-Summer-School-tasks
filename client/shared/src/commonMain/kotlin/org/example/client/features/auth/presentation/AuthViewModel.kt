package org.example.client.features.auth.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.client.core.storage.TokenStorage
import org.example.client.features.auth.data.AuthApiException
import org.example.client.features.auth.data.AuthRepository
import org.example.client.features.auth.domain.PhoneNumberValidator
import org.example.client.features.auth.domain.RequestCodeUseCase
import org.example.client.features.auth.domain.VerifyCodeUseCase

class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenStorage: TokenStorage,
    private val validator: PhoneNumberValidator = PhoneNumberValidator(),
    private val requestCodeUseCase: RequestCodeUseCase = RequestCodeUseCase(),
    private val verifyCodeUseCase: VerifyCodeUseCase = VerifyCodeUseCase(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mutableState = MutableStateFlow<AuthState>(AuthState.Initial)
    private val mutablePhone = MutableStateFlow("")
    private val mutableCode = MutableStateFlow("")
    private val mutableRetryAfter = MutableStateFlow(0)
    private var countdownJob: Job? = null

    val state: StateFlow<AuthState> = mutableState.asStateFlow()
    val phone: StateFlow<String> = mutablePhone.asStateFlow()
    val code: StateFlow<String> = mutableCode.asStateFlow()
    val retryAfter: StateFlow<Int> = mutableRetryAfter.asStateFlow()

    fun updatePhone(value: String) {
        mutablePhone.value = validator.normalize(value)
        if (mutableState.value is AuthState.Error) mutableState.value = AuthState.Initial
    }

    fun updateCode(value: String) {
        mutableCode.value = value.filter(Char::isDigit).take(6)
        val current = mutableState.value
        if (current is AuthState.Error && current.screen == AuthScreen.OTP) {
            mutableState.value = AuthState.CodeSent(mutablePhone.value, mutableRetryAfter.value)
        }
    }

    fun requestCode() {
        sendCode(AuthScreen.PHONE)
    }

    fun resendCode() {
        sendCode(AuthScreen.OTP)
    }

    fun verifyCode() {
        val validation = verifyCodeUseCase(mutablePhone.value, mutableCode.value)
        if (validation.isFailure) {
            val message = validation.exceptionOrNull()?.messageOrDefault() ?: "Проверьте номер и код"
            val screen = if (validator.isValid(mutablePhone.value)) AuthScreen.OTP else AuthScreen.PHONE
            mutableState.value = AuthState.Error(message, screen)
            return
        }
        val (phone, code) = validation.getOrThrow()
        scope.launch {
            mutableState.value = AuthState.Loading(AuthScreen.OTP)
            repository.verifyCode(phone, code)
                .onSuccess { session ->
                    countdownJob?.cancel()
                    mutableRetryAfter.value = 0
                    mutableState.value = AuthState.Authorized(session)
                }
                .onFailure { error -> mutableState.value = AuthState.Error(error.messageOrDefault(), AuthScreen.OTP) }
        }
    }

    fun restoreSession() {
        scope.launch {
            val token = tokenStorage.read()
            if (!token.isNullOrBlank()) {
                mutableState.value = AuthState.Authorized(org.example.client.features.auth.domain.ClientSession(token))
            }
        }
    }

    fun logout() {
        scope.launch {
            tokenStorage.clear()
            mutablePhone.value = ""
            mutableCode.value = ""
            mutableRetryAfter.value = 0
            mutableState.value = AuthState.Initial
        }
    }

    fun close() {
        countdownJob?.cancel()
        scope.cancel()
    }

    private fun sendCode(screen: AuthScreen) {
        val validation = requestCodeUseCase(mutablePhone.value)
        if (validation.isFailure) {
            mutableState.value = AuthState.Error(
                validation.exceptionOrNull()?.messageOrDefault() ?: "Введите номер в формате +7XXXXXXXXXX",
                AuthScreen.PHONE,
            )
            return
        }
        val phone = validation.getOrThrow()
        scope.launch {
            mutableState.value = AuthState.Loading(screen)
            repository.requestCode(phone)
                .onSuccess { response ->
                    val retryAfter = response.retryAfter ?: 60
                    mutableRetryAfter.value = retryAfter
                    mutableState.value = AuthState.CodeSent(phone, retryAfter)
                    startCountdown(retryAfter)
                }
                .onFailure { error -> mutableState.value = AuthState.Error(error.messageOrDefault(), screen) }
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1_000)
                remaining -= 1
                mutableRetryAfter.value = remaining
                mutableState.value = AuthState.CodeSent(mutablePhone.value, remaining)
            }
        }
    }

    private fun Throwable.messageOrDefault(): String = (this as? AuthApiException)?.message ?: "Не удалось выполнить действие. Попробуйте ещё раз"
}
