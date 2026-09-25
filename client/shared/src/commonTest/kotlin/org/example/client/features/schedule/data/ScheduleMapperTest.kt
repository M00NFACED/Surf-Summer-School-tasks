package org.example.client.features.schedule.data

import org.example.client.features.schedule.domain.SlotStatus
import org.example.client.features.schedule.domain.TrainingFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduleMapperTest {
    @Test
    fun mapsDtoToDomainAndFormatRules() {
        val item = ScheduleMapper.toDomain(testSlotSummary())

        assertEquals("Новичковый болдеринг", item.format.displayName)
        assertEquals(8, item.format.maxCapacity)
        assertEquals("Анна Петрова", item.instructor.fullName)
        assertEquals(SlotStatus.AVAILABLE, item.status)
        assertTrue(item.isAvailable)
    }

    @Test
    fun mapsUnavailableAndCancelledSlots() {
        assertFalse(ScheduleMapper.toDomain(testSlotSummary(availablePlaces = 0)).isAvailable)
        assertFalse(ScheduleMapper.toDomain(testSlotSummary(status = "cancelled")).isAvailable)
        assertEquals(TrainingFormat.ROPE_ROUTES.maxCapacity, 16)
    }
}
