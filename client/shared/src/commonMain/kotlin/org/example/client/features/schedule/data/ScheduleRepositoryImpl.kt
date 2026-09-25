package org.example.client.features.schedule.data

import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Clock
import org.example.client.core.storage.TokenStorage
import org.example.client.features.schedule.domain.ScheduleFilter
import org.example.client.features.schedule.domain.ScheduleSnapshot

class ScheduleRepositoryImpl(
    private val remote: ScheduleRemoteDataSource,
    private val tokenStorage: TokenStorage,
    private val cache: ScheduleCache,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ScheduleRepository {
    override suspend fun getSchedule(filter: ScheduleFilter): Result<ScheduleSnapshot> {
        val token = tokenStorage.read()
        if (token.isNullOrBlank()) {
            return Result.failure(ScheduleApiException(401, "UNAUTHORIZED", "Сессия истекла"))
        }
        val remoteResult = try {
            remote.load(token, filter)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            return cachedOrFailure(error)
        }
        val error = remoteResult.exceptionOrNull()
        if (error != null) {
            return cachedOrFailure(error)
        }
        return try {
            val snapshot = ScheduleMapper.toDomain(remoteResult.getOrThrow()).copy(cachedAtEpochMillis = now())
            cache.write(snapshot)
            Result.success(snapshot)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            cachedOrFailure(error)
        }
    }

    override suspend fun clearCache() = cache.clear()

    private suspend fun cachedOrFailure(error: Throwable): Result<ScheduleSnapshot> {
        if (error is ScheduleApiException && error.statusCode != 503) {
            return Result.failure(error)
        }
        val cached = cache.read()
        return if (cached == null) Result.failure(error) else Result.success(cached.copy(isOffline = true))
    }
}
