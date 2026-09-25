package org.example.client.features.navigation.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val title: String, val icon: ImageVector) {
    SCHEDULE("Тренировки", Icons.Default.DateRange),
    MY_BOOKINGS("Мои записи", Icons.AutoMirrored.Filled.List),
    PROFILE("Профиль", Icons.Default.Person),
}
