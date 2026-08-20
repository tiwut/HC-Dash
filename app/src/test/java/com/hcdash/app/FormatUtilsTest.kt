package com.hcdash.app

import com.hcdash.app.domain.model.MetricType
import com.hcdash.app.domain.utils.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun testFormatInteger() {
        assertEquals("12,345", FormatUtils.formatInteger(12345))
        assertEquals("0", FormatUtils.formatInteger(0))
        assertEquals("--", FormatUtils.formatInteger(null))
        assertEquals("--", FormatUtils.formatInteger(Double.NaN))
    }

    @Test
    fun testFormatDecimal1() {
        assertEquals("76.5", FormatUtils.formatDecimal1(76.499999))
        assertEquals("0.0", FormatUtils.formatDecimal1(0.0))
        assertEquals("--", FormatUtils.formatDecimal1(null))
        assertEquals("--", FormatUtils.formatDecimal1(Double.POSITIVE_INFINITY))
    }

    @Test
    fun testFormatDuration() {
        assertEquals("7h 45m", FormatUtils.formatDuration(465))
        assertEquals("45m", FormatUtils.formatDuration(45))
        assertEquals("--", FormatUtils.formatDuration(null))
        assertEquals("--", FormatUtils.formatDuration(0))
    }

    @Test
    fun testFormatPercentChange() {
        assertEquals("+4.2%", FormatUtils.formatPercentChange(4.2))
        assertEquals("-1.5%", FormatUtils.formatPercentChange(-1.5))
        assertEquals("0.0%", FormatUtils.formatPercentChange(0.0))
        assertEquals("0.0%", FormatUtils.formatPercentChange(null))
    }

    @Test
    fun testFormatMetricValue() {
        assertEquals("10,500", FormatUtils.formatMetricValue(MetricType.STEPS, 10500.0))
        assertEquals("2,450", FormatUtils.formatMetricValue(MetricType.CALORIES, 2450.4))
        assertEquals("72", FormatUtils.formatMetricValue(MetricType.HEART_RATE, 72.3))
        assertEquals("98.5", FormatUtils.formatMetricValue(MetricType.OXYGEN_SATURATION, 98.54))
        assertEquals("76.5", FormatUtils.formatMetricValue(MetricType.WEIGHT, 76.48))
        assertEquals("23.4", FormatUtils.formatMetricValue(MetricType.BMI, 23.41))
        assertEquals("17.5", FormatUtils.formatMetricValue(MetricType.BODY_FAT, 17.52))
        assertEquals("35", FormatUtils.formatMetricValue(MetricType.STRESS_HRV, 35.0))
        assertEquals("--", FormatUtils.formatMetricValue(MetricType.STEPS, null))
    }
}
