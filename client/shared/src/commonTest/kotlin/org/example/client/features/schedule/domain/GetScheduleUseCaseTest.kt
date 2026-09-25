package org.example.client.features.schedule.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.example.client.features.booking.domain.testSlotItem

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
    fun acceptsBackendCanonicalInstructorIdAndNormalizesIt() = runBlocking {
        val now = Instant.parse("2026-09-25T12:00:00Z")
        val instructorId = "00000000-0000-0000-0000-000000000001"
        var captured: ScheduleFilter? = null
        val useCase = GetScheduleUseCase(
            load = { filter ->
                captured = filter
                Result.success(ScheduleSnapshot(now, now + 7.days, emptyList()))
            },
            now = { now },
        )

        val result = useCase(ScheduleFilter(instructorId = " $instructorId "))

        assertTrue(result.isSuccess)
        assertEquals(instructorId, captured?.instructorId)
    }

    @Test
    fun supportsOnlyUpperDateBoundary() = runBlocking {
        val now = Instant.parse("2026-09-25T12:00:00Z")
        val upperBound = now + 3.days
        var captured: ScheduleFilter? = null
        val useCase = GetScheduleUseCase(
            load = { filter ->
                captured = filter
                Result.success(ScheduleSnapshot(now, now + 7.days, emptyList()))
            },
            now = { now },
        )

        val result = useCase(ScheduleFilter(to = upperBound))

        assertTrue(result.isSuccess)
        assertEquals(upperBound.minus(7.days), captured?.from)
        assertEquals(upperBound, captured?.to)
    }

    @Test
    fun keepsOnlyAvailableSlotsWhenClientFilterEnabled() = runBlocking {
        val now = Instant.parse("2026-09-25T12:00:00Z")
        val useCase = GetScheduleUseCase(
            load = {
                Result.success(
                    ScheduleSnapshot(
                        from = now,
                        to = now + 7.days,
                        items = listOf(
                            testSlotItem(id = "11111111-1111-4111-8111-111111111111", availablePlaces = 2),
                            testSlotItem(id = "33333333-3333-4333-8333-333333333333", availablePlaces = 0),
                        ),
                    ),
                )
            },
            now = { now },
        )

        val result = useCase(ScheduleFilter(onlyAvailable = true))

        assertTrue(result.isSuccess)
        assertEquals(listOf("11111111-1111-4111-8111-111111111111"), result.getOrThrow().items.map { it.id })
    }

    @Test
    fun rejectsInvalidPeriodAndInstructorId() = runBlocking {
        val now = Instant.parse("2026-09-25T12:00:00Z")
        val useCase = GetScheduleUseCase(load = { Result.success(ScheduleSnapshot(now, now, emptyList())) }, now = { now })

        assertTrue(useCase(ScheduleFilter(from = now, to = now)).isFailure)
        assertTrue(useCase(ScheduleFilter(instructorId = "not-a-uuid")).isFailure)
    }
}
