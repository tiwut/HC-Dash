package com.hcdash.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "step_records",
    indices = [Index(value = ["startTime", "endTime"])]
)
data class StepRecordEntity(
    @PrimaryKey val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val count: Long,
    val sourceApp: String? = null,
    val syncedAt: Instant = Instant.now()
)

@Entity(
    tableName = "calorie_records",
    indices = [Index(value = ["startTime", "endTime"])]
)
data class CalorieRecordEntity(
    @PrimaryKey val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val energyKcal: Double,
    val isBasal: Boolean = false,
    val sourceApp: String? = null,
    val syncedAt: Instant = Instant.now()
)

@Entity(
    tableName = "heart_rate_records",
    indices = [Index(value = ["time"])]
)
data class HeartRateRecordEntity(
    @PrimaryKey val id: String,
    val time: Instant,
    val bpm: Double,
    val isResting: Boolean = false,
    val sourceApp: String? = null,
    val syncedAt: Instant = Instant.now()
)

@Entity(
    tableName = "oxygen_saturation_records",
    indices = [Index(value = ["time"])]
)
data class OxygenSaturationRecordEntity(
    @PrimaryKey val id: String,
    val time: Instant,
    val percentage: Double,
    val sourceApp: String? = null,
    val syncedAt: Instant = Instant.now()
)

@Entity(
    tableName = "sleep_sessions",
    indices = [Index(value = ["startTime", "endTime"])]
)
data class SleepSessionEntity(
    @PrimaryKey val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Long,
    val deepMinutes: Long = 0,
    val lightMinutes: Long = 0,
    val remMinutes: Long = 0,
    val awakeMinutes: Long = 0,
    val efficiencyScore: Int = 85,
    val title: String? = null,
    val sourceApp: String? = null,
    val syncedAt: Instant = Instant.now()
)

@Entity(
    tableName = "weight_records",
    indices = [Index(value = ["time"])]
)
data class WeightRecordEntity(
    @PrimaryKey val id: String,
    val time: Instant,
    val weightKg: Double,
    val bmi: Double? = null,
    val sourceApp: String? = null,
    val syncedAt: Instant = Instant.now()
)

@Entity(
    tableName = "body_composition_records",
    indices = [Index(value = ["time"])]
)
data class BodyCompositionEntity(
    @PrimaryKey val id: String,
    val time: Instant,
    val bodyFatPercentage: Double? = null,
    val bodyWaterPercentage: Double? = null,
    val muscleMassKg: Double? = null,
    val boneMassKg: Double? = null,
    val sourceApp: String? = null,
    val syncedAt: Instant = Instant.now()
)

@Entity(
    tableName = "stress_hrv_records",
    indices = [Index(value = ["time"])]
)
data class StressHrvEntity(
    @PrimaryKey val id: String,
    val time: Instant,
    val rmssdMs: Double,
    val stressScore: Int, // 0-100
    val sourceApp: String? = null,
    val syncedAt: Instant = Instant.now()
)

@Entity(
    tableName = "sync_metadata"
)
data class SyncMetaEntity(
    @PrimaryKey val category: String,
    val lastSyncTime: Instant,
    val changeToken: String? = null,
    val status: String = "SUCCESS",
    val errorMessage: String? = null
)
