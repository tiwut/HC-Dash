package com.hcdash.app.data.repository

import com.hcdash.app.data.generator.SampleHealthDataGenerator
import com.hcdash.app.data.healthconnect.HealthConnectAvailability
import com.hcdash.app.data.healthconnect.HealthConnectManager
import com.hcdash.app.data.local.dao.HealthMetricsDao
import com.hcdash.app.data.local.entities.*
import com.hcdash.app.domain.model.HealthSummary
import com.hcdash.app.domain.model.MetricType
import com.hcdash.app.domain.utils.FormatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class HealthRepository(
    val healthConnectManager: HealthConnectManager,
    val dao: HealthMetricsDao
) {
    private val generator = SampleHealthDataGenerator(dao)

    fun checkHealthConnectAvailability(): HealthConnectAvailability =
        healthConnectManager.checkAvailability()

    suspend fun hasAllPermissions(): Boolean =
        healthConnectManager.hasAllPermissions()

    suspend fun getGrantedPermissions(): Set<String> =
        healthConnectManager.getGrantedPermissions()

    // ==================== SYNC METHODS ====================

    suspend fun syncAllMetrics(lookbackDays: Int = 30): Result<Unit> = withContext(Dispatchers.IO) {
        val now = Instant.now()
        val startTime = now.minus(lookbackDays.toLong(), ChronoUnit.DAYS)

        if (!healthConnectManager.isAvailable()) {
            return@withContext Result.failure(Exception("Health Connect is not available on this device"))
        }

        return@withContext try {
            val steps = healthConnectManager.readSteps(startTime, now)
            if (steps.isNotEmpty()) dao.insertSteps(steps)

            val calories = healthConnectManager.readCalories(startTime, now)
            if (calories.isNotEmpty()) dao.insertCalories(calories)

            val heartRates = healthConnectManager.readHeartRates(startTime, now)
            if (heartRates.isNotEmpty()) dao.insertHeartRates(heartRates)

            val oxygen = healthConnectManager.readOxygenSaturation(startTime, now)
            if (oxygen.isNotEmpty()) dao.insertOxygenRecords(oxygen)

            val sleep = healthConnectManager.readSleepSessions(startTime, now)
            if (sleep.isNotEmpty()) dao.insertSleepSessions(sleep)

            val weights = healthConnectManager.readWeights(startTime, now)
            if (weights.isNotEmpty()) dao.insertWeights(weights)

            val body = healthConnectManager.readBodyComposition(startTime, now)
            if (body.isNotEmpty()) dao.insertBodyCompositions(body)

            val stress = healthConnectManager.readStressHrv(startTime, now)
            if (stress.isNotEmpty()) dao.insertStressHrv(stress)

            dao.insertSyncMeta(
                SyncMetaEntity(
                    category = "ALL",
                    lastSyncTime = now,
                    status = "SUCCESS"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            dao.insertSyncMeta(
                SyncMetaEntity(
                    category = "ALL",
                    lastSyncTime = now,
                    status = "ERROR",
                    errorMessage = e.message
                )
            )
            Result.failure(e)
        }
    }

    suspend fun syncMetric(metricType: MetricType, lookbackDays: Int = 30): Result<Unit> = withContext(Dispatchers.IO) {
        val now = Instant.now()
        val startTime = now.minus(lookbackDays.toLong(), ChronoUnit.DAYS)

        if (!healthConnectManager.isAvailable()) {
            return@withContext Result.failure(Exception("Health Connect is not available"))
        }

        return@withContext try {
            when (metricType) {
                MetricType.STEPS -> {
                    val list = healthConnectManager.readSteps(startTime, now)
                    if (list.isNotEmpty()) dao.insertSteps(list)
                }
                MetricType.CALORIES -> {
                    val list = healthConnectManager.readCalories(startTime, now)
                    if (list.isNotEmpty()) dao.insertCalories(list)
                }
                MetricType.HEART_RATE, MetricType.RESTING_HEART_RATE -> {
                    val list = healthConnectManager.readHeartRates(startTime, now)
                    if (list.isNotEmpty()) dao.insertHeartRates(list)
                }
                MetricType.OXYGEN_SATURATION -> {
                    val list = healthConnectManager.readOxygenSaturation(startTime, now)
                    if (list.isNotEmpty()) dao.insertOxygenRecords(list)
                }
                MetricType.SLEEP -> {
                    val list = healthConnectManager.readSleepSessions(startTime, now)
                    if (list.isNotEmpty()) dao.insertSleepSessions(list)
                }
                MetricType.WEIGHT, MetricType.BMI -> {
                    val list = healthConnectManager.readWeights(startTime, now)
                    if (list.isNotEmpty()) dao.insertWeights(list)
                }
                MetricType.BODY_FAT, MetricType.BODY_WATER, MetricType.MUSCLE_MASS, MetricType.BONE_MASS -> {
                    val list = healthConnectManager.readBodyComposition(startTime, now)
                    if (list.isNotEmpty()) dao.insertBodyCompositions(list)
                }
                MetricType.STRESS_HRV -> {
                    val list = healthConnectManager.readStressHrv(startTime, now)
                    if (list.isNotEmpty()) dao.insertStressHrv(list)
                }
            }

            dao.insertSyncMeta(
                SyncMetaEntity(
                    category = metricType.name,
                    lastSyncTime = now,
                    status = "SUCCESS"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== OBSERVABLE FLOWS ====================

    fun observeSteps(start: Instant, end: Instant): Flow<List<StepRecordEntity>> =
        dao.getStepsBetween(start, end)

    fun observeCalories(start: Instant, end: Instant): Flow<List<CalorieRecordEntity>> =
        dao.getCaloriesBetween(start, end)

    fun observeHeartRates(start: Instant, end: Instant): Flow<List<HeartRateRecordEntity>> =
        dao.getHeartRatesBetween(start, end)

    fun observeRestingHeartRates(start: Instant, end: Instant): Flow<List<HeartRateRecordEntity>> =
        dao.getRestingHeartRatesBetween(start, end)

    fun observeOxygen(start: Instant, end: Instant): Flow<List<OxygenSaturationRecordEntity>> =
        dao.getOxygenBetween(start, end)

    fun observeSleepSessions(start: Instant, end: Instant): Flow<List<SleepSessionEntity>> =
        dao.getSleepSessionsBetween(start, end)

    fun observeWeights(start: Instant, end: Instant): Flow<List<WeightRecordEntity>> =
        dao.getWeightsBetween(start, end)

    fun observeBodyCompositions(start: Instant, end: Instant): Flow<List<BodyCompositionEntity>> =
        dao.getBodyCompositionsBetween(start, end)

    fun observeStressHrv(start: Instant, end: Instant): Flow<List<StressHrvEntity>> =
        dao.getStressHrvBetween(start, end)

    fun observeLatestSyncTime(): Flow<Instant?> =
        dao.getLatestSuccessfulSyncTime()

    // Summary Flow for Dashboard with clean rounded values
    fun observeHealthSummary(): Flow<HealthSummary> {
        val zone = ZoneId.systemDefault()
        val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val endOfToday = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant()

        return combine(
            dao.getStepsBetween(startOfToday, endOfToday),
            dao.getCaloriesBetween(startOfToday, endOfToday),
            dao.getLatestHeartRate(),
            dao.getLatestRestingHeartRate(),
            dao.getLatestOxygenRecord(),
            dao.getLatestSleepSession(),
            dao.getLatestWeight(),
            dao.getLatestBodyComposition(),
            dao.getLatestStressHrv(),
            dao.getLatestSuccessfulSyncTime()
        ) { args: Array<Any?> ->
            @Suppress("UNCHECKED_CAST")
            val steps = args[0] as? List<StepRecordEntity> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val calories = args[1] as? List<CalorieRecordEntity> ?: emptyList()
            val latestHr = args[2] as? HeartRateRecordEntity
            val latestRhr = args[3] as? HeartRateRecordEntity
            val latestSpo2 = args[4] as? OxygenSaturationRecordEntity
            val latestSleep = args[5] as? SleepSessionEntity
            val latestWeight = args[6] as? WeightRecordEntity
            val latestBody = args[7] as? BodyCompositionEntity
            val latestStress = args[8] as? StressHrvEntity
            val lastSync = args[9] as? Instant

            val todayStepsCount = steps.sumOf { it.count }
            val todayCaloriesKcal = calories.sumOf { it.energyKcal }

            // Overall health score calculation (0-100)
            var score = 80
            if (todayStepsCount >= 10000) score += 5 else if (todayStepsCount < 3000) score -= 5
            latestSleep?.let {
                if (it.durationMinutes >= 420) score += 5 else if (it.durationMinutes < 330) score -= 8
            }
            latestStress?.let {
                if (it.stressScore < 40) score += 5 else if (it.stressScore > 70) score -= 8
            }
            latestSpo2?.let {
                if (it.percentage >= 97.0) score += 3 else if (it.percentage < 95.0) score -= 6
            }

            HealthSummary(
                todaySteps = todayStepsCount,
                todayCalories = FormatUtils.roundTo1Decimal(todayCaloriesKcal),
                currentHeartRate = latestHr?.bpm?.let { FormatUtils.roundTo1Decimal(it) },
                restingHeartRate = latestRhr?.bpm?.let { FormatUtils.roundTo1Decimal(it) },
                oxygenSaturation = latestSpo2?.percentage?.let { FormatUtils.roundTo1Decimal(it) },
                latestSleepDurationMinutes = latestSleep?.durationMinutes,
                latestSleepQualityScore = latestSleep?.efficiencyScore,
                latestWeight = latestWeight?.weightKg?.let { FormatUtils.roundTo1Decimal(it) },
                latestBmi = latestWeight?.bmi?.let { FormatUtils.roundTo1Decimal(it) },
                latestBodyFat = latestBody?.bodyFatPercentage?.let { FormatUtils.roundTo1Decimal(it) },
                latestBodyWater = latestBody?.bodyWaterPercentage?.let { FormatUtils.roundTo1Decimal(it) },
                latestMuscleMass = latestBody?.muscleMassKg?.let { FormatUtils.roundTo1Decimal(it) },
                latestStressScore = latestStress?.stressScore,
                latestHrvRmssd = latestStress?.rmssdMs?.let { FormatUtils.roundTo1Decimal(it) },
                overallHealthScore = score.coerceIn(40, 99),
                lastSyncTimestamp = lastSync
            )
        }
    }

    // App Settings
    fun observeAppSettings(): Flow<AppSettingsEntity> =
        dao.getAppSettings().map { it ?: AppSettingsEntity() }

    suspend fun updateAppSettings(settings: AppSettingsEntity) = withContext(Dispatchers.IO) {
        dao.insertAppSettings(settings)
    }

    // Dev / Demo helpers
    suspend fun seedSampleData(days: Int = 30) {
        generator.generateSampleData(days)
    }

    suspend fun clearAllData() {
        dao.clearAllData()
    }
}
