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
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import org.example.client.core.network.ErrorResponse
import org.example.client.core.validation.UuidValidator

class KtorBookingRemoteDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : BookingRemoteDataSource {
    override suspend fun getSlotDetails(token: String, slotId: String): Result<TrainingSlotDetails> {
        val normalizedSlotId = UuidValidator.normalize(slotId)
            ?: return Result.failure(IllegalArgumentException("Некорректный идентификатор слота"))
        return execute(HttpStatusCode.OK, {
            client.get(slotUrl(normalizedSlotId)) {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
            }
        }) { it.body() }
    }

    override suspend fun createBooking(token: String, request: CreateBookingRequest): Result<BookingResponse> =
        execute(HttpStatusCode.Created, {
            client.post("${baseUrl.trimEnd('/')}/bookings") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }) { it.body() }

    override suspend fun getMyBookings(token: String): Result<MyBookingsResponse> =
        execute(HttpStatusCode.OK, {
            client.get("${baseUrl.trimEnd('/')}/bookings/my") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
            }
        }) { it.body() }

    override suspend fun cancelBooking(token: String, bookingId: String): Result<BookingResponse> =
        execute(HttpStatusCode.OK, {
            client.post(bookingUrl(bookingId)) {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
            }
        }) { it.body() }

    private fun slotUrl(slotId: String): String = URLBuilder("${baseUrl.trimEnd('/')}/slots")
        .appendPathSegments(listOf(slotId), encodeSlash = true)
        .buildString()

    private fun bookingUrl(bookingId: String): String = URLBuilder("${baseUrl.trimEnd('/')}/bookings")
        .appendPathSegments(listOf(bookingId, "cancel"), encodeSlash = true)
        .buildString()

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
