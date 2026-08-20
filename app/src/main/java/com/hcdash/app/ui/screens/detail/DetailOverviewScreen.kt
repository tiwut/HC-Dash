package com.hcdash.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcdash.app.domain.model.MetricType
import com.hcdash.app.domain.utils.FormatUtils
import com.hcdash.app.ui.components.*
import com.hcdash.app.ui.theme.*

@Composable
fun DetailOverviewScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val metricColor = Color(state.metricType.primaryColor)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Bar & Back Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            painter = painterResource(id = AppIcons.ChevronLeft),
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = AppIcons.forMetric(state.metricType)),
                            contentDescription = null,
                            tint = metricColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.metricType.title,
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                SyncStatusIndicator(
                    syncState = syncState,
                    onManualSync = { viewModel.manualRefresh() }
                )
            }
        }

        // Time Range Selector
        item {
            TimeRangeSelector(
                selectedRange = state.selectedRange,
                onRangeSelected = { viewModel.setTimeRange(it) }
            )
        }

        // Interactive Visualizer Chart
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
                        Text(
                            text = "Trend Distribution",
                            style = Typography.titleMedium,
                            color = TextPrimary
                        )
                        state.latestValue?.let {
                            val formattedLatest = FormatUtils.formatMetricValue(state.metricType, it)
                            Text(
                                text = "Latest: $formattedLatest ${state.metricType.unit}",
                                style = Typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = metricColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (state.isBarChart) {
                        MetricBarChart(
                            points = state.points,
                            barColor = metricColor,
                            unit = state.metricType.unit
                        )
                    } else {
                        MetricLineChart(
                            points = state.points,
                            lineColor = metricColor,
                            unit = state.metricType.unit
                        )
                    }

                    state.trend?.let { trend ->
                        Spacer(modifier = Modifier.height(14.dp))
                        StatSummaryRow(
                            trend = trend,
                            unit = state.metricType.unit
                        )
                    }
                }
            }
        }

        // Statistical Deep-Dive Card
        state.trend?.let { trend ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = AppIcons.Info),
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Estimated Statistics & Insights",
                                style = Typography.titleMedium,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = trend.trendSummary,
                            style = Typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val formattedStdDev = FormatUtils.formatDecimal1(trend.standardDeviation)
                        val formattedPrevAvg = FormatUtils.formatMetricValue(state.metricType, trend.previousAverage)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Std Dev: $formattedStdDev ${state.metricType.unit}",
                                style = Typography.labelSmall,
                                color = TextTertiary
                            )
                            Text(
                                text = "Prev Avg: $formattedPrevAvg ${state.metricType.unit}",
                                style = Typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }
        }

        // Historical Data Logs Header
        item {
            Text(
                text = "Logs (${state.points.size} entries)",
                style = Typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Historical Data Logs
        items(state.points.reversed()) { point ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = point.label,
                        style = Typography.bodyMedium,
                        color = TextSecondary
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        val formattedVal = FormatUtils.formatMetricValue(state.metricType, point.value)
                        Text(
                            text = formattedVal,
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.metricType.unit,
                            style = Typography.labelSmall,
                            color = TextTertiary,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
