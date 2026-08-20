package com.hcdash.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hcdash.app.data.healthconnect.HealthConnectAvailability
import com.hcdash.app.data.healthconnect.HealthConnectPermissions
import com.hcdash.app.data.local.entities.AppSettingsEntity
import com.hcdash.app.data.local.entities.SyncMetaEntity
import com.hcdash.app.data.repository.HealthRepository
import com.hcdash.app.data.sync.HealthSyncManager
import com.hcdash.app.domain.utils.FormatUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

data class SettingsUiState(
    val settings: AppSettingsEntity = AppSettingsEntity(),
    val availability: HealthConnectAvailability = HealthConnectAvailability.AVAILABLE,
    val grantedPermissionsCount: Int = 0,
    val totalPermissionsCount: Int = HealthConnectPermissions.READ_PERMISSIONS.size,
    val syncMetaList: List<SyncMetaEntity> = emptyList(),
    val isOperating: Boolean = false,
    val toastMessage: String? = null
)

class SettingsViewModel(
    private val repository: HealthRepository,
    private val syncManager: HealthSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
        viewModelScope.launch {
            repository.observeAppSettings().collect { s ->
                _uiState.update { it.copy(settings = s) }
            }
        }
        viewModelScope.launch {
            repository.dao.getAllSyncMeta().collect { list ->
                _uiState.update { it.copy(syncMetaList = list) }
            }
        }
    }

    fun refreshStatus() {
        val avail = repository.checkHealthConnectAvailability()
        viewModelScope.launch {
            val granted = repository.getGrantedPermissions()
            _uiState.update {
                it.copy(
                    availability = avail,
                    grantedPermissionsCount = granted.size
                )
            }
        }
    }

    // Goals update
    fun updateStepGoal(steps: Long) {
        val current = _uiState.value.settings
        val updated = current.copy(stepGoal = steps)
        viewModelScope.launch { repository.updateAppSettings(updated) }
    }

    fun updateCalorieGoal(kcal: Long) {
        val current = _uiState.value.settings
        val updated = current.copy(calorieGoal = kcal)
        viewModelScope.launch { repository.updateAppSettings(updated) }
    }

    fun updateSleepGoal(hours: Double) {
        val current = _uiState.value.settings
        val updated = current.copy(sleepGoalHours = FormatUtils.roundTo1Decimal(hours))
        viewModelScope.launch { repository.updateAppSettings(updated) }
    }

    fun updateWeightGoal(kg: Double) {
        val current = _uiState.value.settings
        val updated = current.copy(weightGoalKg = FormatUtils.roundTo1Decimal(kg))
        viewModelScope.launch { repository.updateAppSettings(updated) }
    }

    // Automation & Sync settings
    fun toggleAutoSyncLayer(enabled: Boolean) {
        val current = _uiState.value.settings
        val updated = current.copy(autoSyncOnLayerSwitch = enabled)
        viewModelScope.launch { repository.updateAppSettings(updated) }
    }

    fun toggleAutoSyncStartup(enabled: Boolean) {
        val current = _uiState.value.settings
        val updated = current.copy(autoSyncOnStartup = enabled)
        viewModelScope.launch { repository.updateAppSettings(updated) }
    }

    fun setLookbackDays(days: Int) {
        val current = _uiState.value.settings
        val updated = current.copy(syncLookbackDays = days)
        viewModelScope.launch { repository.updateAppSettings(updated) }
    }

    // Theme settings
    fun setThemeMode(mode: String) {
        val current = _uiState.value.settings
        val updated = current.copy(themeMode = mode)
        viewModelScope.launch { repository.updateAppSettings(updated) }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        val current = _uiState.value.settings
        val updated = current.copy(dynamicColor = enabled)
        viewModelScope.launch { repository.updateAppSettings(updated) }
    }

    // Export summary report
    fun exportHealthReport(context: Context) {
        viewModelScope.launch {
            val now = Instant.now()
            val weekAgo = now.minus(7, ChronoUnit.DAYS)
            val steps = repository.dao.getTotalStepsBetweenSync(weekAgo, now)
            val calories = repository.dao.getTotalCaloriesBetweenSync(weekAgo, now)
            val avgHr = repository.dao.getAverageHeartRate(weekAgo, now)
            val avgSpo2 = repository.dao.getAverageOxygen(weekAgo, now)
            val avgSleep = repository.dao.getAverageSleepDurationMinutes(weekAgo, now)
            val avgStress = repository.dao.getAverageStressScore(weekAgo, now)
            val latestWeight = repository.dao.getLatestWeight()

            val report = buildString {
                appendLine("═══ HC-DASH 7-DAY HEALTH REPORT ═══")
                appendLine("Generated on: ${Instant.now()}")
                appendLine("──────────────────────────────────")
                appendLine("• 7-Day Total Steps: ${FormatUtils.formatInteger(steps)}")
                appendLine("• 7-Day Calories Burned: ${FormatUtils.formatInteger(calories.toLong())} kcal")
                appendLine("• Average Heart Rate: ${FormatUtils.formatInteger(avgHr?.toLong())} bpm")
                appendLine("• Average SpO2: ${FormatUtils.formatDecimal1(avgSpo2)}%")
                appendLine("• Average Night Sleep: ${FormatUtils.formatDecimal1(avgSleep?.let { it / 60.0 })} hrs")
                appendLine("• Average Stress Score: ${FormatUtils.formatInteger(avgStress?.toLong())}/100")
                appendLine("• Daily Step Goal: ${FormatUtils.formatInteger(_uiState.value.settings.stepGoal)}")
                appendLine("• Daily Calorie Goal: ${FormatUtils.formatInteger(_uiState.value.settings.calorieGoal)} kcal")
                appendLine("──────────────────────────────────")
                appendLine("Data locally verified from Health Connect & Room Database.")
            }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, report)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Health Report")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        }
    }

    // Seeding & database cleanup
    fun seedSampleData(days: Int = 30) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true) }
            repository.seedSampleData(days)
            _uiState.update {
                it.copy(
                    isOperating = false,
                    toastMessage = "Successfully generated $days days of realistic health data!"
                )
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true) }
            repository.clearAllData()
            _uiState.update {
                it.copy(
                    isOperating = false,
                    toastMessage = "All local Room data cleared."
                )
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
