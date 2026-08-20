package com.hcdash.app.ui.screens.body_mind

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.hcdash.app.ui.screens.body.BodyCompositionScreen
import com.hcdash.app.ui.screens.body.BodyCompositionViewModel
import com.hcdash.app.ui.screens.stress.StressScreen
import com.hcdash.app.ui.screens.stress.StressViewModel
import com.hcdash.app.ui.theme.*

enum class BodyMindTab(val title: String) {
    BODY("Body & Weight"),
    MIND("Stress & HRV")
}

@Composable
fun BodyMindScreen(
    bodyCompositionViewModel: BodyCompositionViewModel,
    stressViewModel: StressViewModel,
    onNavigateToMetric: (MetricType) -> Unit,
    onOpenSettings: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BodyMindTab.BODY) }

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
            BodyMindTab.values().forEach { tab ->
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
            label = "bodyMindTransition"
        ) { tab ->
            when (tab) {
                BodyMindTab.BODY -> {
                    BodyCompositionScreen(
                        viewModel = bodyCompositionViewModel,
                        onNavigateToMetric = onNavigateToMetric
                    )
                }
                BodyMindTab.MIND -> {
                    StressScreen(
                        viewModel = stressViewModel,
                        onNavigateToMetric = onNavigateToMetric
                    )
                }
            }
        }
    }
}
