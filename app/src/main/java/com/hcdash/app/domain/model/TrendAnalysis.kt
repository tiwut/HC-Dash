package com.hcdash.app.domain.model

import java.time.Instant

data class AggregatedMetricPoint(
    val timestamp: Instant,
    val label: String,
    val value: Double,
    val minValue: Double? = null,
    val maxValue: Double? = null,
    val secondaryValue: Double? = null
)

enum class TrendDirection {
    RISING,
    FALLING,
    STEADY
}

data class TrendAnalysis(
    val currentAverage: Double,
    val previousAverage: Double,
    val percentageChange: Double,
    val direction: TrendDirection,
    val min: Double,
    val max: Double,
    val latestValue: Double,
    val standardDeviation: Double,
    val trendSummary: String
)

data class HealthSummary(
    val todaySteps: Long,
    val todayCalories: Double,
    val currentHeartRate: Double?,
    val restingHeartRate: Double?,
    val oxygenSaturation: Double?,
    val latestSleepDurationMinutes: Long?,
    val latestSleepQualityScore: Int?,
    val latestWeight: Double?,
    val latestBmi: Double?,
    val latestBodyFat: Double?,
    val latestBodyWater: Double?,
    val latestMuscleMass: Double?,
    val latestStressScore: Int?,
    val latestHrvRmssd: Double?,
    val overallHealthScore: Int,
    val lastSyncTimestamp: Instant?
)
