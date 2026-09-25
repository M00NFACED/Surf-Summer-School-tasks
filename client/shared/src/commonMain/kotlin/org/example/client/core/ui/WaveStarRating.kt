package org.example.client.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.features.review.domain.MaxReviewScore
import org.example.client.features.review.domain.MinReviewScore

@Composable
fun WaveStarRating(
    score: Int,
    onScoreSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.wave
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        (MinReviewScore..MaxReviewScore).forEach { value ->
            val filled = value <= score
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (filled) colors.accent else colors.iconMuted,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (filled) colors.accentSoft else colors.chip)
                    .clickable(enabled = enabled) { onScoreSelected(value) }
                    .semantics { contentDescription = "$value из $MaxReviewScore" }
                    .padding(9.dp),
            )
        }
    }
}

@Composable
fun WaveNoticeBar(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.wave.onAccentSoft,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
            .background(MaterialTheme.wave.accentSoft)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    )
}
