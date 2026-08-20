package com.hcdash.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcdash.app.data.sync.SyncState
import com.hcdash.app.ui.theme.*

@Composable
fun SyncStatusIndicator(
    syncState: SyncState,
    onManualSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "syncRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "syncRotationAngle"
    )

    val (bg, contentColor, text, isRotating) = when (syncState) {
        is SyncState.Idle -> Tuple4(DarkSurfaceVariant, TextSecondary, "Sync", false)
        is SyncState.Syncing -> Tuple4(NeonCyan.copy(alpha = 0.15f), NeonCyan, "Syncing", true)
        is SyncState.Success -> Tuple4(NeonGreen.copy(alpha = 0.15f), NeonGreen, "Live", false)
        is SyncState.Error -> Tuple4(NeonRed.copy(alpha = 0.15f), NeonRed, "Offline", false)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onManualSync() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = AppIcons.Refresh),
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier
                    .size(14.dp)
                    .then(if (isRotating) Modifier.rotate(rotationAngle) else Modifier)
            )

            Text(
                text = text,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
