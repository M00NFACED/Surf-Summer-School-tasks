package org.example.client.features.navigation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave
import org.example.client.features.navigation.domain.MainTab

internal val MainTab.title: String
    get() = when (this) {
        MainTab.SCHEDULE -> "Тренировки"
        MainTab.MY_BOOKINGS -> "Мои записи"
        MainTab.PROFILE -> "Профиль"
    }

internal val MainTab.icon: ImageVector
    get() = when (this) {
        MainTab.SCHEDULE -> Icons.Default.DateRange
        MainTab.MY_BOOKINGS -> Icons.AutoMirrored.Filled.List
        MainTab.PROFILE -> Icons.Default.Person
    }

@Composable
fun WaveBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    val colors = MaterialTheme.wave
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .padding(bottom = 16.dp)
            .navigationBarsPadding()
            .height(58.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(colors.pill),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MainTab.values().forEach { tab ->
            val selected = tab == selectedTab
            Row(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onTabSelected(tab) }
                    .semantics { contentDescription = tab.title },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = if (selected) colors.accent else colors.iconMuted,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
