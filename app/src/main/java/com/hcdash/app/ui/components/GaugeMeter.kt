package com.hcdash.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcdash.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GaugeMeter(
    value: Float, // 0f to 100f
    valueText: String,
    labelText: String,
    categoryText: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val normalizedValue = (value / 100f).coerceIn(0f, 1f)

    val animatedSweepRatio by animateFloatAsState(
        targetValue = normalizedValue,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "gaugeSweep"
    )

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = size.width - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Track background arc (from 135 deg for 270 deg sweep)
            drawArc(
                color = Color(0xFF1E2435),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active gradient arc
            val activeSweep = 270f * animatedSweepRatio
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    NeonCyan,
                    NeonGreen,
                    NeonAmber,
                    NeonRed
                )
            )

            drawArc(
                brush = gradient,
                startAngle = 135f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Needle indicator dot
            val angleRad = Math.toRadians((135f + activeSweep).toDouble())
            val radius = arcSize / 2f
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            val dotX = centerX + radius * cos(angleRad).toFloat()
            val dotY = centerY + radius * sin(angleRad).toFloat()

            drawCircle(color = DarkBackground, radius = 8.dp.toPx(), center = Offset(dotX, dotY))
            drawCircle(color = accentColor, radius = 5.dp.toPx(), center = Offset(dotX, dotY))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = valueText,
                style = Typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = labelText,
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = categoryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
        }
    }
}
