package org.example.client.core.storage

interface TokenStorage {
    suspend fun read(): String?
    suspend fun write(token: String)
    suspend fun clear()
}
