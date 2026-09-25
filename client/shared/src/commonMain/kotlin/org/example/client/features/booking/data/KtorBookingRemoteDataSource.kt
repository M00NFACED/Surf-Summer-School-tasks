package org.example.client.features.booking.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import org.example.client.core.network.ErrorResponse

class KtorBookingRemoteDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : BookingRemoteDataSource {
    override suspend fun getSlotDetails(token: String, slotId: String): Result<TrainingSlotDetails> =
        execute(HttpStatusCode.OK, {
            client.get("${baseUrl.trimEnd('/')}/slots/$slotId") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
            }
        }) { it.body() }

    override suspend fun createBooking(token: String, request: CreateBookingRequest): Result<BookingResponse> =
        execute(HttpStatusCode.Created, {
            client.post("${baseUrl.trimEnd('/')}/bookings") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }) { it.body() }

    private suspend fun <T> execute(
        expectedStatus: HttpStatusCode,
        request: suspend () -> HttpResponse,
        parse: suspend (HttpResponse) -> T,
    ): Result<T> {
        return try {
            val response = request()
            if (response.status == expectedStatus) {
                Result.success(parse(response))
            } else {
                Result.failure(response.toBookingException())
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    private suspend fun HttpResponse.toBookingException(): BookingApiException {
        val error = runCatching { body<ErrorResponse>() }.getOrNull()
        return BookingApiException(
            statusCode = status.value,
            errorCode = error?.code ?: "HTTP_${status.value}",
            message = error?.message ?: "Не удалось выполнить операцию",
        )
    }
}
