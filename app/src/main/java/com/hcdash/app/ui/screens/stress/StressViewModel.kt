package com.hcdash.app.ui.screens.stress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hcdash.app.data.local.entities.StressHrvEntity
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

data class StressUiState(
    val selectedRange: TimeRange = TimeRange.DAYS_7,
    val stressPoints: List<AggregatedMetricPoint> = emptyList(),
    val stressTrend: TrendAnalysis? = null,
    val latestStress: StressHrvEntity? = null,
    val averageStressScore: Int = 35,
    val averageHrvRmssd: Double = 52.0
)

class StressViewModel(
    private val repository: HealthRepository,
    private val syncManager: HealthSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StressUiState())
    val uiState: StateFlow<StressUiState> = _uiState.asStateFlow()

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
            repository.observeStressHrv(start, now).collect { records ->
                val points = HealthAnalyticsEngine.aggregateStressHrv(records, range)
                val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.STRESS_HRV)
                val latest = records.lastOrNull()
                val avgStress = if (records.isNotEmpty()) records.map { it.stressScore }.average().toInt() else 0
                val avgHrv = if (records.isNotEmpty()) records.map { it.rmssdMs }.average() else 0.0

                _uiState.update {
                    it.copy(
                        stressPoints = points,
                        stressTrend = trend,
                        latestStress = latest,
                        averageStressScore = avgStress,
                        averageHrvRmssd = (avgHrv * 10).toInt() / 10.0
                    )
                }
            }
        }
    }
}
