package com.hcdash.app.ui.screens.stress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcdash.app.domain.model.MetricType
import com.hcdash.app.domain.utils.FormatUtils
import com.hcdash.app.ui.components.*
import com.hcdash.app.ui.theme.*

@Composable
fun StressScreen(
    viewModel: StressViewModel,
    onNavigateToMetric: (MetricType) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TimeRangeSelector(
                selectedRange = state.selectedRange,
                onRangeSelected = { viewModel.setTimeRange(it) }
            )
        }

        // Stress Gauge Card
        item {
            val stress = state.latestStress?.stressScore ?: 35
            val (category, color) = when {
                stress < 30 -> "Rested" to NeonGreen
                stress < 55 -> "Balanced" to NeonCyan
                stress < 75 -> "Elevated" to NeonAmber
                else -> "High" to NeonRed
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurface)
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GaugeMeter(
                        value = stress.toFloat(),
                        valueText = "$stress",
                        labelText = "STRESS",
                        categoryText = category,
                        accentColor = color
                    )
                }
            }
        }

        // Stress Timeline & HRV RMSSD Chart
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = AppIcons.Brain),
                                contentDescription = null,
                                tint = NeonPink,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Stress & HRV Timeline", style = Typography.titleMedium, color = TextPrimary)
                        }

                        Text(
                            text = "Avg HRV: ${FormatUtils.formatInteger(state.averageHrvRmssd.toLong())} ms",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    MetricLineChart(
                        points = state.stressPoints,
                        lineColor = NeonPink,
                        unit = "pts"
                    )

                    state.stressTrend?.let { trend ->
                        Spacer(modifier = Modifier.height(14.dp))
                        StatSummaryRow(
                            trend = trend,
                            unit = "pts"
                        )
                    }
                }
            }
        }
    }
}
