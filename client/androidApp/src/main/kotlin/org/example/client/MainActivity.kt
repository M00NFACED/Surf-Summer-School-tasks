package org.example.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.russhwolf.settings.SharedPreferencesSettings
import org.example.client.core.network.NetworkConfig
import org.example.client.core.storage.SettingsTokenStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenStorage = SettingsTokenStorage(
            SharedPreferencesSettings.Factory(applicationContext).create("surf"),
        )
        setContent {
            App(
                tokenStorage = tokenStorage,
                config = NetworkConfig(baseUrl = BuildConfig.API_BASE_URL),
            )
        }
    }
}
