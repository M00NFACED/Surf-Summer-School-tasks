package org.example.client.features.auth.data

import org.example.client.features.auth.data.RequestCodeResponse
import org.example.client.features.auth.domain.ClientSession

interface AuthRepository {
    suspend fun requestCode(phone: String): Result<RequestCodeResponse>
    suspend fun verifyCode(phone: String, code: String): Result<ClientSession>
}
