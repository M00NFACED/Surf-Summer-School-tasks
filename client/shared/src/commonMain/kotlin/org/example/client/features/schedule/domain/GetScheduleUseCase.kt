package org.example.client.features.schedule.domain

import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.example.client.core.validation.UuidValidator

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
        val instructorId = filter.instructorId?.let(UuidValidator::normalize)
        if (filter.instructorId != null && instructorId == null) {
            return Result.failure(IllegalArgumentException("Некорректный идентификатор инструктора"))
        }
        return load(filter.copy(from = from, to = to, instructorId = instructorId))
    }
}
