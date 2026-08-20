package com.hcdash.app.ui.screens.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hcdash.app.data.repository.HealthRepository
import com.hcdash.app.data.sync.HealthSyncManager
import com.hcdash.app.domain.analytics.HealthAnalyticsEngine
import com.hcdash.app.domain.model.AggregatedMetricPoint
import com.hcdash.app.domain.model.MetricType
import com.hcdash.app.domain.model.TimeRange
import com.hcdash.app.domain.model.TrendAnalysis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

data class ActivityUiState(
    val selectedRange: TimeRange = TimeRange.DAYS_7,
    val stepPoints: List<AggregatedMetricPoint> = emptyList(),
    val stepTrend: TrendAnalysis? = null,
    val caloriePoints: List<AggregatedMetricPoint> = emptyList(),
    val calorieTrend: TrendAnalysis? = null,
    val totalStepsInPeriod: Long = 0L,
    val totalActiveCalories: Double = 0.0,
    val isLoading: Boolean = false
)

class ActivityViewModel(
    private val repository: HealthRepository,
    private val syncManager: HealthSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        loadData(TimeRange.DAYS_7)
    }

    fun setTimeRange(range: TimeRange) {
        _uiState.update { it.copy(selectedRange = range) }
        loadData(range)
    }

    private fun loadData(range: TimeRange) {
        val now = Instant.now()
        val start = now.minus(range.days.toLong(), ChronoUnit.DAYS)

        viewModelScope.launch {
            repository.observeSteps(start, now).collect { steps ->
                val points = HealthAnalyticsEngine.aggregateSteps(steps, range)
                val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.STEPS)
                val total = steps.sumOf { it.count }
                _uiState.update {
                    it.copy(
                        stepPoints = points,
                        stepTrend = trend,
                        totalStepsInPeriod = total
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.observeCalories(start, now).collect { calories ->
                val points = HealthAnalyticsEngine.aggregateCalories(calories, range)
                val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.CALORIES)
                val totalActive = calories.filter { !it.isBasal }.sumOf { it.energyKcal }
                _uiState.update {
                    it.copy(
                        caloriePoints = points,
                        calorieTrend = trend,
                        totalActiveCalories = (totalActive * 10).toInt() / 10.0
                    )
                }
            }
        }
    }
}
