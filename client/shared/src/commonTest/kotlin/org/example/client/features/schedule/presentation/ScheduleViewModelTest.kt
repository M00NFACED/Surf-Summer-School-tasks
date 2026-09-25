package org.example.client.features.schedule.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import org.example.client.core.network.NetworkErrorMessage
import org.example.client.features.booking.domain.testSlotItem
import org.example.client.features.schedule.domain.GetScheduleUseCase
import org.example.client.features.schedule.domain.ScheduleFilter
import org.example.client.features.schedule.domain.ScheduleRepository
import org.example.client.features.schedule.domain.ScheduleSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

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

    @Test
    fun keepsVisibleItemsWhileRefreshingInBackground() {
        val gate = CompletableDeferred<Unit>()
        val repository = SwitchableScheduleRepository()
        val viewModel = ScheduleViewModel(
            getSchedule = GetScheduleUseCase(repository::getSchedule),
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
        )

        viewModel.load()
        val loaded = assertIs<ScheduleState.Success>(viewModel.state.value)

        repository.blockNextRequest(gate)
        viewModel.refresh()
        val refreshing = assertIs<ScheduleState.Refreshing>(viewModel.state.value)

        assertEquals(loaded.snapshot.items, refreshing.snapshot.items)
        assertTrue(viewModel.state.value.hasVisibleItems())

        gate.complete(Unit)
        assertIs<ScheduleState.Success>(viewModel.state.value)
        viewModel.close()
    }

    @Test
    fun showsSkeletonOnlyWithoutVisibleItems() {
        val repository = SwitchableScheduleRepository(empty = true)
        val viewModel = ScheduleViewModel(
            getSchedule = GetScheduleUseCase(repository::getSchedule),
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
        )

        viewModel.load()

        assertEquals(ScheduleState.Empty, viewModel.state.value)
        assertTrue(!viewModel.state.value.hasVisibleItems())
        viewModel.close()
    }

    @Test
    fun keepsItemsAndShowsNoticeWhenRefreshFails() {
        val repository = SwitchableScheduleRepository()
        val viewModel = ScheduleViewModel(
            getSchedule = GetScheduleUseCase(repository::getSchedule),
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
        )
        viewModel.load()

        repository.failNextRequest()
        viewModel.refresh()
        val state = assertIs<ScheduleState.Success>(viewModel.state.value)

        assertEquals(1, state.snapshot.items.size)
        assertEquals(NetworkErrorMessage, state.notice)
        viewModel.close()
    }
}

private class SwitchableScheduleRepository(
    private val empty: Boolean = false,
) : ScheduleRepository {
    private var gate: CompletableDeferred<Unit>? = null
    private var failing = false

    fun blockNextRequest(gate: CompletableDeferred<Unit>) {
        this.gate = gate
    }

    fun failNextRequest() {
        failing = true
    }

    override suspend fun getSchedule(filter: ScheduleFilter): Result<ScheduleSnapshot> {
        val from = Instant.parse("2026-09-25T00:00:00Z")
        val snapshot = ScheduleSnapshot(
            from = from,
            to = Instant.parse("2026-10-02T00:00:00Z"),
            items = if (empty) emptyList() else listOf(testSlotItem()),
        )
        gate?.let {
            gate = null
            it.await()
            return Result.success(snapshot)
        }
        if (failing) {
            failing = false
            return Result.failure(error("offline"))
        }
        return Result.success(snapshot)
    }

    override suspend fun clearCache() = Unit
}

private class ThrowingScheduleRepository : ScheduleRepository {
    override suspend fun getSchedule(filter: ScheduleFilter): Result<ScheduleSnapshot> = error("offline")

    override suspend fun clearCache() = Unit
}
