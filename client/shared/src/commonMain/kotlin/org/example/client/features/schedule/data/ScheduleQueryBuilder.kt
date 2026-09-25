package org.example.client.features.schedule.data

import org.example.client.features.schedule.domain.ScheduleFilter

object ScheduleQueryBuilder {
    fun build(filter: ScheduleFilter): List<Pair<String, String>> = buildList {
        filter.from?.let { add("from" to it.toString()) }
        filter.to?.let { add("to" to it.toString()) }
        filter.format?.let { add("format" to it.apiValue) }
        filter.instructorId?.let { add("instructor_id" to it) }
    }
}
