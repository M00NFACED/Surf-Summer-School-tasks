package org.example.client.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ArtworkVariantTest {
    @Test
    fun morningBouldersAreRounderAndLowerThanEveningSpires() {
        val morning = ridgeProfile(ArtworkStyle.MORNING_BOULDERS, seed = 42, layer = 1)
        val evening = ridgeProfile(ArtworkStyle.EVENING_SPIRES, seed = 42, layer = 1)

        assertEquals(3, morning.size)
        assertEquals(5, evening.size)
        assertTrue(evening.map { it.y }.max() < morning.map { it.y }.max())
    }

    @Test
    fun profilesStayInsideSafeBounds() {
        ArtworkStyle.values().forEach { style ->
            (0..2).forEach { layer ->
                ridgeProfile(style, seed = 7, layer = layer).forEach { point ->
                    assertTrue(point.x in 0f..1f, "x out of range: ${point.x}")
                    assertTrue(point.y in 0.05f..0.95f, "y out of range: ${point.y}")
                }
            }
        }
    }

    @Test
    fun differentSlotsProduceDifferentArtwork() {
        val first = ridgeProfile(ArtworkStyle.MORNING_BOULDERS, artworkSeed("slot-1", "instructor-1"), layer = 2)
        val second = ridgeProfile(ArtworkStyle.MORNING_BOULDERS, artworkSeed("slot-2", "instructor-2"), layer = 2)

        assertNotEquals(first, second)
    }

    @Test
    fun seedIsStableForTheSameSlot() {
        assertEquals(artworkSeed("slot-1", "instructor-1"), artworkSeed("slot-1", "instructor-1"))
        assertNotEquals(artworkSeed("slot-1", "instructor-1"), artworkSeed("slot-2", "instructor-1"))
    }

    @Test
    fun eveningRouteEndsHigherThanMorningRoute() {
        val morning = routeGeometry(ArtworkStyle.MORNING_BOULDERS, seed = 3)
        val evening = routeGeometry(ArtworkStyle.EVENING_SPIRES, seed = 3)

        assertTrue(evening.end.y < morning.end.y)
    }

    @Test
    fun palettesDifferBetweenStyles() {
        val morning = lightArtworkPalette(ArtworkStyle.MORNING_BOULDERS, accentSoft = androidx.compose.ui.graphics.Color(0xFFDCEFEB))
        val evening = lightArtworkPalette(ArtworkStyle.EVENING_SPIRES, accentSoft = androidx.compose.ui.graphics.Color(0xFFDCEFEB))

        assertNotEquals(morning.skyTop, evening.skyTop)
        assertTrue(morning.vertical.not())
        assertTrue(evening.vertical)
    }
}
