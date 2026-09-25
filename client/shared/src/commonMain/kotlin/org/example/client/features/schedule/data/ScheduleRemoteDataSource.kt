package org.example.client.features.schedule.data

import org.example.client.features.schedule.domain.ScheduleFilter

interface ScheduleRemoteDataSource {
    suspend fun load(token: String, filter: ScheduleFilter): Result<SlotListResponse>
}
