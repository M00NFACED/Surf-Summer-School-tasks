package org.example.client.features.auth.presentation

import kotlinx.coroutines.Dispatchers
import org.example.client.core.network.NetworkErrorMessage
import org.example.client.core.storage.InMemoryTokenStorage
import org.example.client.features.auth.data.AuthRepository
import org.example.client.features.auth.data.RequestCodeResponse
import org.example.client.features.auth.domain.ClientSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthViewModelTest {
    @Test
    fun keepsPhoneInCodeSentState() {
        val viewModel = AuthViewModel(
            repository = SuccessfulAuthRepository(),
            tokenStorage = InMemoryTokenStorage(),
            dispatcher = Dispatchers.Unconfined,
        )
        viewModel.updatePhone("9991234567")

        viewModel.requestCode()

        val state = viewModel.state.value
        assertTrue(state is AuthState.CodeSent)
        assertEquals("+79991234567", (state as AuthState.CodeSent).phone)
        viewModel.close()
    }

    @Test
    fun mapsThrownNetworkFailureToSafeErrorState() {
        val viewModel = AuthViewModel(
            repository = ThrowingAuthRepository(),
            tokenStorage = InMemoryTokenStorage(),
            dispatcher = Dispatchers.Unconfined,
        )
        viewModel.updatePhone("9991234567")

        viewModel.requestCode()

        assertEquals(AuthState.Error(NetworkErrorMessage, AuthScreen.PHONE), viewModel.state.value)
        viewModel.close()
    }
}

private class ThrowingAuthRepository : AuthRepository {
    override suspend fun requestCode(phone: String): Result<RequestCodeResponse> = error("offline")

    override suspend fun verifyCode(phone: String, code: String): Result<ClientSession> = error("offline")
}

private class SuccessfulAuthRepository : AuthRepository {
    override suspend fun requestCode(phone: String): Result<RequestCodeResponse> = Result.success(
        RequestCodeResponse(
            status = "sent",
            message = "ok",
            expiresIn = 600,
            retryAfter = 0,
        ),
    )

    override suspend fun verifyCode(phone: String, code: String): Result<ClientSession> = error("not used")
}
