package com.hcdash.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hcdash.app.data.repository.HealthRepository
import com.hcdash.app.data.sync.HealthSyncManager
import com.hcdash.app.data.sync.SyncState
import com.hcdash.app.domain.analytics.HealthAnalyticsEngine
import com.hcdash.app.domain.model.AggregatedMetricPoint
import com.hcdash.app.domain.model.MetricType
import com.hcdash.app.domain.model.TimeRange
import com.hcdash.app.domain.model.TrendAnalysis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

data class DetailUiState(
    val metricType: MetricType = MetricType.STEPS,
    val selectedRange: TimeRange = TimeRange.DAYS_7,
    val points: List<AggregatedMetricPoint> = emptyList(),
    val trend: TrendAnalysis? = null,
    val isBarChart: Boolean = false,
    val latestValue: Double? = null,
    val totalSum: Double? = null
)

class DetailViewModel(
    private val repository: HealthRepository,
    private val syncManager: HealthSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState

    fun initMetric(metricType: MetricType) {
        val isBar = metricType == MetricType.STEPS || metricType == MetricType.CALORIES
        _uiState.update { it.copy(metricType = metricType, isBarChart = isBar) }

        // Trigger automatic refresh from Health Connect on detail open!
        syncManager.onDetailOpened(metricType)

        loadMetricData(metricType, _uiState.value.selectedRange)
    }

    fun setTimeRange(range: TimeRange) {
        _uiState.update { it.copy(selectedRange = range) }
        loadMetricData(_uiState.value.metricType, range)
    }

    fun manualRefresh() {
        syncManager.onDetailOpened(_uiState.value.metricType)
    }

    private fun loadMetricData(metricType: MetricType, range: TimeRange) {
        val now = Instant.now()
        val start = now.minus(range.days.toLong(), ChronoUnit.DAYS)

        viewModelScope.launch {
            when (metricType) {
                MetricType.STEPS -> {
                    repository.observeSteps(start, now).collect { steps ->
                        val points = HealthAnalyticsEngine.aggregateSteps(steps, range)
                        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, metricType)
                        val sum = steps.sumOf { it.count }.toDouble()
                        _uiState.update {
                            it.copy(
                                points = points,
                                trend = trend,
                                latestValue = points.lastOrNull()?.value,
                                totalSum = sum
                            )
                        }
                    }
                }
                MetricType.CALORIES -> {
                    repository.observeCalories(start, now).collect { calories ->
                        val points = HealthAnalyticsEngine.aggregateCalories(calories, range)
                        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, metricType)
                        val sum = calories.filter { !it.isBasal }.sumOf { it.energyKcal }
                        _uiState.update {
                            it.copy(
                                points = points,
                                trend = trend,
                                latestValue = points.lastOrNull()?.value,
                                totalSum = (sum * 10).toInt() / 10.0
                            )
                        }
                    }
                }
                MetricType.HEART_RATE -> {
                    repository.observeHeartRates(start, now).collect { records ->
                        val points = HealthAnalyticsEngine.aggregateHeartRate(records, range)
                        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, metricType)
                        _uiState.update {
                            it.copy(
                                points = points,
                                trend = trend,
                                latestValue = records.lastOrNull()?.bpm
                            )
                        }
                    }
                }
                MetricType.RESTING_HEART_RATE -> {
                    repository.observeRestingHeartRates(start, now).collect { records ->
                        val points = HealthAnalyticsEngine.aggregateHeartRate(records, range)
                        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, metricType)
                        _uiState.update {
                            it.copy(
                                points = points,
                                trend = trend,
                                latestValue = records.lastOrNull()?.bpm
                            )
                        }
                    }
                }
                MetricType.OXYGEN_SATURATION -> {
                    repository.observeOxygen(start, now).collect { records ->
                        val points = HealthAnalyticsEngine.aggregateOxygen(records, range)
                        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, metricType)
                        _uiState.update {
                            it.copy(
                                points = points,
                                trend = trend,
                                latestValue = records.lastOrNull()?.percentage
                            )
                        }
                    }
                }
                MetricType.SLEEP -> {
                    repository.observeSleepSessions(start, now).collect { records ->
                        val points = HealthAnalyticsEngine.aggregateSleep(records, range)
                        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, metricType)
                        _uiState.update {
                            it.copy(
                                points = points,
                                trend = trend,
                                latestValue = points.lastOrNull()?.value
                            )
                        }
                    }
                }
                MetricType.WEIGHT -> {
                    repository.observeWeights(start, now).collect { records ->
                        val points = HealthAnalyticsEngine.aggregateWeight(records)
                        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, metricType)
                        _uiState.update {
                            it.copy(
                                points = points,
                                trend = trend,
                                latestValue = records.lastOrNull()?.weightKg
                            )
                        }
                    }
                }
                MetricType.BMI -> {
                    repository.observeWeights(start, now).collect { records ->
                        val points = records.mapNotNull { rec ->
                            rec.bmi?.let {
                                AggregatedMetricPoint(
                                    timestamp = rec.time,
                                    label = rec.time.toString(),
                                    value = it
                                )
                            }
                        }
                        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, metricType)
                        _uiState.update {
                            it.copy(
                                points = points,
                                trend = trend,
                                latestValue = records.lastOrNull()?.bmi
                            )
                        }
                    }
                }
                MetricType.BODY_FAT, MetricType.BODY_WATER, MetricType.MUSCLE_MASS, MetricType.BONE_MASS -> {
                    repository.observeBodyCompositions(start, now).collect { records ->
                        val points = HealthAnalyticsEngine.aggregateBodyMetric(records, metricType)
                        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, metricType)
                        _uiState.update {
                            it.copy(
                                points = points,
                                trend = trend,
                                latestValue = points.lastOrNull()?.value
                            )
                        }
                    }
                }
                MetricType.STRESS_HRV -> {
                    repository.observeStressHrv(start, now).collect { records ->
                        val points = HealthAnalyticsEngine.aggregateStressHrv(records, range)
                        val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, metricType)
                        _uiState.update {
                            it.copy(
                                points = points,
                                trend = trend,
                                latestValue = records.lastOrNull()?.stressScore?.toDouble()
                            )
                        }
                    }
                }
            }
        }
    }
}
