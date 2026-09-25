package org.example.client.features.schedule.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant

class GetScheduleUseCaseTest {
    @Test
    fun appliesSevenDayDefaultPeriod() = runBlocking {
        val now = Instant.parse("2026-09-25T12:00:00Z")
        var captured: ScheduleFilter? = null
        val useCase = GetScheduleUseCase(
            load = { filter ->
                captured = filter
                Result.success(ScheduleSnapshot(now, now + 7.days, emptyList()))
            },
            now = { now },
        )

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(now, captured?.from)
        assertEquals(now + 7.days, captured?.to)
    }

    @Test
    fun rejectsInvalidPeriodAndInstructorId() = runBlocking {
        val now = Instant.parse("2026-09-25T12:00:00Z")
        val useCase = GetScheduleUseCase(load = { Result.success(ScheduleSnapshot(now, now, emptyList())) }, now = { now })

        assertTrue(useCase(ScheduleFilter(from = now, to = now)).isFailure)
        assertTrue(useCase(ScheduleFilter(instructorId = "not-a-uuid")).isFailure)
    }
}
