package org.example.client.features.schedule.data

import kotlinx.datetime.Instant
import org.example.client.features.schedule.domain.Instructor
import org.example.client.features.schedule.domain.ScheduleSnapshot
import org.example.client.features.schedule.domain.SlotStatus
import org.example.client.features.schedule.domain.TrainingFormat
import org.example.client.features.schedule.domain.TrainingSlotItem

object ScheduleMapper {
    fun toDomain(response: SlotListResponse): ScheduleSnapshot = ScheduleSnapshot(
        from = Instant.parse(response.from),
        to = Instant.parse(response.to),
        items = response.items.map(::toDomain),
    )

    fun toDomain(item: TrainingSlotSummary): TrainingSlotItem = TrainingSlotItem(
        id = item.id,
        startsAt = Instant.parse(item.startsAt),
        endsAt = Instant.parse(item.endsAt),
        format = TrainingFormat.fromApi(item.format) ?: error("Unknown training format"),
        zone = item.zone,
        address = item.address,
        instructor = Instructor(item.instructor.id, item.instructor.fullName, item.instructor.isActive),
        capacity = item.capacity,
        availablePlaces = item.availablePlaces,
        status = SlotStatus.fromApi(item.status) ?: error("Unknown slot status"),
        cancellationReason = item.cancellationReason,
    )
}
