package org.example.client.core.storage

import com.russhwolf.settings.PreferencesSettings
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsTokenStorageTest {
    @Test
    fun persistsAndClearsToken() = runBlocking {
        val settings = PreferencesSettings.Factory().create("surf-token-storage-test")
        settings.clear()
        val storage = SettingsTokenStorage(settings)

        try {
            storage.write("token-value")
            assertEquals("token-value", storage.read())

            storage.clear()
            assertNull(storage.read())
        } finally {
            settings.clear()
        }
    }
}
