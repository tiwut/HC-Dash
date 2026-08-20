package com.hcdash.app.ui.screens.activity

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
fun ActivityScreen(
    viewModel: ActivityViewModel,
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

        // Steps Section
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
                                painter = painterResource(id = AppIcons.Footprints),
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Daily Steps", style = Typography.titleMedium, color = TextPrimary)
                        }

                        Text(
                            text = "${FormatUtils.formatInteger(state.totalStepsInPeriod)} steps",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    MetricBarChart(
                        points = state.stepPoints,
                        barColor = NeonGreen,
                        unit = "steps",
                        targetValue = 10000.0
                    )

                    state.stepTrend?.let { trend ->
                        Spacer(modifier = Modifier.height(14.dp))
                        StatSummaryRow(
                            trend = trend,
                            unit = "steps"
                        )
                    }
                }
            }
        }

        // Calories Section
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
                                painter = painterResource(id = AppIcons.Flame),
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Active Burn", style = Typography.titleMedium, color = TextPrimary)
                        }

                        Text(
                            text = "${FormatUtils.formatInteger(state.totalActiveCalories.toLong())} kcal",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    MetricLineChart(
                        points = state.caloriePoints,
                        lineColor = NeonOrange,
                        unit = "kcal"
                    )

                    state.calorieTrend?.let { trend ->
                        Spacer(modifier = Modifier.height(14.dp))
                        StatSummaryRow(
                            trend = trend,
                            unit = "kcal"
                        )
                    }
                }
            }
        }
    }
}
