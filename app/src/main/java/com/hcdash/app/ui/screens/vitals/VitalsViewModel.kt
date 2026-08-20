package com.hcdash.app.ui.screens.vitals

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

data class VitalsUiState(
    val selectedRange: TimeRange = TimeRange.DAYS_7,
    val heartRatePoints: List<AggregatedMetricPoint> = emptyList(),
    val heartRateTrend: TrendAnalysis? = null,
    val restingHeartRatePoints: List<AggregatedMetricPoint> = emptyList(),
    val oxygenPoints: List<AggregatedMetricPoint> = emptyList(),
    val oxygenTrend: TrendAnalysis? = null,
    val currentHr: Double? = null,
    val restingHr: Double? = null,
    val currentOxygen: Double? = null
)

class VitalsViewModel(
    private val repository: HealthRepository,
    private val syncManager: HealthSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VitalsUiState())
    val uiState: StateFlow<VitalsUiState> = _uiState.asStateFlow()

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
            repository.observeHeartRates(start, now).collect { records ->
                val points = HealthAnalyticsEngine.aggregateHeartRate(records, range)
                val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.HEART_RATE)
                val latest = records.lastOrNull()?.bpm
                _uiState.update {
                    it.copy(
                        heartRatePoints = points,
                        heartRateTrend = trend,
                        currentHr = latest
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.observeRestingHeartRates(start, now).collect { records ->
                val points = HealthAnalyticsEngine.aggregateHeartRate(records, range)
                val latest = records.lastOrNull()?.bpm
                _uiState.update {
                    it.copy(
                        restingHeartRatePoints = points,
                        restingHr = latest
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.observeOxygen(start, now).collect { records ->
                val points = HealthAnalyticsEngine.aggregateOxygen(records, range)
                val trend = HealthAnalyticsEngine.computeTrendAnalysis(points, MetricType.OXYGEN_SATURATION)
                val latest = records.lastOrNull()?.percentage
                _uiState.update {
                    it.copy(
                        oxygenPoints = points,
                        oxygenTrend = trend,
                        currentOxygen = latest
                    )
                }
            }
        }
    }
}
