package com.hcdash.app.ui.screens.vitals

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
fun VitalsScreen(
    viewModel: VitalsViewModel,
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

        // Heart Rate Section
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
                                painter = painterResource(id = AppIcons.HeartPulse),
                                contentDescription = null,
                                tint = NeonRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Heart Rate", style = Typography.titleMedium, color = TextPrimary)
                        }

                        Text(
                            text = "RHR: ${FormatUtils.formatInteger(state.restingHr?.toLong())} bpm",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonRed
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    MetricLineChart(
                        points = state.heartRatePoints,
                        lineColor = NeonRed,
                        unit = "bpm"
                    )

                    state.heartRateTrend?.let { trend ->
                        Spacer(modifier = Modifier.height(14.dp))
                        StatSummaryRow(
                            trend = trend,
                            unit = "bpm"
                        )
                    }
                }
            }
        }

        // Blood Oxygen Section
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
                                painter = painterResource(id = AppIcons.Droplet),
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Blood Oxygen (SpO2)", style = Typography.titleMedium, color = TextPrimary)
                        }

                        Text(
                            text = "${FormatUtils.formatDecimal1(state.currentOxygen)}%",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    MetricLineChart(
                        points = state.oxygenPoints,
                        lineColor = NeonBlue,
                        unit = "%"
                    )

                    state.oxygenTrend?.let { trend ->
                        Spacer(modifier = Modifier.height(14.dp))
                        StatSummaryRow(
                            trend = trend,
                            unit = "%"
                        )
                    }
                }
            }
        }
    }
}
