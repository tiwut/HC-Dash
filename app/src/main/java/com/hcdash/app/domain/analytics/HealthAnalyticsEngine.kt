package com.hcdash.app.domain.analytics

import com.hcdash.app.data.local.entities.*
import com.hcdash.app.domain.model.*
import com.hcdash.app.domain.utils.FormatUtils
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.sqrt

object HealthAnalyticsEngine {

    private val zone = ZoneId.systemDefault()
    private val dayFormatter = DateTimeFormatter.ofPattern("MMM dd")

    /**
     * Compute trend analysis, high/low, averages, and regression direction with safe math
     */
    fun computeTrendAnalysis(
        points: List<AggregatedMetricPoint>,
        metricType: MetricType
    ): TrendAnalysis {
        if (points.isEmpty()) {
            return TrendAnalysis(
                currentAverage = 0.0,
                previousAverage = 0.0,
                percentageChange = 0.0,
                direction = TrendDirection.STEADY,
                min = 0.0,
                max = 0.0,
                latestValue = 0.0,
                standardDeviation = 0.0,
                trendSummary = "No data available"
            )
        }

        val values = points.map { if (it.value.isNaN() || it.value.isInfinite()) 0.0 else it.value }
        val latest = values.lastOrNull() ?: 0.0
        val min = values.minOrNull() ?: 0.0
        val max = values.maxOrNull() ?: 0.0

        val midPoint = points.size / 2
        val firstHalf = if (midPoint > 0) values.subList(0, midPoint) else values
        val secondHalf = if (midPoint > 0) values.subList(midPoint, values.size) else values

        val prevAvg = if (firstHalf.isNotEmpty()) firstHalf.average() else latest
        val currAvg = if (secondHalf.isNotEmpty()) secondHalf.average() else latest

        val pctChange = if (prevAvg > 0.0001) {
            ((currAvg - prevAvg) / prevAvg) * 100.0
        } else {
            0.0
        }

        // Safe linear regression slope calculation
        val n = values.size
        var slope = 0.0
        if (n > 1) {
            val sumX = (0 until n).sum().toDouble()
            val sumY = values.sum()
            val sumXY = values.indices.sumOf { it * values[it] }
            val sumX2 = (0 until n).sumOf { (it * it).toDouble() }
            val denominator = n * sumX2 - sumX.pow(2.0)
            if (denominator != 0.0) {
                slope = (n * sumXY - sumX * sumY) / denominator
            }
        }

        val direction = when {
            slope > 0.05 -> TrendDirection.RISING
            slope < -0.05 -> TrendDirection.FALLING
            else -> TrendDirection.STEADY
        }

        // Safe standard deviation
        val variance = if (values.isNotEmpty()) {
            values.map { (it - currAvg).pow(2.0) }.average()
        } else 0.0
        val stdDev = if (variance >= 0.0) sqrt(variance) else 0.0

        val roundedPct = FormatUtils.roundTo1Decimal(pctChange)
        val formattedPct = FormatUtils.formatPercentChange(roundedPct)
        val directionWord = when (direction) {
            TrendDirection.RISING -> "Trending upward"
            TrendDirection.FALLING -> "Trending downward"
            TrendDirection.STEADY -> "Stable"
        }

        val summary = "$formattedPct vs previous period · $directionWord"

        return TrendAnalysis(
            currentAverage = FormatUtils.roundTo1Decimal(currAvg),
            previousAverage = FormatUtils.roundTo1Decimal(prevAvg),
            percentageChange = roundedPct,
            direction = direction,
            min = FormatUtils.roundTo1Decimal(min),
            max = FormatUtils.roundTo1Decimal(max),
            latestValue = FormatUtils.roundTo1Decimal(latest),
            standardDeviation = FormatUtils.roundTo1Decimal(stdDev),
            trendSummary = summary
        )
    }

    // ==================== STEPS AGGREGATION ====================
    fun aggregateSteps(
        steps: List<StepRecordEntity>,
        timeRange: TimeRange
    ): List<AggregatedMetricPoint> {
        if (steps.isEmpty()) return emptyList()

        return if (timeRange == TimeRange.DAY_1) {
            steps.groupBy {
                it.startTime.atZone(zone).hour
            }.map { (hour, list) ->
                val sum = list.sumOf { it.count }.toDouble()
                val sampleInstant = list.first().startTime
                AggregatedMetricPoint(
                    timestamp = sampleInstant,
                    label = String.format(java.util.Locale.US, "%02d:00", hour),
                    value = sum
                )
            }.sortedBy { it.timestamp }
        } else {
            steps.groupBy {
                it.startTime.atZone(zone).toLocalDate()
            }.map { (date, list) ->
                val sum = list.sumOf { it.count }.toDouble()
                AggregatedMetricPoint(
                    timestamp = date.atStartOfDay(zone).toInstant(),
                    label = date.format(dayFormatter),
                    value = sum
                )
            }.sortedBy { it.timestamp }
        }
    }

    // ==================== CALORIES AGGREGATION ====================
    fun aggregateCalories(
        calories: List<CalorieRecordEntity>,
        timeRange: TimeRange
    ): List<AggregatedMetricPoint> {
        if (calories.isEmpty()) return emptyList()

        return if (timeRange == TimeRange.DAY_1) {
            calories.filter { !it.isBasal }.groupBy {
                it.startTime.atZone(zone).hour
            }.map { (hour, list) ->
                val sum = list.sumOf { it.energyKcal }
                AggregatedMetricPoint(
                    timestamp = list.first().startTime,
                    label = String.format(java.util.Locale.US, "%02d:00", hour),
                    value = FormatUtils.roundTo1Decimal(sum)
                )
            }.sortedBy { it.timestamp }
        } else {
            calories.groupBy {
                it.startTime.atZone(zone).toLocalDate()
            }.map { (date, list) ->
                val active = list.filter { !it.isBasal }.sumOf { it.energyKcal }
                val total = list.sumOf { it.energyKcal }
                AggregatedMetricPoint(
                    timestamp = date.atStartOfDay(zone).toInstant(),
                    label = date.format(dayFormatter),
                    value = FormatUtils.roundTo1Decimal(active),
                    secondaryValue = FormatUtils.roundTo1Decimal(total)
                )
            }.sortedBy { it.timestamp }
        }
    }

    // ==================== HEART RATE AGGREGATION ====================
    fun aggregateHeartRate(
        records: List<HeartRateRecordEntity>,
        timeRange: TimeRange
    ): List<AggregatedMetricPoint> {
        if (records.isEmpty()) return emptyList()

        return if (timeRange == TimeRange.DAY_1) {
            records.groupBy {
                it.time.atZone(zone).hour
            }.map { (hour, list) ->
                val avg = list.map { it.bpm }.average()
                val min = list.minOf { it.bpm }
                val max = list.maxOf { it.bpm }
                AggregatedMetricPoint(
                    timestamp = list.first().time,
                    label = String.format(java.util.Locale.US, "%02d:00", hour),
                    value = FormatUtils.roundTo1Decimal(avg),
                    minValue = FormatUtils.roundTo1Decimal(min),
                    maxValue = FormatUtils.roundTo1Decimal(max)
                )
            }.sortedBy { it.timestamp }
        } else {
            records.groupBy {
                it.time.atZone(zone).toLocalDate()
            }.map { (date, list) ->
                val avg = list.map { it.bpm }.average()
                val min = list.minOf { it.bpm }
                val max = list.maxOf { it.bpm }
                AggregatedMetricPoint(
                    timestamp = date.atStartOfDay(zone).toInstant(),
                    label = date.format(dayFormatter),
                    value = FormatUtils.roundTo1Decimal(avg),
                    minValue = FormatUtils.roundTo1Decimal(min),
                    maxValue = FormatUtils.roundTo1Decimal(max)
                )
            }.sortedBy { it.timestamp }
        }
    }

    // ==================== OXYGEN AGGREGATION ====================
    fun aggregateOxygen(
        records: List<OxygenSaturationRecordEntity>,
        timeRange: TimeRange
    ): List<AggregatedMetricPoint> {
        if (records.isEmpty()) return emptyList()

        return records.groupBy {
            it.time.atZone(zone).toLocalDate()
        }.map { (date, list) ->
            val avg = list.map { it.percentage }.average()
            val min = list.minOf { it.percentage }
            val max = list.maxOf { it.percentage }
            AggregatedMetricPoint(
                timestamp = date.atStartOfDay(zone).toInstant(),
                label = date.format(dayFormatter),
                value = FormatUtils.roundTo1Decimal(avg),
                minValue = FormatUtils.roundTo1Decimal(min),
                maxValue = FormatUtils.roundTo1Decimal(max)
            )
        }.sortedBy { it.timestamp }
    }

    // ==================== SLEEP AGGREGATION ====================
    fun aggregateSleep(
        records: List<SleepSessionEntity>,
        timeRange: TimeRange
    ): List<AggregatedMetricPoint> {
        if (records.isEmpty()) return emptyList()

        return records.map { session ->
            val hours = session.durationMinutes / 60.0
            AggregatedMetricPoint(
                timestamp = session.startTime,
                label = session.startTime.atZone(zone).toLocalDate().format(dayFormatter),
                value = FormatUtils.roundTo1Decimal(hours),
                secondaryValue = session.efficiencyScore.toDouble()
            )
        }.sortedBy { it.timestamp }
    }

    // ==================== WEIGHT & BODY COMPOSITION ====================
    fun aggregateWeight(
        records: List<WeightRecordEntity>
    ): List<AggregatedMetricPoint> {
        if (records.isEmpty()) return emptyList()

        return records.map {
            AggregatedMetricPoint(
                timestamp = it.time,
                label = it.time.atZone(zone).toLocalDate().format(dayFormatter),
                value = FormatUtils.roundTo1Decimal(it.weightKg),
                secondaryValue = it.bmi?.let { bmi -> FormatUtils.roundTo1Decimal(bmi) }
            )
        }.sortedBy { it.timestamp }
    }

    fun aggregateBodyMetric(
        records: List<BodyCompositionEntity>,
        metricType: MetricType
    ): List<AggregatedMetricPoint> {
        if (records.isEmpty()) return emptyList()

        return records.mapNotNull { item ->
            val v = when (metricType) {
                MetricType.BODY_FAT -> item.bodyFatPercentage
                MetricType.BODY_WATER -> item.bodyWaterPercentage
                MetricType.MUSCLE_MASS -> item.muscleMassKg
                MetricType.BONE_MASS -> item.boneMassKg
                else -> null
            }
            v?.let {
                AggregatedMetricPoint(
                    timestamp = item.time,
                    label = item.time.atZone(zone).toLocalDate().format(dayFormatter),
                    value = FormatUtils.roundTo1Decimal(it)
                )
            }
        }.sortedBy { it.timestamp }
    }

    // ==================== STRESS & HRV AGGREGATION ====================
    fun aggregateStressHrv(
        records: List<StressHrvEntity>,
        timeRange: TimeRange
    ): List<AggregatedMetricPoint> {
        if (records.isEmpty()) return emptyList()

        return if (timeRange == TimeRange.DAY_1) {
            records.groupBy {
                it.time.atZone(zone).hour
            }.map { (hour, list) ->
                val avgStress = list.map { it.stressScore }.average()
                val avgHrv = list.map { it.rmssdMs }.average()
                AggregatedMetricPoint(
                    timestamp = list.first().time,
                    label = String.format(java.util.Locale.US, "%02d:00", hour),
                    value = FormatUtils.roundTo1Decimal(avgStress),
                    secondaryValue = FormatUtils.roundTo1Decimal(avgHrv)
                )
            }.sortedBy { it.timestamp }
        } else {
            records.groupBy {
                it.time.atZone(zone).toLocalDate()
            }.map { (date, list) ->
                val avgStress = list.map { it.stressScore }.average()
                val avgHrv = list.map { it.rmssdMs }.average()
                AggregatedMetricPoint(
                    timestamp = date.atStartOfDay(zone).toInstant(),
                    label = date.format(dayFormatter),
                    value = FormatUtils.roundTo1Decimal(avgStress),
                    secondaryValue = FormatUtils.roundTo1Decimal(avgHrv)
                )
            }.sortedBy { it.timestamp }
        }
    }
}
