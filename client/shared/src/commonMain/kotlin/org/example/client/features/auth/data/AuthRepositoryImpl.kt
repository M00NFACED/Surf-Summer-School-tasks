package org.example.client.features.auth.data

import kotlinx.coroutines.CancellationException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.HttpStatusCode
import org.example.client.core.network.ErrorResponse
import org.example.client.core.storage.TokenStorage
import org.example.client.features.auth.domain.Client
import org.example.client.features.auth.domain.ClientSession

class AuthRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String,
    private val tokenStorage: TokenStorage,
) : AuthRepository {
    override suspend fun requestCode(phone: String): Result<RequestCodeResponse> {
        return try {
            val response = client.post(endpoint("/auth/request-code")) {
                contentType(ContentType.Application.Json)
                setBody(RequestCodeRequest(phone))
            }
            if (response.status == HttpStatusCode.Accepted) {
                try {
                    Result.success(response.body<RequestCodeResponse>())
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    Result.failure(error)
                }
            } else {
                Result.failure(response.toAuthException())
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    override suspend fun verifyCode(phone: String, code: String): Result<ClientSession> {
        return try {
            val response = client.post(endpoint("/auth/verify-code")) {
                contentType(ContentType.Application.Json)
                setBody(VerifyCodeRequest(phone, code))
            }
            if (response.status != HttpStatusCode.OK) {
                Result.failure(response.toAuthException())
            } else {
                try {
                    val token = response.body<TokenResponse>()
                    tokenStorage.write(token.accessToken)
                    Result.success(ClientSession(token.accessToken, Client(token.client.id, token.client.phone)))
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    Result.failure(error)
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    private fun endpoint(path: String): String = "${baseUrl.trimEnd('/')}$path"

    private suspend fun io.ktor.client.statement.HttpResponse.toAuthException(): AuthApiException {
        val error = runCatching { body<ErrorResponse>() }.getOrNull()
        return AuthApiException(
            statusCode = status.value,
            errorCode = error?.code ?: "HTTP_${status.value}",
            message = error?.message ?: "Не удалось выполнить авторизацию",
        )
    }
}
