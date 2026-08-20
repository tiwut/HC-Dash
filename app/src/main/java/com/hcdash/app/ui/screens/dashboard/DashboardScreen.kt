package com.hcdash.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.hcdash.app.ui.components.AppIcons
import com.hcdash.app.ui.components.GaugeMeter
import com.hcdash.app.ui.components.HealthCard
import com.hcdash.app.ui.components.SyncStatusIndicator
import com.hcdash.app.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToMetric: (MetricType) -> Unit,
    onNavigateToLayer: (String) -> Unit
) {
    val summary by viewModel.healthSummary.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Wellness Score Card with Gauge
        item {
            val scoreCategory = when {
                summary.overallHealthScore >= 85 -> "Optimal"
                summary.overallHealthScore >= 70 -> "Good"
                summary.overallHealthScore >= 55 -> "Fair"
                else -> "Attention"
            }
            val scoreColor = when {
                summary.overallHealthScore >= 85 -> NeonGreen
                summary.overallHealthScore >= 70 -> NeonCyan
                summary.overallHealthScore >= 55 -> NeonAmber
                else -> NeonRed
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
                        value = summary.overallHealthScore.toFloat(),
                        valueText = "${summary.overallHealthScore}",
                        labelText = "WELLNESS",
                        categoryText = scoreCategory,
                        accentColor = scoreColor
                    )
                }
            }
        }

        // Quick Fail-Safe Banner if no records yet
        if (summary.todaySteps == 0L && summary.currentHeartRate == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = AppIcons.Info),
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "No records yet", color = TextSecondary, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { viewModel.manualRefresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Activity & Energy
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthCard(
                    title = "Steps",
                    value = FormatUtils.formatInteger(summary.todaySteps),
                    unit = "",
                    iconRes = AppIcons.Footprints,
                    accentColor = NeonGreen,
                    modifier = Modifier.weight(1f),
                    subtitle = "Goal: 10k",
                    trendText = if (summary.todaySteps >= 10000) "Goal reached" else "${((summary.todaySteps / 10000.0) * 100).toInt()}%",
                    isPositiveTrend = summary.todaySteps >= 8000,
                    onClick = { onNavigateToMetric(MetricType.STEPS) }
                )

                HealthCard(
                    title = "Active Burn",
                    value = FormatUtils.formatInteger(summary.todayCalories.toLong()),
                    unit = "kcal",
                    iconRes = AppIcons.Flame,
                    accentColor = NeonOrange,
                    modifier = Modifier.weight(1f),
                    subtitle = "Active energy",
                    trendText = "+12%",
                    isPositiveTrend = true,
                    onClick = { onNavigateToMetric(MetricType.CALORIES) }
                )
            }
        }

        // Section: Vitals & Heart
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthCard(
                    title = "Pulse",
                    value = FormatUtils.formatInteger(summary.currentHeartRate?.toLong()),
                    unit = "bpm",
                    iconRes = AppIcons.HeartPulse,
                    accentColor = NeonRed,
                    modifier = Modifier.weight(1f),
                    subtitle = "RHR: ${FormatUtils.formatInteger(summary.restingHeartRate?.toLong())} bpm",
                    trendText = "Normal",
                    isPositiveTrend = true,
                    onClick = { onNavigateToMetric(MetricType.HEART_RATE) }
                )

                HealthCard(
                    title = "SpO2",
                    value = FormatUtils.formatDecimal1(summary.oxygenSaturation),
                    unit = "%",
                    iconRes = AppIcons.Droplet,
                    accentColor = NeonBlue,
                    modifier = Modifier.weight(1f),
                    subtitle = "Oxygen",
                    trendText = if ((summary.oxygenSaturation ?: 98.0) >= 95.0) "Optimal" else "Low",
                    isPositiveTrend = (summary.oxygenSaturation ?: 98.0) >= 95.0,
                    onClick = { onNavigateToMetric(MetricType.OXYGEN_SATURATION) }
                )
            }
        }

        // Section: Sleep & Stress
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val sleepText = FormatUtils.formatDuration(summary.latestSleepDurationMinutes)
                val sleepScoreText = summary.latestSleepQualityScore?.let { "Score: $it/100" } ?: "Night"

                HealthCard(
                    title = "Sleep",
                    value = sleepText,
                    unit = "",
                    iconRes = AppIcons.Moon,
                    accentColor = NeonPurple,
                    modifier = Modifier.weight(1f),
                    subtitle = sleepScoreText,
                    trendText = if ((summary.latestSleepDurationMinutes ?: 0L) >= 420) "Good" else "Short",
                    isPositiveTrend = (summary.latestSleepDurationMinutes ?: 0L) >= 420,
                    onClick = { onNavigateToMetric(MetricType.SLEEP) }
                )

                HealthCard(
                    title = "Stress",
                    value = FormatUtils.formatInteger(summary.latestStressScore),
                    unit = "/100",
                    iconRes = AppIcons.Brain,
                    accentColor = NeonPink,
                    modifier = Modifier.weight(1f),
                    subtitle = "HRV: ${FormatUtils.formatInteger(summary.latestHrvRmssd?.toLong())} ms",
                    trendText = if ((summary.latestStressScore ?: 30) < 50) "Rested" else "Load",
                    isPositiveTrend = (summary.latestStressScore ?: 30) < 50,
                    onClick = { onNavigateToMetric(MetricType.STRESS_HRV) }
                )
            }
        }

        // Section: Body & Weight
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthCard(
                    title = "Weight",
                    value = FormatUtils.formatDecimal1(summary.latestWeight),
                    unit = "kg",
                    iconRes = AppIcons.Scale,
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f),
                    subtitle = "BMI: ${FormatUtils.formatDecimal1(summary.latestBmi)}",
                    trendText = "-0.4 kg",
                    isPositiveTrend = true,
                    onClick = { onNavigateToMetric(MetricType.WEIGHT) }
                )

                HealthCard(
                    title = "Body Fat",
                    value = FormatUtils.formatDecimal1(summary.latestBodyFat),
                    unit = "%",
                    iconRes = AppIcons.Biceps,
                    accentColor = NeonAmber,
                    modifier = Modifier.weight(1f),
                    subtitle = "Muscle: ${FormatUtils.formatDecimal1(summary.latestMuscleMass)} kg",
                    trendText = "${FormatUtils.formatDecimal1(summary.latestBodyWater)}% H2O",
                    isPositiveTrend = true,
                    onClick = { onNavigateToMetric(MetricType.BODY_FAT) }
                )
            }
        }
    }
}
