package org.example.client.features.schedule.data

import org.example.client.features.schedule.domain.ScheduleError

class ScheduleApiException(
    statusCode: Int,
    val errorCode: String,
    message: String,
) : ScheduleError(statusCode, message)
