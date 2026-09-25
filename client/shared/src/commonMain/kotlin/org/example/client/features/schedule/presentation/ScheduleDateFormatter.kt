package org.example.client.features.schedule.presentation

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatScheduleDateTime(value: Instant): String {
    val date = value.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d.%02d.%04d, %02d:%02d".format(
        date.dayOfMonth,
        date.month.ordinal + 1,
        date.year,
        date.hour,
        date.minute,
    )
}

fun formatScheduleDate(value: Instant): String {
    val date = value.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d.%02d.%04d".format(date.dayOfMonth, date.month.ordinal + 1, date.year)
}
