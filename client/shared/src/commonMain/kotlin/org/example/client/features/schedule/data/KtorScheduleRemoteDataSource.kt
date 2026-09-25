package org.example.client.features.schedule.data

import kotlinx.coroutines.CancellationException
import org.example.client.core.network.ErrorResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import org.example.client.features.schedule.domain.ScheduleFilter

class KtorScheduleRemoteDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : ScheduleRemoteDataSource {
    override suspend fun load(token: String, filter: ScheduleFilter): Result<SlotListResponse> {
        return try {
            val response = client.get("${baseUrl.trimEnd('/')}/slots") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                url { ScheduleQueryBuilder.build(filter).forEach { (key, value) -> parameters.append(key, value) } }
            }
            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body<SlotListResponse>())
            } else {
                Result.failure(response.toScheduleException())
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    private suspend fun HttpResponse.toScheduleException(): ScheduleApiException {
        val error = runCatching { body<ErrorResponse>() }.getOrNull()
        return ScheduleApiException(
            statusCode = status.value,
            errorCode = error?.code ?: "HTTP_${status.value}",
            message = error?.message ?: "Не удалось загрузить расписание",
        )
    }
}
