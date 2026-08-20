package com.hcdash.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcdash.app.domain.model.TrendAnalysis
import com.hcdash.app.domain.model.TrendDirection
import com.hcdash.app.domain.utils.FormatUtils
import com.hcdash.app.ui.theme.*

@Composable
fun StatSummaryRow(
    trend: TrendAnalysis,
    unit: String,
    modifier: Modifier = Modifier
) {
    val isIntegerMetric = unit == "steps" || unit == "kcal" || unit == "bpm" || unit == "pts"

    val avgStr = if (isIntegerMetric) FormatUtils.formatInteger(trend.currentAverage.toLong()) else FormatUtils.formatDecimal1(trend.currentAverage)
    val maxStr = if (isIntegerMetric) FormatUtils.formatInteger(trend.max.toLong()) else FormatUtils.formatDecimal1(trend.max)
    val minStr = if (isIntegerMetric) FormatUtils.formatInteger(trend.min.toLong()) else FormatUtils.formatDecimal1(trend.min)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            label = "Average",
            value = avgStr,
            unit = unit,
            accentColor = NeonCyan,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "High",
            value = maxStr,
            unit = unit,
            accentColor = NeonOrange,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Low",
            value = minStr,
            unit = unit,
            accentColor = NeonGreen,
            modifier = Modifier.weight(1f)
        )
        TrendDeltaCard(
            trend = trend,
            modifier = Modifier.weight(1.1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder.copy(alpha = 0.5f), shape)
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = label, color = TextTertiary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = unit,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendDeltaCard(
    trend: TrendAnalysis,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val (iconRes, color) = when (trend.direction) {
        TrendDirection.RISING -> AppIcons.TrendingUp to NeonGreen
        TrendDirection.FALLING -> AppIcons.TrendingDown to NeonRed
        TrendDirection.STEADY -> AppIcons.TrendingFlat to TextSecondary
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(DarkSurfaceVariant)
            .border(1.dp, color.copy(alpha = 0.3f), shape)
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = "Trend Delta", color = TextTertiary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = FormatUtils.formatPercentChange(trend.percentageChange),
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
