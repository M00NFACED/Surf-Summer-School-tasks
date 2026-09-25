package org.example.client.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave

enum class WaveBadgeTone { GREEN, YELLOW, ACCENT }

@Composable
fun WaveBadge(
    text: String,
    tone: WaveBadgeTone,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.wave
    val (background, content) = when (tone) {
        WaveBadgeTone.GREEN -> colors.badgeGreen to colors.onBadgeGreen
        WaveBadgeTone.YELLOW -> colors.badgeYellow to colors.onBadgeYellow
        WaveBadgeTone.ACCENT -> colors.accentSoft to colors.onAccentSoft
    }
    Row(
        modifier = modifier
            .background(background, CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = content)
    }
}

@Composable
fun WaveBadgeRow(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        content()
    }
}
