package com.hcdash.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcdash.app.data.healthconnect.HealthConnectAvailability
import com.hcdash.app.domain.utils.FormatUtils
import com.hcdash.app.ui.components.AppIcons
import com.hcdash.app.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onRequestPermissions: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = Typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // ==================== 1. DAILY GOALS & TARGETS ====================
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
                        Icon(painter = painterResource(id = AppIcons.Target), contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Daily Goals", style = Typography.titleMedium, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step Goal Slider
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = AppIcons.Footprints), contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Step Goal", style = Typography.bodyMedium, color = TextSecondary)
                        }
                        Text(text = "${FormatUtils.formatInteger(state.settings.stepGoal)}", style = Typography.bodyLarge, fontWeight = FontWeight.Bold, color = NeonGreen)
                    }
                    Slider(
                        value = state.settings.stepGoal.toFloat(),
                        onValueChange = { viewModel.updateStepGoal((it / 500).toLong() * 500) },
                        valueRange = 3000f..25000f,
                        steps = 43,
                        colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calorie Goal Slider
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = AppIcons.Flame), contentDescription = null, tint = NeonOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Active Burn", style = Typography.bodyMedium, color = TextSecondary)
                        }
                        Text(text = "${FormatUtils.formatInteger(state.settings.calorieGoal)} kcal", style = Typography.bodyLarge, fontWeight = FontWeight.Bold, color = NeonOrange)
                    }
                    Slider(
                        value = state.settings.calorieGoal.toFloat(),
                        onValueChange = { viewModel.updateCalorieGoal((it / 50).toLong() * 50) },
                        valueRange = 200f..1500f,
                        steps = 25,
                        colors = SliderDefaults.colors(thumbColor = NeonOrange, activeTrackColor = NeonOrange)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sleep Target Slider
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = AppIcons.Moon), contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Sleep Target", style = Typography.bodyMedium, color = TextSecondary)
                        }
                        Text(text = "${FormatUtils.formatDecimal1(state.settings.sleepGoalHours)} hrs", style = Typography.bodyLarge, fontWeight = FontWeight.Bold, color = NeonPurple)
                    }
                    Slider(
                        value = state.settings.sleepGoalHours.toFloat(),
                        onValueChange = { viewModel.updateSleepGoal((it * 2).toInt() / 2.0) },
                        valueRange = 5f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple)
                    )
                }
            }
        }

        // ==================== 2. SYNC & AUTOMATION ====================
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
                        Icon(painter = painterResource(id = AppIcons.Refresh), contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Sync Automation", style = Typography.titleMedium, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-sync on Layer switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Auto-Sync on Tab Switch", style = Typography.bodyMedium, color = TextPrimary)
                        Switch(
                            checked = state.settings.autoSyncOnLayerSwitch,
                            onCheckedChange = { viewModel.toggleAutoSyncLayer(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Auto-sync on App start
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Auto-Sync on Startup", style = Typography.bodyMedium, color = TextPrimary)
                        Switch(
                            checked = state.settings.autoSyncOnStartup,
                            onCheckedChange = { viewModel.toggleAutoSyncStartup(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }

        // ==================== 3. APPEARANCE & NATIVE THEME ====================
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
                        Icon(painter = painterResource(id = AppIcons.Palette), contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Theme & Colors", style = Typography.titleMedium, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceVariant)
                            .padding(4.dp)
                    ) {
                        listOf("DARK" to "Dark", "LIGHT" to "Light", "SYSTEM" to "System").forEach { (mode, label) ->
                            val isSelected = state.settings.themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) DarkSurface else Color.Transparent)
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) NeonCyan else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Material You Dynamic Palette", style = Typography.bodyMedium, color = TextPrimary)
                        Switch(
                            checked = state.settings.dynamicColor,
                            onCheckedChange = { viewModel.toggleDynamicColor(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }

        // ==================== 4. HEALTH CONNECT PERMISSIONS ====================
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
                            Icon(painter = painterResource(id = AppIcons.ShieldCheck), contentDescription = null, tint = NeonRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Health Connect", style = Typography.titleMedium, color = TextPrimary)
                        }

                        val (statusText, statusColor) = when (state.availability) {
                            HealthConnectAvailability.AVAILABLE -> "Available" to NeonGreen
                            HealthConnectAvailability.NOT_INSTALLED -> "Not Installed" to NeonAmber
                            HealthConnectAvailability.NOT_SUPPORTED -> "Not Supported" to NeonRed
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Permissions: ${state.grantedPermissionsCount}/${state.totalPermissionsCount} granted",
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onRequestPermissions,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground)
                    ) {
                        Icon(painter = painterResource(id = AppIcons.ShieldCheck), contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Health Connect Permissions", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ==================== 5. DATA EXPORT & ACTIONS ====================
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
                        Icon(painter = painterResource(id = AppIcons.Send), contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Data Management", style = Typography.titleMedium, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.exportHealthReport(context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        Icon(painter = painterResource(id = AppIcons.Send), contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Share 7-Day Health Summary", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.seedSampleData(30) },
                            enabled = !state.isOperating,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White)
                        ) {
                            Text(text = "Seed 30D Demo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearAllData() },
                            enabled = !state.isOperating,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed)
                        ) {
                            Text(text = "Clear Data", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ==================== 6. ABOUT SECTION (tiwut) ====================
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
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = AppIcons.Github),
                                    contentDescription = "GitHub",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "HC-Dash", style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = "v1.0.0 · Local & Offline", style = Typography.labelSmall, color = TextTertiary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Open Source", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Developer Link
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant.copy(alpha = 0.7f))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tiwut"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(painter = painterResource(id = AppIcons.Github), contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Created by tiwut", style = Typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Icon(painter = painterResource(id = AppIcons.ChevronRight), contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Icon Library Link
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant.copy(alpha = 0.7f))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tiwut/Icon-Library"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(painter = painterResource(id = AppIcons.Palette), contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Icons: tiwut / Icon-Library", style = Typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Icon(painter = painterResource(id = AppIcons.ChevronRight), contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
