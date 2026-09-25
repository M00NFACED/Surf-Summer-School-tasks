package org.example.client.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val errorLight = Color(0xFFC62828)
private val errorContainerLight = Color(0xFFFBE0E0)
private val onErrorContainerLight = Color(0xFF5C1010)
private val pageLight = Color(0xFFFFFFFF)
private val pageDark = Color(0xFF0F1513)
private val errorDark = Color(0xFFFF8A80)
private val errorContainerDark = Color(0xFF5C1010)
private val onErrorContainerDark = Color(0xFFFBE0E0)
private val scrim = Color(0x99000000)

val WaveLightColorScheme: ColorScheme = lightColorScheme(
    primary = WaveLightColors.accent,
    onPrimary = WaveLightColors.onAccent,
    primaryContainer = WaveLightColors.accentSoft,
    onPrimaryContainer = WaveLightColors.onAccentSoft,
    inversePrimary = WaveDarkColors.accent,
    secondary = WaveLightColors.accent,
    onSecondary = WaveLightColors.onAccent,
    secondaryContainer = WaveLightColors.chip,
    onSecondaryContainer = WaveLightColors.onChip,
    tertiary = WaveLightColors.accent,
    onTertiary = WaveLightColors.onAccent,
    tertiaryContainer = WaveLightColors.accentSoft,
    onTertiaryContainer = WaveLightColors.onAccentSoft,
    background = pageLight,
    onBackground = WaveLightColors.textPrimary,
    surface = pageLight,
    onSurface = WaveLightColors.textPrimary,
    surfaceVariant = WaveLightColors.card,
    onSurfaceVariant = WaveLightColors.textSecondary,
    surfaceTint = WaveLightColors.accent,
    inverseSurface = WaveDarkColors.card,
    inverseOnSurface = WaveDarkColors.textPrimary,
    error = errorLight,
    onError = WaveLightColors.onAccent,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    outline = WaveLightColors.border,
    outlineVariant = WaveLightColors.chip,
    scrim = scrim,
)

val WaveDarkColorScheme: ColorScheme = darkColorScheme(
    primary = WaveDarkColors.accent,
    onPrimary = WaveDarkColors.onAccent,
    primaryContainer = WaveDarkColors.accentSoft,
    onPrimaryContainer = WaveDarkColors.onAccentSoft,
    inversePrimary = WaveLightColors.accent,
    secondary = WaveDarkColors.accent,
    onSecondary = WaveDarkColors.onAccent,
    secondaryContainer = WaveDarkColors.chip,
    onSecondaryContainer = WaveDarkColors.onChip,
    tertiary = WaveDarkColors.accent,
    onTertiary = WaveDarkColors.onAccent,
    tertiaryContainer = WaveDarkColors.accentSoft,
    onTertiaryContainer = WaveDarkColors.onAccentSoft,
    background = pageDark,
    onBackground = WaveDarkColors.textPrimary,
    surface = pageDark,
    onSurface = WaveDarkColors.textPrimary,
    surfaceVariant = WaveDarkColors.card,
    onSurfaceVariant = WaveDarkColors.textSecondary,
    surfaceTint = WaveDarkColors.accent,
    inverseSurface = WaveLightColors.textPrimary,
    inverseOnSurface = WaveLightColors.card,
    error = errorDark,
    onError = WaveDarkColors.cardInner,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    outline = WaveDarkColors.border,
    outlineVariant = WaveDarkColors.chip,
    scrim = scrim,
)

@Composable
fun WaveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalWaveColors provides if (darkTheme) WaveDarkColors else WaveLightColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) WaveDarkColorScheme else WaveLightColorScheme,
            content = content,
        )
    }
}
