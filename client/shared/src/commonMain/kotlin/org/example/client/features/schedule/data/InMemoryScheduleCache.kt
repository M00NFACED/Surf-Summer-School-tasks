package org.example.client.features.schedule.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.client.features.schedule.domain.ScheduleSnapshot

class InMemoryScheduleCache : ScheduleCache {
    private val mutex = Mutex()
    private var snapshot: ScheduleSnapshot? = null

    override suspend fun read(): ScheduleSnapshot? = mutex.withLock { snapshot }

    override suspend fun write(snapshot: ScheduleSnapshot) {
        mutex.withLock { this.snapshot = snapshot.copy(isOffline = false) }
    }

    override suspend fun clear() {
        mutex.withLock { snapshot = null }
    }
}
