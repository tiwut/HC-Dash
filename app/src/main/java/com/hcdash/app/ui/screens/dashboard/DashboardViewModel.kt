package com.hcdash.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hcdash.app.data.repository.HealthRepository
import com.hcdash.app.data.sync.HealthSyncManager
import com.hcdash.app.data.sync.SyncState
import com.hcdash.app.domain.model.HealthSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val repository: HealthRepository,
    val syncManager: HealthSyncManager
) : ViewModel() {

    val healthSummary: StateFlow<HealthSummary> = repository.observeHealthSummary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HealthSummary(
                todaySteps = 0L,
                todayCalories = 0.0,
                currentHeartRate = null,
                restingHeartRate = null,
                oxygenSaturation = null,
                latestSleepDurationMinutes = null,
                latestSleepQualityScore = null,
                latestWeight = null,
                latestBmi = null,
                latestBodyFat = null,
                latestBodyWater = null,
                latestMuscleMass = null,
                latestStressScore = null,
                latestHrvRmssd = null,
                overallHealthScore = 80,
                lastSyncTimestamp = null
            )
        )

    val syncState: StateFlow<SyncState> = syncManager.syncState

    fun manualRefresh() {
        syncManager.triggerManualSync()
    }
}
