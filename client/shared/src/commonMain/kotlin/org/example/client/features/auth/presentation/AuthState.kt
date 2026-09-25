package org.example.client.features.auth.presentation

enum class AuthScreen {
    PHONE,
    OTP,
}

sealed interface AuthState {
    data object Initial : AuthState
    data class Loading(val screen: AuthScreen) : AuthState
    data class CodeSent(
        val phone: String,
        val retryAfterSeconds: Int = 60,
    ) : AuthState
    data class Authorized(val session: org.example.client.features.auth.domain.ClientSession) : AuthState
    data class Error(
        val message: String,
        val screen: AuthScreen,
    ) : AuthState
}
