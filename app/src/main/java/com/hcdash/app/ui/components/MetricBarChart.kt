package com.hcdash.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcdash.app.domain.model.AggregatedMetricPoint
import com.hcdash.app.domain.utils.FormatUtils
import com.hcdash.app.ui.theme.*

@Composable
fun MetricBarChart(
    points: List<AggregatedMetricPoint>,
    barColor: Color,
    unit: String = "",
    targetValue: Double? = null,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(text = "No data recorded for this period", color = TextTertiary, fontSize = 13.sp)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Smooth animation trigger when points change
    var animationTrigger by remember(points) { mutableFloatStateOf(0f) }
    LaunchedEffect(points) {
        animationTrigger = 1f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = animationTrigger,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "barProgress"
    )

    val safePoints = points.map {
        if (it.value.isNaN() || it.value.isInfinite()) it.copy(value = 0.0) else it
    }
    val values = safePoints.map { it.value }
    val maxBarValue = (values.maxOrNull() ?: 1.0).coerceAtLeast(targetValue ?: 1.0).coerceAtLeast(1.0) * 1.15

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            selectedIndex?.let { idx ->
                if (idx in safePoints.indices) {
                    val p = safePoints[idx]
                    val formattedVal = if (unit == "steps" || unit == "kcal") {
                        FormatUtils.formatInteger(p.value.toLong())
                    } else {
                        FormatUtils.formatDecimal1(p.value)
                    }
                    Text(
                        text = "${p.label}: $formattedVal $unit",
                        color = barColor,
                        fontSize = 13.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }
            } ?: run {
                val total = values.sum()
                val avg = if (values.isNotEmpty()) values.average() else 0.0
                val totalStr = if (unit == "steps" || unit == "kcal") FormatUtils.formatInteger(total.toLong()) else FormatUtils.formatDecimal1(total)
                val avgStr = if (unit == "steps" || unit == "kcal") FormatUtils.formatInteger(avg.toLong()) else FormatUtils.formatDecimal1(avg)

                Text(
                    text = "Total: $totalStr $unit · Avg: $avgStr / day",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            if (targetValue != null && targetValue > 0) {
                val targetStr = if (unit == "steps" || unit == "kcal") FormatUtils.formatInteger(targetValue.toLong()) else FormatUtils.formatDecimal1(targetValue)
                Text(
                    text = "Goal: $targetStr $unit",
                    color = NeonAmber,
                    fontSize = 11.sp
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(safePoints) {
                    detectTapGestures(
                        onPress = { offset ->
                            val barSlotWidth = size.width / safePoints.size.coerceAtLeast(1)
                            val idx = (offset.x / barSlotWidth).toInt().coerceIn(0, safePoints.size - 1)
                            selectedIndex = idx
                        },
                        onTap = { selectedIndex = null }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val count = safePoints.size
            val slotWidth = width / count.coerceAtLeast(1)
            val barWidth = (slotWidth * 0.65f).coerceAtMost(28.dp.toPx()).coerceAtLeast(2.dp.toPx())

            // 1. Draw target goal dashed line
            if (targetValue != null && targetValue > 0) {
                val targetY = (height - ((targetValue / maxBarValue) * height)).toFloat()
                if (!targetY.isNaN() && !targetY.isInfinite()) {
                    drawLine(
                        color = NeonAmber.copy(alpha = 0.6f),
                        start = Offset(0f, targetY),
                        end = Offset(width, targetY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                }
            }

            // 2. Draw bars with animated height
            safePoints.forEachIndexed { index, point ->
                val ratio = (point.value / maxBarValue).coerceIn(0.0, 1.0)
                val fullBarHeight = (ratio * height).toFloat()
                val barHeight = (fullBarHeight * animatedProgress).coerceAtLeast(4.dp.toPx())
                val x = index * slotWidth + (slotWidth - barWidth) / 2f
                val y = height - barHeight

                val isSelected = selectedIndex == index
                val baseColor = if (isSelected) barColor else barColor.copy(alpha = 0.75f)

                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        baseColor,
                        baseColor.copy(alpha = 0.25f)
                    ),
                    startY = y,
                    endY = height
                )

                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                if (isSelected) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.8f),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, 3.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }

        // Bottom labels
        if (safePoints.size in 2..10) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                safePoints.forEach { p ->
                    Text(
                        text = p.label.takeLast(2),
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
        } else if (safePoints.size > 10) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = safePoints.first().label, color = TextTertiary, fontSize = 10.sp)
                Text(text = safePoints[safePoints.size / 2].label, color = TextTertiary, fontSize = 10.sp)
                Text(text = safePoints.last().label, color = TextTertiary, fontSize = 10.sp)
            }
        }
    }
}
