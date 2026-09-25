package org.example.client.features.schedule.presentation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.example.client.core.network.NetworkErrorMessage
import org.example.client.features.schedule.domain.GetScheduleUseCase
import org.example.client.features.schedule.domain.ScheduleError
import org.example.client.features.schedule.domain.Instructor
import org.example.client.features.schedule.domain.ScheduleFilter
import org.example.client.features.schedule.domain.ScheduleRepository
import org.example.client.features.schedule.domain.TrainingFormat

class ScheduleViewModel(
    private val getSchedule: GetScheduleUseCase,
    private val repository: ScheduleRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutableState = MutableStateFlow<ScheduleState>(ScheduleState.Initial)
    private val mutableFilter = MutableStateFlow(ScheduleFilter())
    private val mutableInstructors = MutableStateFlow<List<Instructor>>(emptyList())
    private var requestJob: Job? = null
    private var debounceJob: Job? = null

    val state: StateFlow<ScheduleState> = mutableState.asStateFlow()
    val filter: StateFlow<ScheduleFilter> = mutableFilter.asStateFlow()
    val instructors: StateFlow<List<Instructor>> = mutableInstructors.asStateFlow()

    fun load() = scheduleLoad(debounce = false)

    fun refresh() = scheduleLoad(debounce = false)

    fun setFormat(format: TrainingFormat?) {
        updateFilter(mutableFilter.value.copy(format = format))
    }

    fun setInstructor(instructorId: String?) {
        updateFilter(mutableFilter.value.copy(instructorId = instructorId))
    }

    fun setPeriod(from: Instant, to: Instant) {
        updateFilter(mutableFilter.value.copy(from = from, to = to))
    }

    fun resetFilters() {
        updateFilter(ScheduleFilter())
    }

    suspend fun clearCache() {
        requestJob?.cancel()
        debounceJob?.cancel()
        try {
            repository.clearCache()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            Unit
        } finally {
            mutableState.value = ScheduleState.Initial
            mutableFilter.value = ScheduleFilter()
            mutableInstructors.value = emptyList()
        }
    }

    fun close() {
        requestJob?.cancel()
        debounceJob?.cancel()
        scope.cancel()
    }

    private fun updateFilter(filter: ScheduleFilter) {
        mutableFilter.value = filter
        scheduleLoad(debounce = true)
    }

    private fun scheduleLoad(debounce: Boolean) {
        requestJob?.cancel()
        debounceJob?.cancel()
        if (debounce) {
            debounceJob = scope.launch {
                delay(300)
                fetch()
            }
        } else {
            fetch()
        }
    }

    private fun fetch() {
        requestJob = scope.launch {
            mutableState.value = ScheduleState.Loading
            try {
                getSchedule(mutableFilter.value)
                    .onSuccess { snapshot ->
                        if (snapshot.items.isNotEmpty()) {
                            mutableInstructors.value = (mutableInstructors.value + snapshot.items.map { it.instructor })
                                .distinctBy { it.id }
                        }
                        mutableState.value = when {
                            snapshot.isOffline -> ScheduleState.Offline(snapshot)
                            snapshot.items.isEmpty() -> ScheduleState.Empty
                            else -> ScheduleState.Success(snapshot)
                        }
                    }
                    .onFailure { error -> mutableState.value = error.toScheduleState() }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                mutableState.value = ScheduleState.Error(NetworkErrorMessage)
            }
        }
    }

    private fun Throwable.toScheduleState(): ScheduleState = when (this) {
        is ScheduleError -> if (statusCode == 401) {
            ScheduleState.Forbidden
        } else {
            ScheduleState.Error(message ?: "Не удалось загрузить расписание")
        }
        is IllegalArgumentException -> ScheduleState.Error(message ?: "Проверьте параметры расписания")
        else -> ScheduleState.Error(NetworkErrorMessage)
    }
}
