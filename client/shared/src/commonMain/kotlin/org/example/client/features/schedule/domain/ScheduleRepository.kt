package org.example.client.features.schedule.domain

interface ScheduleRepository {
    suspend fun getSchedule(filter: ScheduleFilter): Result<ScheduleSnapshot>
    suspend fun clearCache()
}
