package org.example.client

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = { exitApplication() },
        title = "Скалодром «Вертикаль»",
        state = rememberWindowState(width = 400.dp, height = 800.dp),
    ) {
        App()
    }
}
