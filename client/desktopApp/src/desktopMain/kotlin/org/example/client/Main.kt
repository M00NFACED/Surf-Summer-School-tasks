package org.example.client

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.russhwolf.settings.PreferencesSettings
import org.example.client.core.network.BaseUrl
import org.example.client.core.network.NetworkConfig
import org.example.client.core.storage.SettingsTokenStorage

fun main() {
    val tokenStorage = SettingsTokenStorage(PreferencesSettings.Factory().create("surf"))
    application {
        Window(
            onCloseRequest = { exitApplication() },
            title = "Скалодром «Вертикаль»",
            state = rememberWindowState(width = 400.dp, height = 800.dp),
        ) {
            App(
                tokenStorage = tokenStorage,
                config = NetworkConfig(baseUrl = BaseUrl.LocalDesktop),
            )
        }
    }
}
