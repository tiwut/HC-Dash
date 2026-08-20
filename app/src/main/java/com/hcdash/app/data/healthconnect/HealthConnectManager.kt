package com.hcdash.app.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hcdash.app.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.util.UUID

enum class HealthConnectAvailability {
    AVAILABLE,
    NOT_INSTALLED,
    NOT_SUPPORTED
}

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient: HealthConnectClient? by lazy {
        try {
            if (isAvailable()) {
                HealthConnectClient.getOrCreate(context)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun checkAvailability(): HealthConnectAvailability {
        return try {
            val status = HealthConnectClient.getSdkStatus(context)
            when (status) {
                HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.NOT_INSTALLED
                else -> HealthConnectAvailability.NOT_SUPPORTED
            }
        } catch (e: Exception) {
            HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    fun isAvailable(): Boolean {
        return checkAvailability() == HealthConnectAvailability.AVAILABLE
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            HealthConnectPermissions.READ_PERMISSIONS.all { it in granted }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getGrantedPermissions(): Set<String> {
        val client = healthConnectClient ?: return emptySet()
        return try {
            client.permissionController.getGrantedPermissions()
        } catch (e: Exception) {
            emptySet()
        }
    }

    // ==================== READ STEPS ====================
    suspend fun readSteps(startTime: Instant, endTime: Instant): List<StepRecordEntity> =
        withContext(Dispatchers.IO) {
            val client = healthConnectClient ?: return@withContext emptyList()
            try {
                val request = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val response = client.readRecords(request)
                response.records.map { record ->
                    StepRecordEntity(
                        id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                        startTime = record.startTime,
                        endTime = record.endTime,
                        count = record.count.coerceAtLeast(0L),
                        sourceApp = record.metadata.dataOrigin.packageName
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    // ==================== READ CALORIES ====================
    suspend fun readCalories(startTime: Instant, endTime: Instant): List<CalorieRecordEntity> =
        withContext(Dispatchers.IO) {
            val client = healthConnectClient ?: return@withContext emptyList()
            val list = mutableListOf<CalorieRecordEntity>()
            try {
                // Active Calories
                val activeRequest = ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val activeResponse = client.readRecords(activeRequest)
                activeResponse.records.forEach { record ->
                    val kcal = record.energy.inKilocalories
                    if (!kcal.isNaN() && !kcal.isInfinite() && kcal >= 0.0) {
                        list.add(
                            CalorieRecordEntity(
                                id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                                startTime = record.startTime,
                                endTime = record.endTime,
                                energyKcal = kcal,
                                isBasal = false,
                                sourceApp = record.metadata.dataOrigin.packageName
                            )
                        )
                    }
                }

                // Total / Basal Calories
                val totalRequest = ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val totalResponse = client.readRecords(totalRequest)
                totalResponse.records.forEach { record ->
                    val kcal = record.energy.inKilocalories
                    if (!kcal.isNaN() && !kcal.isInfinite() && kcal >= 0.0) {
                        list.add(
                            CalorieRecordEntity(
                                id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                                startTime = record.startTime,
                                endTime = record.endTime,
                                energyKcal = kcal,
                                isBasal = true,
                                sourceApp = record.metadata.dataOrigin.packageName
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Log or ignore
            }
            list
        }

    // ==================== READ HEART RATE ====================
    suspend fun readHeartRates(startTime: Instant, endTime: Instant): List<HeartRateRecordEntity> =
        withContext(Dispatchers.IO) {
            val client = healthConnectClient ?: return@withContext emptyList()
            val list = mutableListOf<HeartRateRecordEntity>()
            try {
                // Continuous / sampled heart rate
                val hrRequest = ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val hrResponse = client.readRecords(hrRequest)
                hrResponse.records.forEach { record ->
                    record.samples.forEachIndexed { index, sample ->
                        val bpm = sample.beatsPerMinute.toDouble()
                        if (bpm in 30.0..250.0) {
                            list.add(
                                HeartRateRecordEntity(
                                    id = "${record.metadata.id}_$index",
                                    time = sample.time,
                                    bpm = bpm,
                                    isResting = false,
                                    sourceApp = record.metadata.dataOrigin.packageName
                                )
                            )
                        }
                    }
                }

                // Resting Heart Rate
                val restingRequest = ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val restingResponse = client.readRecords(restingRequest)
                restingResponse.records.forEach { record ->
                    val bpm = record.beatsPerMinute.toDouble()
                    if (bpm in 30.0..220.0) {
                        list.add(
                            HeartRateRecordEntity(
                                id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                                time = record.time,
                                bpm = bpm,
                                isResting = true,
                                sourceApp = record.metadata.dataOrigin.packageName
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Log or ignore
            }
            list
        }

    // ==================== READ OXYGEN SATURATION ====================
    suspend fun readOxygenSaturation(startTime: Instant, endTime: Instant): List<OxygenSaturationRecordEntity> =
        withContext(Dispatchers.IO) {
            val client = healthConnectClient ?: return@withContext emptyList()
            try {
                val request = ReadRecordsRequest(
                    recordType = OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val response = client.readRecords(request)
                response.records.mapNotNull { record ->
                    val pct = record.percentage.value
                    if (pct in 50.0..100.0) {
                        OxygenSaturationRecordEntity(
                            id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                            time = record.time,
                            percentage = pct,
                            sourceApp = record.metadata.dataOrigin.packageName
                        )
                    } else null
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    // ==================== READ SLEEP SESSIONS ====================
    suspend fun readSleepSessions(startTime: Instant, endTime: Instant): List<SleepSessionEntity> =
        withContext(Dispatchers.IO) {
            val client = healthConnectClient ?: return@withContext emptyList()
            try {
                val request = ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val response = client.readRecords(request)
                response.records.map { record ->
                    val durationMin = Duration.between(record.startTime, record.endTime).toMinutes().coerceAtLeast(0L)
                    var deep = 0L
                    var light = 0L
                    var rem = 0L
                    var awake = 0L

                    record.stages.forEach { stage ->
                        val stageMin = Duration.between(stage.startTime, stage.endTime).toMinutes().coerceAtLeast(0L)
                        when (stage.stage) {
                            SleepSessionRecord.STAGE_TYPE_DEEP -> deep += stageMin
                            SleepSessionRecord.STAGE_TYPE_LIGHT -> light += stageMin
                            SleepSessionRecord.STAGE_TYPE_REM -> rem += stageMin
                            SleepSessionRecord.STAGE_TYPE_AWAKE,
                            SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> awake += stageMin
                            else -> light += stageMin
                        }
                    }

                    // Calculate score
                    val score = when {
                        durationMin >= 420 && deep >= 60 -> 90
                        durationMin >= 360 -> 78
                        durationMin >= 300 -> 65
                        else -> 50
                    }

                    SleepSessionEntity(
                        id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                        startTime = record.startTime,
                        endTime = record.endTime,
                        durationMinutes = durationMin,
                        deepMinutes = if (deep > 0) deep else (durationMin * 0.20).toLong(),
                        lightMinutes = if (light > 0) light else (durationMin * 0.50).toLong(),
                        remMinutes = if (rem > 0) rem else (durationMin * 0.22).toLong(),
                        awakeMinutes = if (awake > 0) awake else (durationMin * 0.08).toLong(),
                        efficiencyScore = score,
                        title = record.title,
                        sourceApp = record.metadata.dataOrigin.packageName
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    // ==================== READ WEIGHT & BODY ====================
    suspend fun readWeights(startTime: Instant, endTime: Instant): List<WeightRecordEntity> =
        withContext(Dispatchers.IO) {
            val client = healthConnectClient ?: return@withContext emptyList()
            try {
                val weightRequest = ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val weightResponse = client.readRecords(weightRequest)

                // Optional height query for BMI calculation
                val heightRequest = ReadRecordsRequest(
                    recordType = HeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(Instant.EPOCH, Instant.now())
                )
                val userHeightMeters = try {
                    client.readRecords(heightRequest).records.lastOrNull()?.height?.inMeters ?: 1.75
                } catch (e: Exception) {
                    1.75
                }

                weightResponse.records.mapNotNull { record ->
                    val weightKg = record.weight.inKilograms
                    if (weightKg in 20.0..350.0) {
                        val bmi = if (userHeightMeters > 0) weightKg / (userHeightMeters * userHeightMeters) else null
                        WeightRecordEntity(
                            id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                            time = record.time,
                            weightKg = weightKg,
                            bmi = bmi,
                            sourceApp = record.metadata.dataOrigin.packageName
                        )
                    } else null
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    // ==================== READ BODY COMPOSITION ====================
    suspend fun readBodyComposition(startTime: Instant, endTime: Instant): List<BodyCompositionEntity> =
        withContext(Dispatchers.IO) {
            val client = healthConnectClient ?: return@withContext emptyList()
            val list = mutableListOf<BodyCompositionEntity>()
            try {
                val fatRequest = ReadRecordsRequest(
                    recordType = BodyFatRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val fatResponse = client.readRecords(fatRequest)

                val waterRequest = ReadRecordsRequest(
                    recordType = BodyWaterMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val waterResponse = client.readRecords(waterRequest)

                val boneRequest = ReadRecordsRequest(
                    recordType = BoneMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val boneResponse = client.readRecords(boneRequest)

                val muscleRequest = ReadRecordsRequest(
                    recordType = LeanBodyMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val muscleResponse = client.readRecords(muscleRequest)

                fatResponse.records.forEach { fatRec ->
                    val time = fatRec.time
                    val water = waterResponse.records.firstOrNull { Duration.between(it.time, time).abs().toMinutes() < 60 }
                    val bone = boneResponse.records.firstOrNull { Duration.between(it.time, time).abs().toMinutes() < 60 }
                    val muscle = muscleResponse.records.firstOrNull { Duration.between(it.time, time).abs().toMinutes() < 60 }

                    val waterPct: Double? = water?.mass?.inKilograms?.let { (it / 75.0) * 100.0 }

                    list.add(
                        BodyCompositionEntity(
                            id = fatRec.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                            time = time,
                            bodyFatPercentage = fatRec.percentage.value,
                            bodyWaterPercentage = waterPct,
                            muscleMassKg = muscle?.mass?.inKilograms,
                            boneMassKg = bone?.mass?.inKilograms,
                            sourceApp = fatRec.metadata.dataOrigin.packageName
                        )
                    )
                }
            } catch (e: Exception) {
                // Log or ignore
            }
            list
        }

    // ==================== READ STRESS & HRV ====================
    suspend fun readStressHrv(startTime: Instant, endTime: Instant): List<StressHrvEntity> =
        withContext(Dispatchers.IO) {
            val client = healthConnectClient ?: return@withContext emptyList()
            try {
                val request = ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val response = client.readRecords(request)
                response.records.mapNotNull { record ->
                    val rmssd = record.heartRateVariabilityMillis
                    if (rmssd in 5.0..300.0) {
                        val stress = (100.0 - (rmssd * 1.2)).coerceIn(10.0, 95.0).toInt()
                        StressHrvEntity(
                            id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                            time = record.time,
                            rmssdMs = rmssd,
                            stressScore = stress,
                            sourceApp = record.metadata.dataOrigin.packageName
                        )
                    } else null
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
}
