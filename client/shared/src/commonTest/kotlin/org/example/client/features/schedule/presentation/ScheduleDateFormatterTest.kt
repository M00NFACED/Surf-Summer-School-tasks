package org.example.client.features.schedule.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ScheduleDateFormatterTest {
    @Test
    fun formatsCardDateWithWeekdayMonthAndTime() {
        val value = Instant.parse("2026-06-21T10:00:00Z")

        val formatted = formatSlotCardDate(value)

        assertTrue(formatted.contains("21 июня ·"), "unexpected: $formatted")
    }

    @Test
    fun parsesFilterDateInNumericFormat() {
        val parsed = parseScheduleDate("21.06.2026")

        assertEquals(2026, parsed?.toLocalDateTime(TimeZone.currentSystemDefault())?.year)
        assertEquals(6, parsed?.toLocalDateTime(TimeZone.currentSystemDefault())?.monthNumber)
        assertEquals(21, parsed?.toLocalDateTime(TimeZone.currentSystemDefault())?.dayOfMonth)
    }

    @Test
    fun returnsNullForInvalidFilterDate() {
        assertEquals(null, parseScheduleDate("не дата"))
    }
}
