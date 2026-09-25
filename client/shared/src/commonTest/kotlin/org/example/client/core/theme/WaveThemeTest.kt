package org.example.client.core.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class WaveThemeTest {
    @Test
    fun usesWavePrimaryInBothThemes() {
        assertEquals(Color(0xFF389F82), WaveLightColorScheme.primary)
        assertEquals(Color(0xFF4DB6AC), WaveDarkColorScheme.primary)
    }
}
