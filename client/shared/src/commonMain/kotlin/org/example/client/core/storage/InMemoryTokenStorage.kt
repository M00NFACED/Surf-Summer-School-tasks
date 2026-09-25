package org.example.client.core.storage

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryTokenStorage : TokenStorage {
    private val mutex = Mutex()
    private var token: String? = null

    override suspend fun read(): String? = mutex.withLock { token }

    override suspend fun write(token: String) {
        mutex.withLock { this.token = token }
    }

    override suspend fun clear() {
        mutex.withLock { token = null }
    }
}
