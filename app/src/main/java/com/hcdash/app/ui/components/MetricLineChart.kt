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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcdash.app.domain.model.AggregatedMetricPoint
import com.hcdash.app.domain.utils.FormatUtils
import com.hcdash.app.ui.theme.*

@Composable
fun MetricLineChart(
    points: List<AggregatedMetricPoint>,
    lineColor: Color,
    unit: String = "",
    modifier: Modifier = Modifier,
    showAverageLine: Boolean = true,
    showMinMaxMarkers: Boolean = true
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
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "lineProgress"
    )

    val safePoints = points.map {
        if (it.value.isNaN() || it.value.isInfinite()) it.copy(value = 0.0) else it
    }
    val values = safePoints.map { it.value }
    val minValue = values.minOrNull() ?: 0.0
    val maxValue = values.maxOrNull() ?: 1.0
    val avgValue = if (values.isNotEmpty()) values.average() else 0.0

    // Add padding to range so line doesn't hit exact top/bottom
    val rangeDelta = if (maxValue == minValue) (if (maxValue == 0.0) 1.0 else maxValue * 0.2) else maxValue - minValue
    val chartMin = minValue - (rangeDelta * 0.1)
    val chartMax = maxValue + (rangeDelta * 0.1)
    val totalRange = (chartMax - chartMin).coerceAtLeast(0.0001)

    Column(modifier = modifier.fillMaxWidth()) {
        // Selected scrubber value header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            selectedIndex?.let { idx ->
                if (idx in safePoints.indices) {
                    val point = safePoints[idx]
                    val formattedVal = if (unit == "steps" || unit == "kcal" || unit == "bpm" || unit == "pts") {
                        FormatUtils.formatInteger(point.value.toLong())
                    } else {
                        FormatUtils.formatDecimal1(point.value)
                    }
                    Text(
                        text = "${point.label}: $formattedVal $unit",
                        color = lineColor,
                        fontSize = 13.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }
            } ?: run {
                val formattedAvg = if (unit == "steps" || unit == "kcal" || unit == "bpm" || unit == "pts") {
                    FormatUtils.formatInteger(avgValue.toLong())
                } else {
                    FormatUtils.formatDecimal1(avgValue)
                }
                Text(
                    text = "Avg: $formattedAvg $unit",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            val formattedMin = if (unit == "steps" || unit == "kcal" || unit == "bpm" || unit == "pts") {
                FormatUtils.formatInteger(minValue.toLong())
            } else {
                FormatUtils.formatDecimal1(minValue)
            }
            val formattedMax = if (unit == "steps" || unit == "kcal" || unit == "bpm" || unit == "pts") {
                FormatUtils.formatInteger(maxValue.toLong())
            } else {
                FormatUtils.formatDecimal1(maxValue)
            }

            Text(
                text = "Min: $formattedMin · Max: $formattedMax $unit",
                color = TextTertiary,
                fontSize = 11.sp
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(safePoints) {
                    detectTapGestures(
                        onPress = { offset ->
                            val sectionWidth = size.width / (safePoints.size - 1).coerceAtLeast(1)
                            val index = ((offset.x + sectionWidth / 2) / sectionWidth).toInt()
                                .coerceIn(0, safePoints.size - 1)
                            selectedIndex = index
                        },
                        onTap = { selectedIndex = null }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val n = safePoints.size

            // 1. Draw horizontal grid lines (3 lines)
            val gridColor = DarkBorder.copy(alpha = 0.4f)
            for (i in 0..2) {
                val y = height * (i / 2f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Draw dashed average line
            if (showAverageLine) {
                val avgY = (height - ((avgValue - chartMin) / totalRange * height)).toFloat()
                if (!avgY.isNaN() && !avgY.isInfinite()) {
                    drawLine(
                        color = TextTertiary.copy(alpha = 0.5f),
                        start = Offset(0f, avgY),
                        end = Offset(width * animatedProgress, avgY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }

            if (n == 1) {
                // Single point
                val y = height / 2f
                drawCircle(color = lineColor, radius = 6.dp.toPx() * animatedProgress, center = Offset(width / 2f, y))
                return@Canvas
            }

            val stepX = width / (n - 1).coerceAtLeast(1)
            val pointsCoordinates = safePoints.mapIndexed { index, point ->
                val x = index * stepX
                val targetY = (height - ((point.value - chartMin) / totalRange * height)).toFloat()
                val currentY = height - ((height - targetY) * animatedProgress)
                val safeY = if (currentY.isNaN() || currentY.isInfinite()) height / 2f else currentY.coerceIn(0f, height)
                Offset(x, safeY)
            }

            // Build smooth cubic Bézier path
            val linePath = Path()
            val fillPath = Path()

            linePath.moveTo(pointsCoordinates.first().x, pointsCoordinates.first().y)
            fillPath.moveTo(pointsCoordinates.first().x, height)
            fillPath.lineTo(pointsCoordinates.first().x, pointsCoordinates.first().y)

            for (i in 0 until pointsCoordinates.size - 1) {
                val p0 = pointsCoordinates[i]
                val p1 = pointsCoordinates[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2
                val controlY2 = p1.y

                linePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }

            fillPath.lineTo(pointsCoordinates.last().x, height)
            fillPath.close()

            // 3. Draw gradient area fill
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.35f * animatedProgress),
                    lineColor.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = height
            )
            drawPath(path = fillPath, brush = gradient)

            // 4. Draw smooth stroke line
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 5. Draw min / max glow markers
            if (showMinMaxMarkers && animatedProgress > 0.8f) {
                val minIdx = values.indexOf(minValue)
                val maxIdx = values.indexOf(maxValue)

                if (minIdx in pointsCoordinates.indices) {
                    val minCoord = pointsCoordinates[minIdx]
                    drawCircle(color = lineColor.copy(alpha = 0.25f), radius = 7.dp.toPx(), center = minCoord)
                    drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = minCoord)
                }

                if (maxIdx in pointsCoordinates.indices && maxIdx != minIdx) {
                    val maxCoord = pointsCoordinates[maxIdx]
                    drawCircle(color = lineColor.copy(alpha = 0.25f), radius = 7.dp.toPx(), center = maxCoord)
                    drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = maxCoord)
                }
            }

            // 6. Draw scrubber selection vertical indicator if tapped
            selectedIndex?.let { idx ->
                if (idx in pointsCoordinates.indices) {
                    val coord = pointsCoordinates[idx]
                    drawLine(
                        color = lineColor.copy(alpha = 0.7f),
                        start = Offset(coord.x, 0f),
                        end = Offset(coord.x, height),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    drawCircle(color = DarkBackground, radius = 6.dp.toPx(), center = coord)
                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = coord)
                }
            }
        }

        // Bottom label tags (first, mid, last)
        if (safePoints.size >= 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = safePoints.first().label, color = TextTertiary, fontSize = 10.sp)
                if (safePoints.size > 2) {
                    Text(text = safePoints[safePoints.size / 2].label, color = TextTertiary, fontSize = 10.sp)
                }
                Text(text = safePoints.last().label, color = TextTertiary, fontSize = 10.sp)
            }
        }
    }
}
