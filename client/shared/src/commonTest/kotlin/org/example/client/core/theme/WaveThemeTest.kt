package org.example.client.core.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class WaveThemeTest {
    @Test
    fun usesDesignSystemAccentInBothThemes() {
        assertEquals(Color(0xFF4E9E92), WaveLightColors.accent)
        assertEquals(Color(0xFF6FBFB2), WaveDarkColors.accent)
        assertEquals(WaveLightColors.accent, WaveLightColorScheme.primary)
        assertEquals(WaveDarkColors.accent, WaveDarkColorScheme.primary)
    }

    @Test
    fun keepsBadgeAndCardTonesForBothThemes() {
        assertEquals(Color(0xFFF1F1F1), WaveLightColors.card)
        assertEquals(Color(0xFFC9E7C4), WaveLightColors.badgeGreen)
        assertEquals(Color(0xFFF5EFA6), WaveLightColors.badgeYellow)
        assertEquals(Color(0xFFEFEFEF), WaveLightColors.chip)
    }
}
