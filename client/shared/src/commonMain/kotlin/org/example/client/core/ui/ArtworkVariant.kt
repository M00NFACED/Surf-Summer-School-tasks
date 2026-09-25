package org.example.client.core.ui

import androidx.compose.ui.graphics.Color

enum class ArtworkStyle { MORNING_BOULDERS, EVENING_SPIRES }

internal fun artworkSeed(slotId: String, instructorId: String): Int =
    (slotId + instructorId).fold(7) { acc, char -> acc * 31 + char.code }

internal data class ArtworkPalette(
    val skyTop: Color,
    val skyBottom: Color,
    val sun: Color,
    val ridgeFar: Color,
    val ridgeMid: Color,
    val ridgeNear: Color,
    val route: Color,
    val vertical: Boolean,
)

internal fun lightArtworkPalette(style: ArtworkStyle, accentSoft: Color): ArtworkPalette = when (style) {
    ArtworkStyle.MORNING_BOULDERS -> ArtworkPalette(
        skyTop = accentSoft,
        skyBottom = Color(0xFFF4F9F7),
        sun = Color(0xFFB6DDD4),
        ridgeFar = Color(0xFFBFDCD4),
        ridgeMid = Color(0xFF8DC2B7),
        ridgeNear = Color(0xFF5C9A8D),
        route = Color(0xFFFFFFFF),
        vertical = false,
    )
    ArtworkStyle.EVENING_SPIRES -> ArtworkPalette(
        skyTop = Color(0xFF2E5F72),
        skyBottom = Color(0xFFF0B98A),
        sun = Color(0xFFFCE3C0),
        ridgeFar = Color(0xFF7C93A0),
        ridgeMid = Color(0xFF4E6672),
        ridgeNear = Color(0xFF2B3D47),
        route = Color(0xFFFFF1DC),
        vertical = true,
    )
}

internal fun darkArtworkPalette(style: ArtworkStyle): ArtworkPalette = when (style) {
    ArtworkStyle.MORNING_BOULDERS -> ArtworkPalette(
        skyTop = Color(0xFF0F1C19),
        skyBottom = Color(0xFF16302B),
        sun = Color(0xFF3E7A6E),
        ridgeFar = Color(0xFF24443D),
        ridgeMid = Color(0xFF1B342E),
        ridgeNear = Color(0xFF11221E),
        route = Color(0xFF8FD3C5),
        vertical = false,
    )
    ArtworkStyle.EVENING_SPIRES -> ArtworkPalette(
        skyTop = Color(0xFF12212E),
        skyBottom = Color(0xFF5A3340),
        sun = Color(0xFFF0B27A),
        ridgeFar = Color(0xFF3C4E5C),
        ridgeMid = Color(0xFF2A3946),
        ridgeNear = Color(0xFF1A2530),
        route = Color(0xFFFFD9A8),
        vertical = true,
    )
}

internal fun ridgeProfile(
    style: ArtworkStyle,
    seed: Int,
    layer: Int,
): List<ArtworkPoint> {
    val compact = style == ArtworkStyle.MORNING_BOULDERS
    val count = if (compact) 3 else 5
    val baseY = when (layer) {
        0 -> if (compact) 0.34f else 0.22f
        1 -> if (compact) 0.50f else 0.36f
        else -> if (compact) 0.66f else 0.54f
    }
    val amplitude = if (compact) 0.10f else 0.22f
    return (0 until count).map { index ->
        val x = (index + 1f) / (count + 1f)
        val jitter = ((seed shr (index * 3 + layer * 2)) and 0x3) / 3f
        val peak = if (compact) baseY + amplitude * (0.2f + jitter * 0.8f) else baseY + amplitude * jitter
        ArtworkPoint(x, peak.coerceIn(0.10f, 0.92f))
    }
}

internal data class ArtworkPoint(val x: Float, val y: Float)

internal data class RouteGeometry(
    val start: ArtworkPoint,
    val control: ArtworkPoint,
    val end: ArtworkPoint,
)

internal fun routeGeometry(style: ArtworkStyle, seed: Int): RouteGeometry {
    val jitter = (seed and 0x7) / 7f
    return RouteGeometry(
        start = ArtworkPoint(0.14f, 0.82f + jitter * 0.04f),
        control = ArtworkPoint(0.44f + jitter * 0.08f, 0.52f),
        end = ArtworkPoint(
            x = if (style == ArtworkStyle.MORNING_BOULDERS) 0.86f else 0.78f + jitter * 0.1f,
            y = if (style == ArtworkStyle.MORNING_BOULDERS) 0.30f else 0.22f,
        ),
    )
}
