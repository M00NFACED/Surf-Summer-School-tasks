package org.example.client.core.ui

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ArtworkVariantTest {
    @Test
    fun bouldersUseThreeLayeredSilhouettesWithRequestedOpacity() {
        val geometry = bouldersGeometry(seed = 42)

        assertEquals(3, geometry.layers.size)
        assertEquals(listOf(0.15f, 0.35f, 0.6f), geometry.layers.map { it.alpha })
        assertEquals(BoulderLayerAlphas, geometry.layers.map { it.alpha })
    }

    @Test
    fun boulderRouteIsAnArcOfFiveHoldsOnTheFrontRock() {
        val geometry = bouldersGeometry(seed = 42)

        assertEquals(5, geometry.holds.size)
        assertTrue(geometry.holds.all { it.x in 0.05f..0.95f })
        assertTrue(geometry.holds.all { it.y in 0.70f..0.98f })
        val middle = geometry.layers[1]
        assertTrue(geometry.holds.all { hold -> hold.y > middle.humps.minOf { it.y } })
        assertTrue(geometry.holds[2].y <= geometry.holds.first().y, "holds must form an upward arc")
    }

    @Test
    fun spiresKeepPointedPeaksAndVerticalRope() {
        val geometry = spiresGeometry(seed = 11)

        assertTrue(geometry.near.size >= 5)
        val peak = geometry.near.minBy { it.y }
        assertTrue(peak.y < 0.30f, "main peak must stay high")
        val ropeX = geometry.rope.map { it.x }
        assertEquals(1, ropeX.distinct().size, "rope must be vertical")
        assertTrue(geometry.rope.first().y < geometry.rope.last().y)
    }

    @Test
    fun spireSunSitsLowBehindRidgesAndNoLineCrossesItsCenter() {
        listOf(0, 1, 7, 42, 12345).forEach { seed ->
            val geometry = spiresGeometry(seed)
            assertTrue(geometry.sun.y in 0.25f..0.45f, "sun must stay low, seed=$seed")
            ridgeLines(geometry).forEach { ridge ->
                ridge.zipWithNext().forEach { (from, to) ->
                    val distance = distanceToSegment(geometry.sun, from, to)
                    assertTrue(
                        distance > geometry.sunRadius * 0.45f,
                        "ridge line too close to sun center, seed=$seed distance=$distance",
                    )
                }
            }
        }
    }

    @Test
    fun spireRopeStaysAwayFromSunAndScreenEdges() {
        val geometry = spiresGeometry(seed = 11)
        val ropeX = geometry.rope.first().x

        assertTrue(abs(ropeX - geometry.sun.x) > 0.2f, "rope must not cross the sun")
        assertTrue(ropeX in 0.12f..0.88f, "rope must not touch the screen edge")
        assertTrue(geometry.rope.all { it.y in 0.05f..0.95f })
    }

    @Test
    fun differentSlotsProduceDifferentArtwork() {
        assertNotEquals(
            bouldersGeometry(artworkSeed("slot-1", "instructor-1")).layers,
            bouldersGeometry(artworkSeed("slot-2", "instructor-2")).layers,
        )
        assertNotEquals(
            spiresGeometry(artworkSeed("slot-1", "a")).near,
            spiresGeometry(artworkSeed("slot-2", "b")).near,
        )
    }

    @Test
    fun seedIsStableForTheSameSlot() {
        assertEquals(artworkSeed("slot-1", "instructor-1"), artworkSeed("slot-1", "instructor-1"))
        assertNotEquals(artworkSeed("slot-1", "instructor-1"), artworkSeed("slot-2", "instructor-1"))
    }

    @Test
    fun palettesDifferBetweenStyles() {
        val accentSoft = androidx.compose.ui.graphics.Color(0xFFDCEFEB)
        val morning = lightArtworkPalette(ArtworkStyle.MORNING_BOULDERS, accentSoft)
        val evening = lightArtworkPalette(ArtworkStyle.EVENING_SPIRES, accentSoft)

        assertNotEquals(morning.skyStops.first(), evening.skyStops.first())
        assertTrue(evening.skyStops.size >= 3)
    }

    @Test
    fun artworkBlockUsesFixedBannerProportions() {
        assertEquals(120, ArtworkBlockHeight.value.toInt())
        assertEquals(16, ArtworkCornerRadius.value.toInt())
    }
}

private fun distanceToSegment(point: ArtworkPoint, from: ArtworkPoint, to: ArtworkPoint): Float {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val lengthSquared = dx * dx + dy * dy
    if (lengthSquared == 0f) {
        val px = point.x - from.x
        val py = point.y - from.y
        return kotlin.math.sqrt(px * px + py * py)
    }
    val t = (((point.x - from.x) * dx + (point.y - from.y) * dy) / lengthSquared).coerceIn(0f, 1f)
    val px = from.x + t * dx
    val py = from.y + t * dy
    val ex = point.x - px
    val ey = point.y - py
    return kotlin.math.sqrt(ex * ex + ey * ey)
}
