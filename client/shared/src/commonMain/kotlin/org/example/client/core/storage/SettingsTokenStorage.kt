package org.example.client.core.storage

import com.russhwolf.settings.Settings

class SettingsTokenStorage(private val settings: Settings) : TokenStorage {
    private val tokenKey = "surf.auth.access_token"

    override suspend fun read(): String? = settings.getStringOrNull(tokenKey)

    override suspend fun write(token: String) {
        settings.putString(tokenKey, token)
    }

    override suspend fun clear() {
        settings.remove(tokenKey)
    }
}
