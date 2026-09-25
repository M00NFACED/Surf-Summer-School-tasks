package org.example.client.features.my_bookings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.client.features.my_bookings.domain.MyBooking

@Composable
fun MyBookingsScreen(
    viewModel: MyBookingsViewModel,
    onBookingClick: (String) -> Unit,
    onOpenSchedule: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val bookings = state.snapshot?.let { if (selectedTab == 0) it.active else it.history }.orEmpty()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Мои записи", style = MaterialTheme.typography.headlineSmall)
        if (state.isOffline) {
            Text(
                "Нет сети — показаны последние загруженные данные",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("Предстоящие") })
            FilterChip(selected = selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("Прошедшие") })
        }
        when {
            state.isLoading && state.snapshot == null -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            state.isForbidden -> Text("Сессия истекла. Войдите снова", color = MaterialTheme.colorScheme.error)
            state.errorMessage != null && state.snapshot == null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::load) { Text("Повторить") }
            }
            state.isEmpty -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Пока нет записей")
                Button(onClick = onOpenSchedule) { Text("Перейти к расписанию") }
            }
            bookings.isEmpty() -> Text(if (selectedTab == 0) "Нет предстоящих записей" else "Нет прошедших записей")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(bookings, key = { it.id }) { booking ->
                    MyBookingCard(booking = booking, onClick = { onBookingClick(booking.id) })
                }
            }
        }
    }
}
