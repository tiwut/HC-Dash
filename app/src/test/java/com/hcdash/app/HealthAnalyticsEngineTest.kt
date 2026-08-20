package com.hcdash.app

import com.hcdash.app.data.local.entities.StepRecordEntity
import com.hcdash.app.domain.analytics.HealthAnalyticsEngine
import com.hcdash.app.domain.model.AggregatedMetricPoint
import com.hcdash.app.domain.model.MetricType
import com.hcdash.app.domain.model.TimeRange
import com.hcdash.app.domain.model.TrendDirection
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthAnalyticsEngineTest {

    @Test
    fun testTrendAnalysisRising() {
        val now = Instant.now()
        val points = listOf(
            AggregatedMetricPoint(now.minus(4, ChronoUnit.DAYS), "Day 1", 5000.0),
            AggregatedMetricPoint(now.minus(3, ChronoUnit.DAYS), "Day 2", 6000.0),
            AggregatedMetricPoint(now.minus(2, ChronoUnit.DAYS), "Day 3", 7500.0),
            AggregatedMetricPoint(now.minus(1, ChronoUnit.DAYS), "Day 4", 9000.0),
            AggregatedMetricPoint(now, "Day 5", 10500.0)
        )

        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.STEPS)

        assertEquals(5000.0, trend.min, 0.1)
        assertEquals(10500.0, trend.max, 0.1)
        assertEquals(10500.0, trend.latestValue, 0.1)
        assertTrue(trend.percentageChange > 0)
        assertEquals(TrendDirection.RISING, trend.direction)
    }

    @Test
    fun testTrendAnalysisFalling() {
        val now = Instant.now()
        val points = listOf(
            AggregatedMetricPoint(now.minus(4, ChronoUnit.DAYS), "Day 1", 10000.0),
            AggregatedMetricPoint(now.minus(3, ChronoUnit.DAYS), "Day 2", 9000.0),
            AggregatedMetricPoint(now.minus(2, ChronoUnit.DAYS), "Day 3", 7500.0),
            AggregatedMetricPoint(now.minus(1, ChronoUnit.DAYS), "Day 4", 6000.0),
            AggregatedMetricPoint(now, "Day 5", 4000.0)
        )

        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.STEPS)

        assertEquals(4000.0, trend.min, 0.1)
        assertEquals(10000.0, trend.max, 0.1)
        assertTrue(trend.percentageChange < 0)
        assertEquals(TrendDirection.FALLING, trend.direction)
    }

    @Test
    fun testTrendAnalysisEmptyList() {
        val trend = HealthAnalyticsEngine.computeTrendAnalysis(emptyList(), MetricType.WEIGHT)
        assertEquals(0.0, trend.min, 0.01)
        assertEquals(0.0, trend.max, 0.01)
        assertEquals(TrendDirection.STEADY, trend.direction)
    }

    @Test
    fun testTrendAnalysisSinglePoint() {
        val now = Instant.now()
        val points = listOf(AggregatedMetricPoint(now, "Today", 76.5))
        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.WEIGHT)
        assertEquals(76.5, trend.min, 0.01)
        assertEquals(76.5, trend.max, 0.01)
        assertEquals(76.5, trend.currentAverage, 0.01)
        assertEquals(TrendDirection.STEADY, trend.direction)
    }

    @Test
    fun testTrendAnalysisIdenticalValues() {
        val now = Instant.now()
        val points = listOf(
            AggregatedMetricPoint(now.minus(2, ChronoUnit.DAYS), "Day 1", 70.0),
            AggregatedMetricPoint(now.minus(1, ChronoUnit.DAYS), "Day 2", 70.0),
            AggregatedMetricPoint(now, "Day 3", 70.0)
        )
        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.HEART_RATE)
        assertEquals(70.0, trend.min, 0.01)
        assertEquals(70.0, trend.max, 0.01)
        assertEquals(0.0, trend.standardDeviation, 0.01)
        assertEquals(TrendDirection.STEADY, trend.direction)
    }

    @Test
    fun testAggregateStepsDaily() {
        val now = Instant.now()
        val steps = listOf(
            StepRecordEntity("1", now.minus(2, ChronoUnit.HOURS), now.minus(1, ChronoUnit.HOURS), 2500),
            StepRecordEntity("2", now.minus(1, ChronoUnit.HOURS), now, 3500)
        )

        val points = HealthAnalyticsEngine.aggregateSteps(steps, TimeRange.DAYS_7)
        assertFalse(points.isEmpty())
        assertEquals(6000.0, points.first().value, 0.1)
    }
}
