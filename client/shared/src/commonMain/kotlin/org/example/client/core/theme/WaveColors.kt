package org.example.client.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class WaveColors(
    val accent: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val onAccentSoft: Color,
    val card: Color,
    val cardInner: Color,
    val chip: Color,
    val onChip: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val badgeGreen: Color,
    val onBadgeGreen: Color,
    val badgeYellow: Color,
    val onBadgeYellow: Color,
    val pill: Color,
    val iconMuted: Color,
)

val WaveLightColors = WaveColors(
    accent = Color(0xFF4E9E92),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0xFFDCEFEB),
    onAccentSoft = Color(0xFF11443D),
    card = Color(0xFFF1F1F1),
    cardInner = Color(0xFFFFFFFF),
    chip = Color(0xFFEFEFEF),
    onChip = Color(0xFF1A1A1A),
    border = Color(0xFFDCDCDC),
    textPrimary = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF6B6B6B),
    badgeGreen = Color(0xFFC9E7C4),
    onBadgeGreen = Color(0xFF1E5B2A),
    badgeYellow = Color(0xFFF5EFA6),
    onBadgeYellow = Color(0xFF4A4300),
    pill = Color(0xFFFFFFFF),
    iconMuted = Color(0xFF9A9A9A),
)

val WaveDarkColors = WaveColors(
    accent = Color(0xFF6FBFB2),
    onAccent = Color(0xFF06201C),
    accentSoft = Color(0xFF1C3D38),
    onAccentSoft = Color(0xFFBFE6DF),
    card = Color(0xFF1A211F),
    cardInner = Color(0xFF131A19),
    chip = Color(0xFF262E2C),
    onChip = Color(0xFFE6EAE8),
    border = Color(0xFF323B39),
    textPrimary = Color(0xFFE6EAE8),
    textSecondary = Color(0xFFA6B0AD),
    badgeGreen = Color(0xFF2C4A32),
    onBadgeGreen = Color(0xFFC9E7C4),
    badgeYellow = Color(0xFF47431F),
    onBadgeYellow = Color(0xFFF5EFA6),
    pill = Color(0xFF1A211F),
    iconMuted = Color(0xFF7C8783),
)

val LocalWaveColors = staticCompositionLocalOf { WaveLightColors }

val MaterialTheme.wave: WaveColors
    @Composable
    @ReadOnlyComposable
    get() = LocalWaveColors.current
