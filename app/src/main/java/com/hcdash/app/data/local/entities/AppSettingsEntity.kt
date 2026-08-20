package com.hcdash.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val stepGoal: Long = 10000L,
    val calorieGoal: Long = 500L,
    val sleepGoalHours: Double = 8.0,
    val weightGoalKg: Double = 72.0,
    val autoSyncOnLayerSwitch: Boolean = true,
    val autoSyncOnStartup: Boolean = true,
    val syncLookbackDays: Int = 30,
    val themeMode: String = "DARK", // "SYSTEM", "DARK", "LIGHT"
    val dynamicColor: Boolean = true
)
