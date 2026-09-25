package org.example.client.features.schedule.presentation

import kotlinx.coroutines.Dispatchers
import org.example.client.core.network.NetworkErrorMessage
import org.example.client.features.schedule.domain.GetScheduleUseCase
import org.example.client.features.schedule.domain.ScheduleFilter
import org.example.client.features.schedule.domain.ScheduleRepository
import org.example.client.features.schedule.domain.ScheduleSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleViewModelTest {
    @Test
    fun mapsThrownNetworkFailureToSafeErrorState() {
        val repository = ThrowingScheduleRepository()
        val viewModel = ScheduleViewModel(
            getSchedule = GetScheduleUseCase(repository::getSchedule),
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
        )

        viewModel.load()

        assertEquals(ScheduleState.Error(NetworkErrorMessage), viewModel.state.value)
        viewModel.close()
    }
}

private class ThrowingScheduleRepository : ScheduleRepository {
    override suspend fun getSchedule(filter: ScheduleFilter): Result<ScheduleSnapshot> = error("offline")

    override suspend fun clearCache() = Unit
}
