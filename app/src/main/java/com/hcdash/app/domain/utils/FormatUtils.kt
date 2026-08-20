package com.hcdash.app.domain.utils

import com.hcdash.app.domain.model.MetricType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.round

object FormatUtils {

    private val symbols = DecimalFormatSymbols(Locale.US)
    private val intFormat = DecimalFormat("#,##0", symbols)
    private val dec1Format = DecimalFormat("#,##0.0", symbols)
    private val dec2Format = DecimalFormat("#,##0.00", symbols)

    /**
     * Round double to 1 decimal place cleanly
     */
    fun roundTo1Decimal(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return round(value * 10.0) / 10.0
    }

    /**
     * Round double to 2 decimal places cleanly
     */
    fun roundTo2Decimals(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return round(value * 100.0) / 100.0
    }

    /**
     * Format number as integer with comma grouping (e.g. 12,345)
     */
    fun formatInteger(value: Number?): String {
        if (value == null) return "--"
        val d = value.toDouble()
        if (d.isNaN() || d.isInfinite()) return "--"
        return intFormat.format(d.toLong())
    }

    /**
     * Format number with 1 decimal place (e.g. 76.5)
     */
    fun formatDecimal1(value: Number?): String {
        if (value == null) return "--"
        val d = value.toDouble()
        if (d.isNaN() || d.isInfinite()) return "--"
        return dec1Format.format(d)
    }

    /**
     * Format number with 2 decimal places (e.g. 23.45)
     */
    fun formatDecimal2(value: Number?): String {
        if (value == null) return "--"
        val d = value.toDouble()
        if (d.isNaN() || d.isInfinite()) return "--"
        return dec2Format.format(d)
    }

    /**
     * Format minutes as readable hours & minutes (e.g. 7h 45m)
     */
    fun formatDuration(minutes: Long?): String {
        if (minutes == null || minutes <= 0) return "--"
        val hours = minutes / 60
        val remMins = minutes % 60
        return if (hours > 0) "${hours}h ${remMins}m" else "${remMins}m"
    }

    /**
     * Format signed percentage change (e.g. +4.2% or -1.5%)
     */
    fun formatPercentChange(delta: Double?): String {
        if (delta == null || delta.isNaN() || delta.isInfinite()) return "0.0%"
        val sign = if (delta > 0) "+" else ""
        return "$sign${dec1Format.format(delta)}%"
    }

    /**
     * Context-aware formatting based on metric type
     */
    fun formatMetricValue(metricType: MetricType, value: Double?): String {
        if (value == null || value.isNaN() || value.isInfinite()) return "--"
        return when (metricType) {
            MetricType.STEPS -> formatInteger(value.toLong())
            MetricType.CALORIES -> formatInteger(value.toLong())
            MetricType.HEART_RATE, MetricType.RESTING_HEART_RATE -> formatInteger(value.toLong())
            MetricType.OXYGEN_SATURATION -> formatDecimal1(value)
            MetricType.SLEEP -> formatDecimal1(value)
            MetricType.WEIGHT -> formatDecimal1(value)
            MetricType.BMI -> formatDecimal1(value)
            MetricType.BODY_FAT, MetricType.BODY_WATER -> formatDecimal1(value)
            MetricType.MUSCLE_MASS, MetricType.BONE_MASS -> formatDecimal1(value)
            MetricType.STRESS_HRV -> formatInteger(value.toLong())
        }
    }
}
