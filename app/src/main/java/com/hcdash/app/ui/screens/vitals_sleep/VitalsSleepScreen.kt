package com.hcdash.app.ui.screens.vitals_sleep

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcdash.app.domain.model.MetricType
import com.hcdash.app.ui.screens.sleep.SleepScreen
import com.hcdash.app.ui.screens.sleep.SleepViewModel
import com.hcdash.app.ui.screens.vitals.VitalsScreen
import com.hcdash.app.ui.screens.vitals.VitalsViewModel
import com.hcdash.app.ui.theme.*

enum class VitalsSleepTab(val title: String) {
    VITALS("Vitals & Heart"),
    SLEEP("Sleep & Recovery")
}

@Composable
fun VitalsSleepScreen(
    vitalsViewModel: VitalsViewModel,
    sleepViewModel: SleepViewModel,
    onNavigateToMetric: (MetricType) -> Unit,
    onOpenSettings: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(VitalsSleepTab.VITALS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Segmented Sub-Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .padding(4.dp)
        ) {
            VitalsSleepTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val bg = if (isSelected) DarkSurface else Color.Transparent
                val textCol = if (isSelected) NeonCyan else TextSecondary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title,
                        color = textCol,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "vitalsSleepTransition"
        ) { tab ->
            when (tab) {
                VitalsSleepTab.VITALS -> {
                    VitalsScreen(
                        viewModel = vitalsViewModel,
                        onNavigateToMetric = onNavigateToMetric
                    )
                }
                VitalsSleepTab.SLEEP -> {
                    SleepScreen(
                        viewModel = sleepViewModel,
                        onNavigateToMetric = onNavigateToMetric
                    )
                }
            }
        }
    }
}
