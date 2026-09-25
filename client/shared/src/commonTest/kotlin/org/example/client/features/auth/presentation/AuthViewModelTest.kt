package org.example.client.features.auth.presentation

import kotlinx.coroutines.Dispatchers
import org.example.client.core.network.NetworkErrorMessage
import org.example.client.core.storage.InMemoryTokenStorage
import org.example.client.features.auth.data.AuthRepository
import org.example.client.features.auth.data.RequestCodeResponse
import org.example.client.features.auth.domain.ClientSession
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthViewModelTest {
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
