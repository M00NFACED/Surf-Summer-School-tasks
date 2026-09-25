package org.example.client.features.schedule.presentation

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

private val monthNames = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря",
)

private val weekdayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

fun formatSlotCardDate(value: Instant): String {
    val dateTime = value.toLocalDateTime(TimeZone.currentSystemDefault())
    val weekday = weekdayNames.getOrElse(dateTime.dayOfWeek.ordinal) { "" }
    val month = monthNames.getOrElse(dateTime.month.ordinal) { "" }
    return "$weekday, ${dateTime.dayOfMonth} $month · %02d:%02d".format(dateTime.hour, dateTime.minute)
}

fun formatSlotTimeRange(start: Instant, end: Instant): String {
    val startTime = start.toLocalDateTime(TimeZone.currentSystemDefault())
    val endTime = end.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d — %02d:%02d".format(startTime.hour, startTime.minute, endTime.hour, endTime.minute)
}

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

fun formatScheduleDate(value: Instant): String =
    formatScheduleDate(value.toLocalDateTime(TimeZone.currentSystemDefault()).date)

fun formatScheduleDate(date: LocalDate): String =
    "%02d.%02d.%04d".format(date.dayOfMonth, date.month.ordinal + 1, date.year)

fun parseScheduleDate(text: String): Instant? = runCatching {
    val normalized = text.trim()
    val parts = normalized.split(".", "-", "/")
    require(parts.size == 3)
    val day = parts[0].trim().toInt()
    val month = parts[1].trim().toInt()
    val year = parts[2].trim().toInt()
    LocalDate(year, Month(month), day).atStartOfDayIn(TimeZone.currentSystemDefault())
}.getOrNull()
