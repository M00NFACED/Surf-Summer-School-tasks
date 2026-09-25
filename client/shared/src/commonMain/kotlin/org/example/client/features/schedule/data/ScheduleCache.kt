package org.example.client.features.schedule.data

import org.example.client.features.schedule.domain.ScheduleSnapshot

interface ScheduleCache {
    suspend fun read(): ScheduleSnapshot?
    suspend fun write(snapshot: ScheduleSnapshot)
    suspend fun clear()
}
