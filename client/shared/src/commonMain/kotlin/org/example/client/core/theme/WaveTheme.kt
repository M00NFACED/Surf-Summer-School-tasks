package org.example.client.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val WaveLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF389F82),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB6F0DF),
    onPrimaryContainer = Color(0xFF002018),
    inversePrimary = Color(0xFF5CD6B5),
    secondary = Color(0xFF4B9B8A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDEFE4),
    onSecondaryContainer = Color(0xFF072019),
    tertiary = Color(0xFF3F6F8F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC5E7FF),
    onTertiaryContainer = Color(0xFF001E2C),
    background = Color(0xFFF5FBF8),
    onBackground = Color(0xFF171D1B),
    surface = Color(0xFFF5FBF8),
    onSurface = Color(0xFF171D1B),
    surfaceVariant = Color(0xFFDCE9E3),
    onSurfaceVariant = Color(0xFF3F4945),
    surfaceTint = Color(0xFF389F82),
    inverseSurface = Color(0xFF2B322F),
    inverseOnSurface = Color(0xFFECF2EE),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF6F7975),
    outlineVariant = Color(0xFFBEC9C3),
    scrim = Color(0xFF000000),
)

val WaveDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF1D6B5B),
    onPrimaryContainer = Color(0xFFB6F0DF),
    inversePrimary = Color(0xFF389F82),
    secondary = Color(0xFFB1D3C8),
    onSecondary = Color(0xFF1D352D),
    secondaryContainer = Color(0xFF334B43),
    onSecondaryContainer = Color(0xFFCDEFE4),
    tertiary = Color(0xFFA9CBE3),
    onTertiary = Color(0xFF0B3448),
    tertiaryContainer = Color(0xFF264B60),
    onTertiaryContainer = Color(0xFFC5E7FF),
    background = Color(0xFF0F1513),
    onBackground = Color(0xFFDFE4E1),
    surface = Color(0xFF0F1513),
    onSurface = Color(0xFFDFE4E1),
    surfaceVariant = Color(0xFF3F4945),
    onSurfaceVariant = Color(0xFFBEC9C3),
    surfaceTint = Color(0xFF4DB6AC),
    inverseSurface = Color(0xFFDFE4E1),
    inverseOnSurface = Color(0xFF2B322F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF89938E),
    outlineVariant = Color(0xFF3F4945),
    scrim = Color(0xFF000000),
)

@Composable
fun WaveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) WaveDarkColorScheme else WaveLightColorScheme,
        content = content,
    )
}
