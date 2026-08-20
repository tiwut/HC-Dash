package com.hcdash.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcdash.app.domain.utils.FormatUtils
import com.hcdash.app.ui.theme.*

@Composable
fun SleepHypnogram(
    deepMinutes: Long,
    remMinutes: Long,
    lightMinutes: Long,
    awakeMinutes: Long,
    modifier: Modifier = Modifier
) {
    val totalMinutes = (deepMinutes + remMinutes + lightMinutes + awakeMinutes).coerceAtLeast(1L)

    val deepTarget = (deepMinutes.toFloat() / totalMinutes).coerceAtLeast(0.01f)
    val remTarget = (remMinutes.toFloat() / totalMinutes).coerceAtLeast(0.01f)
    val lightTarget = (lightMinutes.toFloat() / totalMinutes).coerceAtLeast(0.01f)
    val awakeTarget = (awakeMinutes.toFloat() / totalMinutes).coerceAtLeast(0.01f)

    val animDeep by animateFloatAsState(targetValue = deepTarget, animationSpec = tween(700, easing = FastOutSlowInEasing), label = "deep")
    val animRem by animateFloatAsState(targetValue = remTarget, animationSpec = tween(700, easing = FastOutSlowInEasing), label = "rem")
    val animLight by animateFloatAsState(targetValue = lightTarget, animationSpec = tween(700, easing = FastOutSlowInEasing), label = "light")
    val animAwake by animateFloatAsState(targetValue = awakeTarget, animationSpec = tween(700, easing = FastOutSlowInEasing), label = "awake")

    val deepColor = Color(0xFF5E35B1) // Deep Purple
    val remColor = Color(0xFF00B0FF)  // Sky Blue
    val lightColor = Color(0xFFB388FF) // Lilac
    val awakeColor = Color(0xFFFF5252) // Coral

    Column(modifier = modifier.fillMaxWidth()) {
        // Multi-segment progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .weight(animDeep)
                    .fillMaxHeight()
                    .background(deepColor)
            )
            Box(
                modifier = Modifier
                    .weight(animRem)
                    .fillMaxHeight()
                    .background(remColor)
            )
            Box(
                modifier = Modifier
                    .weight(animLight)
                    .fillMaxHeight()
                    .background(lightColor)
            )
            Box(
                modifier = Modifier
                    .weight(animAwake)
                    .fillMaxHeight()
                    .background(awakeColor)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend row with clean duration & percentages
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SleepStageLegendItem("Deep", FormatUtils.formatDuration(deepMinutes), ((deepMinutes.toFloat() / totalMinutes) * 100).toInt(), deepColor)
            SleepStageLegendItem("REM", FormatUtils.formatDuration(remMinutes), ((remMinutes.toFloat() / totalMinutes) * 100).toInt(), remColor)
            SleepStageLegendItem("Light", FormatUtils.formatDuration(lightMinutes), ((lightMinutes.toFloat() / totalMinutes) * 100).toInt(), lightColor)
            SleepStageLegendItem("Awake", FormatUtils.formatDuration(awakeMinutes), ((awakeMinutes.toFloat() / totalMinutes) * 100).toInt(), awakeColor)
        }
    }
}

@Composable
private fun SleepStageLegendItem(
    label: String,
    duration: String,
    percent: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = duration,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "$percent%",
            color = TextTertiary,
            fontSize = 10.sp
        )
    }
}
