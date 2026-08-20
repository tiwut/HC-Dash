package com.hcdash.app.data.local.dao

import androidx.room.*
import com.hcdash.app.data.local.entities.*
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface HealthMetricsDao {

    // ==================== STEPS ====================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<StepRecordEntity>)

    @Query("SELECT * FROM step_records WHERE startTime >= :start AND endTime <= :end ORDER BY startTime ASC")
    fun getStepsBetween(start: Instant, end: Instant): Flow<List<StepRecordEntity>>

    @Query("SELECT * FROM step_records WHERE startTime >= :start AND endTime <= :end ORDER BY startTime ASC")
    suspend fun getStepsBetweenSync(start: Instant, end: Instant): List<StepRecordEntity>

    @Query("SELECT COALESCE(SUM(count), 0) FROM step_records WHERE startTime >= :start AND endTime <= :end")
    fun getTotalStepsBetween(start: Instant, end: Instant): Flow<Long>

    @Query("SELECT COALESCE(SUM(count), 0) FROM step_records WHERE startTime >= :start AND endTime <= :end")
    suspend fun getTotalStepsBetweenSync(start: Instant, end: Instant): Long

    // ==================== CALORIES ====================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalories(calories: List<CalorieRecordEntity>)

    @Query("SELECT * FROM calorie_records WHERE startTime >= :start AND endTime <= :end ORDER BY startTime ASC")
    fun getCaloriesBetween(start: Instant, end: Instant): Flow<List<CalorieRecordEntity>>

    @Query("SELECT * FROM calorie_records WHERE startTime >= :start AND endTime <= :end ORDER BY startTime ASC")
    suspend fun getCaloriesBetweenSync(start: Instant, end: Instant): List<CalorieRecordEntity>

    @Query("SELECT COALESCE(SUM(energyKcal), 0.0) FROM calorie_records WHERE startTime >= :start AND endTime <= :end AND isBasal = 0")
    fun getActiveCaloriesBetween(start: Instant, end: Instant): Flow<Double>

    @Query("SELECT COALESCE(SUM(energyKcal), 0.0) FROM calorie_records WHERE startTime >= :start AND endTime <= :end")
    suspend fun getTotalCaloriesBetweenSync(start: Instant, end: Instant): Double

    // ==================== HEART RATE ====================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeartRates(records: List<HeartRateRecordEntity>)

    @Query("SELECT * FROM heart_rate_records WHERE time >= :start AND time <= :end ORDER BY time ASC")
    fun getHeartRatesBetween(start: Instant, end: Instant): Flow<List<HeartRateRecordEntity>>

    @Query("SELECT * FROM heart_rate_records WHERE time >= :start AND time <= :end ORDER BY time ASC")
    suspend fun getHeartRatesBetweenSync(start: Instant, end: Instant): List<HeartRateRecordEntity>

    @Query("SELECT * FROM heart_rate_records WHERE isResting = 1 AND time >= :start AND time <= :end ORDER BY time ASC")
    fun getRestingHeartRatesBetween(start: Instant, end: Instant): Flow<List<HeartRateRecordEntity>>

    @Query("SELECT * FROM heart_rate_records ORDER BY time DESC LIMIT 1")
    fun getLatestHeartRate(): Flow<HeartRateRecordEntity?>

    @Query("SELECT * FROM heart_rate_records WHERE isResting = 1 ORDER BY time DESC LIMIT 1")
    fun getLatestRestingHeartRate(): Flow<HeartRateRecordEntity?>

    @Query("SELECT AVG(bpm) FROM heart_rate_records WHERE time >= :start AND time <= :end")
    suspend fun getAverageHeartRate(start: Instant, end: Instant): Double?

    @Query("SELECT MIN(bpm) FROM heart_rate_records WHERE time >= :start AND time <= :end")
    suspend fun getMinHeartRate(start: Instant, end: Instant): Double?

    @Query("SELECT MAX(bpm) FROM heart_rate_records WHERE time >= :start AND time <= :end")
    suspend fun getMaxHeartRate(start: Instant, end: Instant): Double?

    // ==================== OXYGEN SATURATION ====================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOxygenRecords(records: List<OxygenSaturationRecordEntity>)

    @Query("SELECT * FROM oxygen_saturation_records WHERE time >= :start AND time <= :end ORDER BY time ASC")
    fun getOxygenBetween(start: Instant, end: Instant): Flow<List<OxygenSaturationRecordEntity>>

    @Query("SELECT * FROM oxygen_saturation_records WHERE time >= :start AND time <= :end ORDER BY time ASC")
    suspend fun getOxygenBetweenSync(start: Instant, end: Instant): List<OxygenSaturationRecordEntity>

    @Query("SELECT * FROM oxygen_saturation_records ORDER BY time DESC LIMIT 1")
    fun getLatestOxygenRecord(): Flow<OxygenSaturationRecordEntity?>

    @Query("SELECT AVG(percentage) FROM oxygen_saturation_records WHERE time >= :start AND time <= :end")
    suspend fun getAverageOxygen(start: Instant, end: Instant): Double?

    @Query("SELECT MIN(percentage) FROM oxygen_saturation_records WHERE time >= :start AND time <= :end")
    suspend fun getMinOxygen(start: Instant, end: Instant): Double?

    // ==================== SLEEP ====================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSessions(records: List<SleepSessionEntity>)

    @Query("SELECT * FROM sleep_sessions WHERE startTime >= :start AND endTime <= :end ORDER BY startTime ASC")
    fun getSleepSessionsBetween(start: Instant, end: Instant): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE startTime >= :start AND endTime <= :end ORDER BY startTime ASC")
    suspend fun getSleepSessionsBetweenSync(start: Instant, end: Instant): List<SleepSessionEntity>

    @Query("SELECT * FROM sleep_sessions ORDER BY endTime DESC LIMIT 1")
    fun getLatestSleepSession(): Flow<SleepSessionEntity?>

    @Query("SELECT AVG(durationMinutes) FROM sleep_sessions WHERE startTime >= :start AND endTime <= :end")
    suspend fun getAverageSleepDurationMinutes(start: Instant, end: Instant): Double?

    // ==================== WEIGHT ====================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeights(records: List<WeightRecordEntity>)

    @Query("SELECT * FROM weight_records WHERE time >= :start AND time <= :end ORDER BY time ASC")
    fun getWeightsBetween(start: Instant, end: Instant): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records WHERE time >= :start AND time <= :end ORDER BY time ASC")
    suspend fun getWeightsBetweenSync(start: Instant, end: Instant): List<WeightRecordEntity>

    @Query("SELECT * FROM weight_records ORDER BY time DESC LIMIT 1")
    fun getLatestWeight(): Flow<WeightRecordEntity?>

    @Query("SELECT MIN(weightKg) FROM weight_records WHERE time >= :start AND time <= :end")
    suspend fun getMinWeight(start: Instant, end: Instant): Double?

    @Query("SELECT MAX(weightKg) FROM weight_records WHERE time >= :start AND time <= :end")
    suspend fun getMaxWeight(start: Instant, end: Instant): Double?

    // ==================== BODY COMPOSITION ====================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyCompositions(records: List<BodyCompositionEntity>)

    @Query("SELECT * FROM body_composition_records WHERE time >= :start AND time <= :end ORDER BY time ASC")
    fun getBodyCompositionsBetween(start: Instant, end: Instant): Flow<List<BodyCompositionEntity>>

    @Query("SELECT * FROM body_composition_records WHERE time >= :start AND time <= :end ORDER BY time ASC")
    suspend fun getBodyCompositionsBetweenSync(start: Instant, end: Instant): List<BodyCompositionEntity>

    @Query("SELECT * FROM body_composition_records ORDER BY time DESC LIMIT 1")
    fun getLatestBodyComposition(): Flow<BodyCompositionEntity?>

    // ==================== STRESS & HRV ====================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStressHrv(records: List<StressHrvEntity>)

    @Query("SELECT * FROM stress_hrv_records WHERE time >= :start AND time <= :end ORDER BY time ASC")
    fun getStressHrvBetween(start: Instant, end: Instant): Flow<List<StressHrvEntity>>

    @Query("SELECT * FROM stress_hrv_records WHERE time >= :start AND time <= :end ORDER BY time ASC")
    suspend fun getStressHrvBetweenSync(start: Instant, end: Instant): List<StressHrvEntity>

    @Query("SELECT * FROM stress_hrv_records ORDER BY time DESC LIMIT 1")
    fun getLatestStressHrv(): Flow<StressHrvEntity?>

    @Query("SELECT AVG(stressScore) FROM stress_hrv_records WHERE time >= :start AND time <= :end")
    suspend fun getAverageStressScore(start: Instant, end: Instant): Double?

    @Query("SELECT AVG(rmssdMs) FROM stress_hrv_records WHERE time >= :start AND time <= :end")
    suspend fun getAverageHrvRmssd(start: Instant, end: Instant): Double?

    // ==================== SYNC METADATA ====================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncMeta(meta: SyncMetaEntity)

    @Query("SELECT * FROM sync_metadata WHERE category = :category")
    suspend fun getSyncMeta(category: String): SyncMetaEntity?

    @Query("SELECT * FROM sync_metadata")
    fun getAllSyncMeta(): Flow<List<SyncMetaEntity>>

    @Query("SELECT MAX(lastSyncTime) FROM sync_metadata WHERE status = 'SUCCESS'")
    fun getLatestSuccessfulSyncTime(): Flow<Instant?>

    // ==================== APP SETTINGS & GOALS ====================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSettings(settings: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getAppSettingsSync(): AppSettingsEntity?

    // ==================== DATA MANAGEMENT ====================
    @Query("DELETE FROM step_records")
    suspend fun clearSteps()

    @Query("DELETE FROM calorie_records")
    suspend fun clearCalories()

    @Query("DELETE FROM heart_rate_records")
    suspend fun clearHeartRates()

    @Query("DELETE FROM oxygen_saturation_records")
    suspend fun clearOxygen()

    @Query("DELETE FROM sleep_sessions")
    suspend fun clearSleep()

    @Query("DELETE FROM weight_records")
    suspend fun clearWeights()

    @Query("DELETE FROM body_composition_records")
    suspend fun clearBodyComposition()

    @Query("DELETE FROM stress_hrv_records")
    suspend fun clearStressHrv()

    @Query("DELETE FROM sync_metadata")
    suspend fun clearSyncMetadata()

    @Transaction
    suspend fun clearAllData() {
        clearSteps()
        clearCalories()
        clearHeartRates()
        clearOxygen()
        clearSleep()
        clearWeights()
        clearBodyComposition()
        clearStressHrv()
        clearSyncMetadata()
    }
}
