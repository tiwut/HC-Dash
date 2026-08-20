package com.hcdash.app.ui.screens.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hcdash.app.data.local.entities.SleepSessionEntity
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

data class SleepUiState(
    val selectedRange: TimeRange = TimeRange.DAYS_7,
    val sleepPoints: List<AggregatedMetricPoint> = emptyList(),
    val sleepTrend: TrendAnalysis? = null,
    val latestSession: SleepSessionEntity? = null,
    val averageDurationHours: Double = 0.0,
    val averageEfficiency: Int = 0
)

class SleepViewModel(
    private val repository: HealthRepository,
    private val syncManager: HealthSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

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
            repository.observeSleepSessions(start, now).collect { sessions ->
                val points = HealthAnalyticsEngine.aggregateSleep(sessions, range)
                val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.SLEEP)
                val latest = sessions.lastOrNull()
                val avgDuration = if (sessions.isNotEmpty()) {
                    sessions.map { it.durationMinutes / 60.0 }.average()
                } else 0.0
                val avgEff = if (sessions.isNotEmpty()) {
                    sessions.map { it.efficiencyScore }.average().toInt()
                } else 0

                _uiState.update {
                    it.copy(
                        sleepPoints = points,
                        sleepTrend = trend,
                        latestSession = latest,
                        averageDurationHours = (avgDuration * 10).toInt() / 10.0,
                        averageEfficiency = avgEff
                    )
                }
            }
        }
    }
}
