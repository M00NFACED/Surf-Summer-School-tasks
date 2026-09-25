package org.example.client.core.ui

import androidx.compose.ui.graphics.Color

enum class ArtworkStyle { MORNING_BOULDERS, EVENING_SPIRES }

internal fun artworkSeed(slotId: String, instructorId: String): Int {
    val base = (slotId + instructorId).fold(7) { acc, char -> acc * 31 + char.code }
    var mixed = base
    mixed = mixed xor (mixed ushr 16)
    mixed *= 0x45D9F3B
    mixed = mixed xor (mixed ushr 16)
    mixed *= 0x45D9F3B
    mixed = mixed xor (mixed ushr 16)
    return mixed
}

internal data class ArtworkPoint(val x: Float, val y: Float)

internal data class ArtworkPalette(
    val skyStops: List<Color>,
    val sun: Color,
    val rock: Color,
    val route: Color,
)

internal val BoulderLayerAlphas = listOf(0.15f, 0.35f, 0.6f)

internal fun lightArtworkPalette(style: ArtworkStyle, accentSoft: Color): ArtworkPalette = when (style) {
    ArtworkStyle.MORNING_BOULDERS -> ArtworkPalette(
        skyStops = listOf(accentSoft, Color(0xFFE7F3EF), Color(0xFFF6FAF8)),
        sun = Color(0xFFFFFFFF),
        rock = Color(0xFF2F8C79),
        route = Color(0xFFFFFFFF),
    )
    ArtworkStyle.EVENING_SPIRES -> ArtworkPalette(
        skyStops = listOf(Color(0xFF24475C), Color(0xFFD97A55), Color(0xFFF6C79A)),
        sun = Color(0xFFFFE9C4),
        rock = Color(0xFF1B2A36),
        route = Color(0xFFFFEBD2),
    )
}

internal fun darkArtworkPalette(style: ArtworkStyle): ArtworkPalette = when (style) {
    ArtworkStyle.MORNING_BOULDERS -> ArtworkPalette(
        skyStops = listOf(Color(0xFF12211E), Color(0xFF1A322C), Color(0xFF21403A)),
        sun = Color(0xFF8FD3C5),
        rock = Color(0xFF7FC9B9),
        route = Color(0xFFEAF7F3),
    )
    ArtworkStyle.EVENING_SPIRES -> ArtworkPalette(
        skyStops = listOf(Color(0xFF16232F), Color(0xFF6A3A45), Color(0xFFB9743F)),
        sun = Color(0xFFFFCE93),
        rock = Color(0xFF0E1720),
        route = Color(0xFFFFDDB4),
    )
}

internal data class BoulderLayer(
    val humps: List<ArtworkPoint>,
    val baseY: Float,
    val alpha: Float,
)

internal data class BouldersGeometry(
    val layers: List<BoulderLayer>,
    val holds: List<ArtworkPoint>,
    val sun: ArtworkPoint,
    val sunRadius: Float,
)

internal fun bouldersGeometry(seed: Int): BouldersGeometry {
    val jitter = { step: Int -> ((seed shr step) and 0x3) / 3f }
    val back = listOf(
        ArtworkPoint(0f, 0.60f + jitter(2) * 0.04f),
        ArtworkPoint(0.26f, 0.66f + jitter(3) * 0.05f),
        ArtworkPoint(0.58f, 0.62f + jitter(4) * 0.05f),
        ArtworkPoint(1f, 0.70f + jitter(5) * 0.04f),
    )
    val middle = listOf(
        ArtworkPoint(0f, 0.74f + jitter(6) * 0.04f),
        ArtworkPoint(0.34f, 0.80f + jitter(7) * 0.05f),
        ArtworkPoint(0.68f, 0.76f + jitter(8) * 0.05f),
        ArtworkPoint(1f, 0.84f + jitter(9) * 0.04f),
    )
    val front = listOf(
        ArtworkPoint(0f, 0.88f + jitter(10) * 0.03f),
        ArtworkPoint(0.22f, 0.94f + jitter(11) * 0.03f),
        ArtworkPoint(0.52f, 0.90f + jitter(12) * 0.03f),
        ArtworkPoint(0.78f, 0.96f + jitter(13) * 0.02f),
        ArtworkPoint(1f, 0.92f + jitter(14) * 0.03f),
    )
    val holds = (0 until 5).map { index ->
        val t = index / 4f
        ArtworkPoint(
            x = 0.30f + t * 0.34f,
            y = 0.90f - (0.12f * kotlin.math.sin(t * Math.PI).toFloat()) + jitter(index + 16) * 0.012f,
        )
    }
    return BouldersGeometry(
        layers = listOf(
            BoulderLayer(back, baseY = 1.05f, alpha = BoulderLayerAlphas[0]),
            BoulderLayer(middle, baseY = 1.08f, alpha = BoulderLayerAlphas[1]),
            BoulderLayer(front, baseY = 1.12f, alpha = BoulderLayerAlphas[2]),
        ),
        holds = holds,
        sun = ArtworkPoint(0.76f, 0.20f),
        sunRadius = 0.07f,
    )
}

internal data class SpiresGeometry(
    val far: List<ArtworkPoint>,
    val mid: List<ArtworkPoint>,
    val near: List<ArtworkPoint>,
    val baseY: Float,
    val sun: ArtworkPoint,
    val sunRadius: Float,
    val rope: List<ArtworkPoint>,
    val ropeDashes: FloatArray,
)

internal fun spiresGeometry(seed: Int): SpiresGeometry {
    val jitter = { step: Int -> ((seed shr step) and 0x3) / 3f }
    val far = listOf(
        ArtworkPoint(0f, 0.52f),
        ArtworkPoint(0.18f, 0.42f + jitter(2) * 0.04f),
        ArtworkPoint(0.52f, 0.48f + jitter(3) * 0.03f),
        ArtworkPoint(0.84f, 0.44f + jitter(4) * 0.04f),
        ArtworkPoint(1f, 0.56f),
    )
    val mid = listOf(
        ArtworkPoint(0f, 0.66f),
        ArtworkPoint(0.30f, 0.54f + jitter(5) * 0.03f),
        ArtworkPoint(0.78f, 0.58f + jitter(6) * 0.03f),
        ArtworkPoint(1f, 0.70f),
    )
    val peakX = 0.34f + jitter(7) * 0.04f
    val peakY = 0.20f + jitter(8) * 0.03f
    val near = listOf(
        ArtworkPoint(0f, 0.82f),
        ArtworkPoint(peakX * 0.55f, 0.48f + jitter(9) * 0.03f),
        ArtworkPoint(peakX, peakY),
        ArtworkPoint(0.66f, 0.44f + jitter(10) * 0.04f),
        ArtworkPoint(1f, 0.64f),
    )
    val ropeTop = ArtworkPoint(peakX + 0.012f, peakY + 0.10f)
    val ropeBottom = ArtworkPoint(peakX + 0.012f, 0.70f)
    return SpiresGeometry(
        far = far,
        mid = mid,
        near = near,
        baseY = 1.12f,
        sun = ArtworkPoint(0.72f, 0.52f),
        sunRadius = 0.085f,
        rope = listOf(ropeTop, ropeBottom),
        ropeDashes = floatArrayOf(0.024f, 0.018f),
    )
}
