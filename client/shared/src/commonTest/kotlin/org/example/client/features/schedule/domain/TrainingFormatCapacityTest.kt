package org.example.client.features.schedule.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrainingFormatCapacityTest {
    @Test
    fun noviceBoulderingIsLimitedToEightAndRopeRoutesToSixteen() {
        assertEquals(8, TrainingFormat.NOVICE_BOULDERING.maxCapacity)
        assertEquals(16, TrainingFormat.ROPE_ROUTES.maxCapacity)
    }

    @Test
    fun formatsAreMappedFromBackendApiValues() {
        assertEquals(TrainingFormat.NOVICE_BOULDERING, TrainingFormat.fromApi("novice_bouldering"))
        assertEquals(TrainingFormat.ROPE_ROUTES, TrainingFormat.fromApi("rope_routes"))
        assertEquals(null, TrainingFormat.fromApi("unknown"))
    }

    @Test
    fun everyFormatLimitStaysWithinApiCapacityRange() {
        TrainingFormat.values().forEach { format ->
            assertTrue(format.maxCapacity in 1..16, "unexpected limit for ${format.apiValue}")
        }
    }
}
