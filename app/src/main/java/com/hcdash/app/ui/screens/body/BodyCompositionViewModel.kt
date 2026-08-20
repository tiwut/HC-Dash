package com.hcdash.app.ui.screens.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hcdash.app.data.local.entities.BodyCompositionEntity
import com.hcdash.app.data.local.entities.WeightRecordEntity
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

data class BodyCompositionUiState(
    val selectedRange: TimeRange = TimeRange.DAYS_30,
    val weightPoints: List<AggregatedMetricPoint> = emptyList(),
    val weightTrend: TrendAnalysis? = null,
    val fatPoints: List<AggregatedMetricPoint> = emptyList(),
    val fatTrend: TrendAnalysis? = null,
    val musclePoints: List<AggregatedMetricPoint> = emptyList(),
    val waterPoints: List<AggregatedMetricPoint> = emptyList(),
    val latestWeight: WeightRecordEntity? = null,
    val latestBody: BodyCompositionEntity? = null
)

class BodyCompositionViewModel(
    private val repository: HealthRepository,
    private val syncManager: HealthSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyCompositionUiState())
    val uiState: StateFlow<BodyCompositionUiState> = _uiState.asStateFlow()

    init {
        loadData(TimeRange.DAYS_30)
    }

    fun setTimeRange(range: TimeRange) {
        _uiState.update { it.copy(selectedRange = range) }
        loadData(range)
    }

    private fun loadData(range: TimeRange) {
        val now = Instant.now()
        val start = now.minus(range.days.toLong(), ChronoUnit.DAYS)

        viewModelScope.launch {
            repository.observeWeights(start, now).collect { weights ->
                val points = HealthAnalyticsEngine.aggregateWeight(weights)
                val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.WEIGHT)
                _uiState.update {
                    it.copy(
                        weightPoints = points,
                        weightTrend = trend,
                        latestWeight = weights.lastOrNull()
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.observeBodyCompositions(start, now).collect { bodies ->
                val fatPoints = HealthAnalyticsEngine.aggregateBodyMetric(bodies, MetricType.BODY_FAT)
                val fatTrend = HealthAnalyticsEngine.computeTrendAnalysis(fatPoints, MetricType.BODY_FAT)
                val musclePoints = HealthAnalyticsEngine.aggregateBodyMetric(bodies, MetricType.MUSCLE_MASS)
                val waterPoints = HealthAnalyticsEngine.aggregateBodyMetric(bodies, MetricType.BODY_WATER)

                _uiState.update {
                    it.copy(
                        fatPoints = fatPoints,
                        fatTrend = fatTrend,
                        musclePoints = musclePoints,
                        waterPoints = waterPoints,
                        latestBody = bodies.lastOrNull()
                    )
                }
            }
        }
    }
}
