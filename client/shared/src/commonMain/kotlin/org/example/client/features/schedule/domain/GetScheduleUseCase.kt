package org.example.client.features.schedule.domain

import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class GetScheduleUseCase(
    private val load: suspend (ScheduleFilter) -> Result<ScheduleSnapshot>,
    private val now: () -> Instant = { Clock.System.now() },
) {
    suspend operator fun invoke(filter: ScheduleFilter = ScheduleFilter()): Result<ScheduleSnapshot> {
        val from = filter.from ?: now()
        val to = filter.to ?: from.plus(7.days)
        if (to <= from) {
            return Result.failure(IllegalArgumentException("Период должен быть положительным"))
        }
        if (filter.instructorId != null && !uuidPattern.matches(filter.instructorId)) {
            return Result.failure(IllegalArgumentException("Некорректный идентификатор инструктора"))
        }
        return load(filter.copy(from = from, to = to))
    }

    private companion object {
        val uuidPattern = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")
    }
}
