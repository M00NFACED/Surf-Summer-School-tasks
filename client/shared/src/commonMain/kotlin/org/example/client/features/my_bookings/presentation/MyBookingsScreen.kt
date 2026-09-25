package org.example.client.features.my_bookings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import org.example.client.core.ui.WaveNoticeBar
import org.example.client.core.ui.WaveOutlineButton
import org.example.client.core.ui.WaveSegmentedToggle
import org.example.client.core.ui.WaveTopBar
import org.example.client.features.review.domain.isReviewable

private enum class BookingsTab(val title: String) {
    UPCOMING("Предстоящие"),
    PAST("Прошедшие"),
}

@Composable
fun MyBookingsScreen(
    viewModel: MyBookingsViewModel,
    onBookingClick: (String) -> Unit,
    onOpenSchedule: () -> Unit,
    onReviewClick: (String) -> Unit = {},
    feedback: String? = null,
) {
    val state by viewModel.state.collectAsState()
    var tab by rememberSaveable { mutableStateOf(BookingsTab.UPCOMING) }
    val bookings = state.snapshot?.let { if (tab == BookingsTab.UPCOMING) it.active else it.history }.orEmpty()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize()) {
        WaveTopBar(title = "Мои записи")
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isOffline) {
                Text(
                    text = "Нет сети — показаны последние загруженные данные",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.wave.textSecondary,
                )
            }
            feedback?.let { WaveNoticeBar(it) }
            WaveSegmentedToggle(
                options = BookingsTab.values().toList(),
                selected = tab,
                label = { it.title },
                onSelect = { tab = it },
            )
            when {
                state.isLoading && state.snapshot == null -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                state.isForbidden -> Text("Сессия истекла. Войдите снова", color = MaterialTheme.colorScheme.error)
                state.errorMessage != null && state.snapshot == null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    WaveOutlineButton(text = "Повторить", onClick = viewModel::load)
                }
                state.isEmpty -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Пока нет записей", color = MaterialTheme.wave.textPrimary)
                    WaveOutlineButton(text = "Перейти к расписанию", onClick = onOpenSchedule)
                }
                bookings.isEmpty() -> Text(
                    text = if (tab == BookingsTab.UPCOMING) "Нет предстоящих записей" else "Нет прошедших записей",
                    color = MaterialTheme.wave.textSecondary,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(bookings, key = { it.id }) { booking ->
                        MyBookingCard(
                            booking = booking,
                            onClick = onBookingClick,
                            onReviewClick = if (tab == BookingsTab.PAST && booking.isReviewable()) {
                                { onReviewClick(booking.id) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }
}
