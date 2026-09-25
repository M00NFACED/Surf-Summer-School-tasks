package org.example.client.features.schedule.presentation

import org.example.client.features.schedule.domain.ScheduleSnapshot

sealed interface ScheduleState {
    data object Initial : ScheduleState
    data object Loading : ScheduleState
    data class Refreshing(val snapshot: ScheduleSnapshot) : ScheduleState
    data class Success(val snapshot: ScheduleSnapshot, val notice: String? = null) : ScheduleState
    data class Offline(val snapshot: ScheduleSnapshot, val notice: String? = null) : ScheduleState
    data object Empty : ScheduleState
    data class Error(val message: String) : ScheduleState
    data object Forbidden : ScheduleState
}

internal val ScheduleState.snapshot: ScheduleSnapshot?
    get() = when (this) {
        is ScheduleState.Refreshing -> snapshot
        is ScheduleState.Success -> snapshot
        is ScheduleState.Offline -> snapshot
        else -> null
    }

internal fun ScheduleState.notice(): String? = when (this) {
    is ScheduleState.Success -> notice
    is ScheduleState.Offline -> notice
    else -> null
}

internal fun ScheduleState.hasVisibleItems(): Boolean = snapshot?.items?.isNotEmpty() == true
