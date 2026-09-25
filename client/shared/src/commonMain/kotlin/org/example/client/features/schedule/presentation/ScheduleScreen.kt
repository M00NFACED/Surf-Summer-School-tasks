package org.example.client.features.schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.core.ui.WaveIconButton
import org.example.client.core.ui.WaveTopBar
import org.example.client.features.schedule.domain.TrainingSlotItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onSlotClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val instructors by viewModel.instructors.collectAsState()
    var sheetVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize()) {
        WaveTopBar(
            title = "Тренировки",
            action = { WaveIconButton(Icons.AutoMirrored.Filled.List, "Фильтры") { sheetVisible = true } },
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
                ScheduleState.Forbidden -> ErrorState("Сессия истекла. Войдите снова", viewModel::refresh)
                is ScheduleState.Success -> SlotList(current.snapshot.items, onSlotClick)
                is ScheduleState.Offline -> if (current.snapshot.items.isEmpty()) {
                    ScheduleEmptyView(viewModel::resetFilters)
                } else {
                    SlotList(current.snapshot.items, onSlotClick)
                }
            }
        }
    }

    if (sheetVisible) {
        ScheduleFilterSheet(
            applied = filter,
            instructors = instructors,
            onDismiss = { sheetVisible = false },
            onApply = { updated ->
                viewModel.applyFilter(updated)
                sheetVisible = false
            },
        )
    }
}

@Composable
private fun SlotList(items: List<TrainingSlotItem>, onSlotClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
            Text(message, color = MaterialTheme.wave.textPrimary)
            TextButton(onClick = onRetry) { Text("Повторить", color = MaterialTheme.wave.accent) }
        }
    }
}
