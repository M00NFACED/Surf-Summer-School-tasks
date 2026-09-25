package org.example.client.features.schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.features.schedule.domain.ScheduleFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onLogout: () -> Unit,
    onSlotClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val instructors by viewModel.instructors.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Расписание тренировок") },
                actions = { TextButton(onClick = onLogout) { Text("Выйти") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = periodLabel(filter),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            ScheduleFiltersView(
                filter = filter,
                instructors = instructors,
                onFormatSelected = viewModel::setFormat,
                onInstructorSelected = viewModel::filterByInstructor,
                onPeriodSelected = viewModel::setPeriod,
                onReset = viewModel::resetFilters,
            )
            if (state is ScheduleState.Offline) {
                ScheduleOfflineBanner((state as ScheduleState.Offline).snapshot.cachedAtEpochMillis)
            }
            PullToRefreshBox(
                isRefreshing = state is ScheduleState.Loading,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val current = state) {
                    ScheduleState.Initial, ScheduleState.Loading -> ScheduleSkeleton()
                    ScheduleState.Empty -> ScheduleEmptyView(viewModel::resetFilters)
                    is ScheduleState.Error -> ErrorState(current.message, viewModel::refresh)
                    ScheduleState.Forbidden -> ErrorState("Сессия истекла. Войдите снова", onLogout)
                    is ScheduleState.Success -> SlotList(current.snapshot.items, onSlotClick)
                    is ScheduleState.Offline -> if (current.snapshot.items.isEmpty()) {
                        ScheduleEmptyView(viewModel::resetFilters)
                    } else {
                        SlotList(current.snapshot.items, onSlotClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotList(items: List<org.example.client.features.schedule.domain.TrainingSlotItem>, onSlotClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items, key = { it.id }) { item ->
            ScheduleCard(item, onSlotClick)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message)
            Button(onClick = onRetry) { Text("Повторить") }
        }
    }
}

private fun periodLabel(filter: ScheduleFilter): String {
    val from = filter.from
    val to = filter.to
    return if (from != null && to != null) {
        "${formatScheduleDate(from)} — ${formatScheduleDate(to)}"
    } else {
        "Ближайшие 7 дней"
    }
}
