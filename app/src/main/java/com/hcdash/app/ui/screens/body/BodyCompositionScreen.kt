package com.hcdash.app.ui.screens.body

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
fun BodyCompositionScreen(
    viewModel: BodyCompositionViewModel,
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

        // Weight Trend Card
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
                                painter = painterResource(id = AppIcons.Scale),
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Weight & BMI", style = Typography.titleMedium, color = TextPrimary)
                        }

                        Text(
                            text = "${FormatUtils.formatDecimal1(state.latestWeight?.weightKg)} kg · BMI ${FormatUtils.formatDecimal1(state.latestWeight?.bmi)}",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    MetricLineChart(
                        points = state.weightPoints,
                        lineColor = NeonCyan,
                        unit = "kg"
                    )

                    state.weightTrend?.let { trend ->
                        Spacer(modifier = Modifier.height(14.dp))
                        StatSummaryRow(
                            trend = trend,
                            unit = "kg"
                        )
                    }
                }
            }
        }

        // Body Fat % Trend Card
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
                                painter = painterResource(id = AppIcons.Biceps),
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Body Fat", style = Typography.titleMedium, color = TextPrimary)
                        }

                        Text(
                            text = "${FormatUtils.formatDecimal1(state.latestBody?.bodyFatPercentage)}%",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    MetricLineChart(
                        points = state.fatPoints,
                        lineColor = NeonAmber,
                        unit = "%"
                    )

                    state.fatTrend?.let { trend ->
                        Spacer(modifier = Modifier.height(14.dp))
                        StatSummaryRow(
                            trend = trend,
                            unit = "%"
                        )
                    }
                }
            }
        }

        // Muscle & Hydration Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurface)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = AppIcons.Biceps),
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Muscle", style = Typography.bodySmall, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${FormatUtils.formatDecimal1(state.latestBody?.muscleMassKg)} kg",
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurface)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = AppIcons.Droplet),
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Hydration", style = Typography.bodySmall, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${FormatUtils.formatDecimal1(state.latestBody?.bodyWaterPercentage)}%",
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonBlue
                        )
                    }
                }
            }
        }
    }
}
