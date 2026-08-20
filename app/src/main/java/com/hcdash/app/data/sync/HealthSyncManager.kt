package com.hcdash.app.data.sync

import com.hcdash.app.data.repository.HealthRepository
import com.hcdash.app.domain.model.MetricType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

sealed interface SyncState {
    data object Idle : SyncState
    data class Syncing(val message: String = "Syncing with Health Connect...") : SyncState
    data class Success(val lastSyncTime: Instant = Instant.now(), val message: String = "Up to date") : SyncState
    data class Error(val message: String, val timestamp: Instant = Instant.now()) : SyncState
}

class HealthSyncManager(
    private val repository: HealthRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val syncMutex = Mutex()
    private val lastSyncMap = ConcurrentHashMap<String, Instant>()
    private val debounceThresholdMillis = 6_000L // 6 seconds debounce for auto-triggers

    /**
     * Triggered automatically on Application Startup
     */
    fun onAppStart() {
        scope.launch {
            val settings = repository.dao.getAppSettingsSync()
            if (settings == null || settings.autoSyncOnStartup) {
                val lookback = settings?.syncLookbackDays ?: 30
                triggerFullSync("App Startup Sync", lookbackDays = lookback)
            }
        }
    }

    /**
     * Triggered automatically whenever the user navigates to a new layer / tab
     */
    fun onLayerOpened(layerName: String) {
        scope.launch {
            val settings = repository.dao.getAppSettingsSync()
            if (settings == null || settings.autoSyncOnLayerSwitch) {
                val lastSync = lastSyncMap[layerName]
                val now = Instant.now()
                if (lastSync == null || Duration.between(lastSync, now).toMillis() > debounceThresholdMillis) {
                    lastSyncMap[layerName] = now
                    val lookback = settings?.syncLookbackDays ?: 30
                    triggerFullSync("Refreshing: $layerName", lookbackDays = lookback)
                }
            }
        }
    }

    /**
     * Triggered automatically whenever the user opens a detailed overview screen
     */
    fun onDetailOpened(metricType: MetricType) {
        val key = "DETAIL_${metricType.name}"
        val lastSync = lastSyncMap[key]
        val now = Instant.now()
        if (lastSync == null || Duration.between(lastSync, now).toMillis() > debounceThresholdMillis) {
            lastSyncMap[key] = now
            triggerMetricSync(metricType, "Refreshing ${metricType.title}")
        }
    }

    /**
     * Triggered manually via Pull-to-refresh or Sync Button
     */
    fun triggerManualSync() {
        triggerFullSync("Manual Refresh", force = true)
    }

    private fun triggerFullSync(reason: String, force: Boolean = false, lookbackDays: Int = 30) {
        scope.launch {
            if (!force && syncMutex.isLocked) return@launch

            syncMutex.withLock {
                _syncState.value = SyncState.Syncing(reason)
                try {
                    withTimeout(25_000L) {
                        val result = repository.syncAllMetrics(lookbackDays = lookbackDays)
                        if (result.isSuccess) {
                            _syncState.value = SyncState.Success(Instant.now(), "Synced from Health Connect")
                        } else {
                            val err = result.exceptionOrNull()?.message ?: "Sync unavailable"
                            _syncState.value = SyncState.Error(err)
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    _syncState.value = SyncState.Error("Sync timed out. Retrying later.")
                } catch (e: CancellationException) {
                    // Normal coroutine cancellation
                } catch (e: Exception) {
                    _syncState.value = SyncState.Error(e.message ?: "Sync error")
                } finally {
                    delay(2500)
                    if (_syncState.value !is SyncState.Syncing) {
                        _syncState.value = SyncState.Idle
                    }
                }
            }
        }
    }

    private fun triggerMetricSync(metricType: MetricType, reason: String) {
        scope.launch {
            syncMutex.withLock {
                _syncState.value = SyncState.Syncing(reason)
                try {
                    withTimeout(15_000L) {
                        val result = repository.syncMetric(metricType, lookbackDays = 30)
                        if (result.isSuccess) {
                            _syncState.value = SyncState.Success(Instant.now(), "Updated ${metricType.title}")
                        } else {
                            _syncState.value = SyncState.Error(result.exceptionOrNull()?.message ?: "Update unavailable")
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    _syncState.value = SyncState.Error("Update timed out")
                } catch (e: CancellationException) {
                    // Normal coroutine cancellation
                } catch (e: Exception) {
                    _syncState.value = SyncState.Error(e.message ?: "Update error")
                } finally {
                    delay(2000)
                    if (_syncState.value !is SyncState.Syncing) {
                        _syncState.value = SyncState.Idle
                    }
                }
            }
        }
    }
}
