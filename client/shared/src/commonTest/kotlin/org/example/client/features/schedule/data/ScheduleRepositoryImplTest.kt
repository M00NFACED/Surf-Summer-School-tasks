package org.example.client.features.schedule.data

import kotlinx.coroutines.runBlocking
import org.example.client.core.storage.InMemoryTokenStorage
import org.example.client.features.schedule.domain.ScheduleFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduleRepositoryImplTest {
    @Test
    fun writesSuccessfulSnapshotAndReturnsItOfflineOnNetworkFailure() = runBlocking {
        val storage = InMemoryTokenStorage().apply { write("token") }
        val remote = FakeRemote(Result.success(testSlotResponse()))
        val repository = ScheduleRepositoryImpl(remote, storage, InMemoryScheduleCache(), now = { 1234L })

        val first = repository.getSchedule(ScheduleFilter())
        remote.result = Result.failure(IllegalStateException("offline"))
        val second = repository.getSchedule(ScheduleFilter())

        assertEquals(false, first.getOrThrow().isOffline)
        assertEquals(1234L, first.getOrThrow().cachedAtEpochMillis)
        assertTrue(second.getOrThrow().isOffline)
        assertEquals(first.getOrThrow().items, second.getOrThrow().items)
    }

    @Test
    fun doesNotUseCacheForUnauthorizedResponse() = runBlocking {
        val storage = InMemoryTokenStorage().apply { write("token") }
        val remote = FakeRemote(Result.success(testSlotResponse()))
        val repository = ScheduleRepositoryImpl(remote, storage, InMemoryScheduleCache(), now = { 1234L })
        repository.getSchedule(ScheduleFilter())

        remote.result = Result.failure(ScheduleApiException(401, "UNAUTHORIZED", "expired"))
        val result = repository.getSchedule(ScheduleFilter())

        assertTrue(result.isFailure)
    }
}

private class FakeRemote(var result: Result<SlotListResponse>) : ScheduleRemoteDataSource {
    override suspend fun load(token: String, filter: ScheduleFilter): Result<SlotListResponse> = result
}
