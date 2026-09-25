package org.example.client.features.schedule.data

import kotlinx.datetime.Instant
import org.example.client.features.schedule.domain.ScheduleFilter
import org.example.client.features.schedule.domain.TrainingFormat
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleQueryBuilderTest {
    @Test
    fun buildsAllScheduleParameters() {
        val from = Instant.parse("2026-09-25T00:00:00Z")
        val to = Instant.parse("2026-10-02T00:00:00Z")
        val filter = ScheduleFilter(
            from = from,
            to = to,
            format = TrainingFormat.NOVICE_BOULDERING,
            instructorId = "22222222-2222-4222-8222-222222222222",
        )

        assertEquals(
            listOf(
                "from" to from.toString(),
                "to" to to.toString(),
                "format" to "novice_bouldering",
                "instructor_id" to "22222222-2222-4222-8222-222222222222",
            ),
            ScheduleQueryBuilder.build(filter),
        )
    }

    @Test
    fun omitsUnsetOptionalFilters() {
        assertEquals(emptyList(), ScheduleQueryBuilder.build(ScheduleFilter()))
    }
}
