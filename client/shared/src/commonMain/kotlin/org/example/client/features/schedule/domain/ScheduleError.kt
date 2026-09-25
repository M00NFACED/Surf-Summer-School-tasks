package org.example.client.features.schedule.domain

open class ScheduleError(
    val statusCode: Int,
    override val message: String,
) : Exception(message)
