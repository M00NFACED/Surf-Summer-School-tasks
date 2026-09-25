package org.example.client.features.schedule.presentation

import org.example.client.features.schedule.domain.ScheduleSnapshot

sealed interface ScheduleState {
    data object Initial : ScheduleState
    data object Loading : ScheduleState
    data class Success(val snapshot: ScheduleSnapshot) : ScheduleState
    data object Empty : ScheduleState
    data class Error(val message: String) : ScheduleState
    data class Offline(val snapshot: ScheduleSnapshot) : ScheduleState
    data object Forbidden : ScheduleState
}
