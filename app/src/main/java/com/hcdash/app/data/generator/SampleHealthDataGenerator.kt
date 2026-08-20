package com.hcdash.app.data.generator

import com.hcdash.app.data.local.dao.HealthMetricsDao
import com.hcdash.app.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random

class SampleHealthDataGenerator(private val dao: HealthMetricsDao) {

    suspend fun generateSampleData(days: Int = 30) = withContext(Dispatchers.IO) {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val stepsList = mutableListOf<StepRecordEntity>()
        val caloriesList = mutableListOf<CalorieRecordEntity>()
        val heartRatesList = mutableListOf<HeartRateRecordEntity>()
        val oxygenList = mutableListOf<OxygenSaturationRecordEntity>()
        val sleepList = mutableListOf<SleepSessionEntity>()
        val weightsList = mutableListOf<WeightRecordEntity>()
        val bodyCompositionsList = mutableListOf<BodyCompositionEntity>()
        val stressList = mutableListOf<StressHrvEntity>()

        var runningWeight = 76.5
        var runningFat = 17.5
        var runningMuscle = 59.8

        for (dayOffset in (days - 1) downTo 0) {
            val date = today.minusDays(dayOffset.toLong())
            val startOfDay = date.atStartOfDay(zone).toInstant()
            val endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant()

            // --- 1. SLEEP (Previous night 23:00 to 07:00 approx) ---
            val sleepStart = date.atTime(22, 30 + Random.nextInt(45)).atZone(zone).toInstant()
            val sleepDurationMins = 380L + Random.nextLong(140) // 6.3 to 8.6 hours
            val sleepEnd = sleepStart.plus(sleepDurationMins, ChronoUnit.MINUTES)

            val deepMins = (sleepDurationMins * (0.18 + Random.nextDouble(0.08))).toLong()
            val remMins = (sleepDurationMins * (0.20 + Random.nextDouble(0.07))).toLong()
            val awakeMins = 15L + Random.nextLong(25)
            val lightMins = sleepDurationMins - deepMins - remMins - awakeMins
            val sleepScore = ((deepMins * 1.5 + remMins * 1.2 + (sleepDurationMins / 6.0)) / 4.5).toInt().coerceIn(62, 96)

            sleepList.add(
                SleepSessionEntity(
                    id = "sample_sleep_${date}",
                    startTime = sleepStart,
                    endTime = sleepEnd,
                    durationMinutes = sleepDurationMins,
                    deepMinutes = deepMins,
                    lightMinutes = lightMins,
                    remMinutes = remMins,
                    awakeMinutes = awakeMins,
                    efficiencyScore = sleepScore,
                    title = "Night Sleep",
                    sourceApp = "com.sample.health"
                )
            )

            // --- 2. STEPS & HOURLY ACTIVITY ---
            val isWeekend = date.dayOfWeek.value >= 6
            val targetSteps = if (isWeekend) 11000 + Random.nextInt(4000) else 7500 + Random.nextInt(4500)
            var dayStepsAccum = 0L

            for (hour in 7..21) {
                val hourStart = date.atTime(hour, 0).atZone(zone).toInstant()
                val hourEnd = date.atTime(hour, 59).atZone(zone).toInstant()

                val hourlyFraction = when (hour) {
                    8, 9 -> 0.15 // Morning commute/walk
                    12, 13 -> 0.12 // Lunch walk
                    17, 18, 19 -> 0.35 // Evening workout
                    else -> 0.05
                }

                val hourSteps = (targetSteps * hourlyFraction * (0.7 + Random.nextDouble(0.6))).toLong()
                dayStepsAccum += hourSteps

                stepsList.add(
                    StepRecordEntity(
                        id = "sample_step_${date}_h$hour",
                        startTime = hourStart,
                        endTime = hourEnd,
                        count = hourSteps,
                        sourceApp = "com.sample.health"
                    )
                )

                // Calories for this hour
                val activeKcal = hourSteps * 0.042 + (if (hour in 17..19) 150.0 else 10.0)
                caloriesList.add(
                    CalorieRecordEntity(
                        id = "sample_cal_act_${date}_h$hour",
                        startTime = hourStart,
                        endTime = hourEnd,
                        energyKcal = activeKcal,
                        isBasal = false,
                        sourceApp = "com.sample.health"
                    )
                )
            }

            // Basal Metabolic Rate calories for the day
            caloriesList.add(
                CalorieRecordEntity(
                    id = "sample_cal_bmr_${date}",
                    startTime = startOfDay,
                    endTime = endOfDay,
                    energyKcal = 1650.0 + Random.nextDouble(50.0),
                    isBasal = true,
                    sourceApp = "com.sample.health"
                )
            )

            // --- 3. HEART RATE & RESTING HEART RATE ---
            val restingHr = 58.0 + Random.nextDouble(8.0)
            heartRatesList.add(
                HeartRateRecordEntity(
                    id = "sample_rhr_${date}",
                    time = date.atTime(6, 30).atZone(zone).toInstant(),
                    bpm = restingHr,
                    isResting = true,
                    sourceApp = "com.sample.health"
                )
            )

            // Sampled heart rates during the day
            for (hour in listOf(7, 9, 12, 15, 18, 21, 23)) {
                val bpm = when (hour) {
                    18 -> 135.0 + Random.nextDouble(30.0) // workout
                    7, 23 -> 59.0 + Random.nextDouble(6.0) // resting
                    else -> 72.0 + Random.nextDouble(16.0) // active
                }
                heartRatesList.add(
                    HeartRateRecordEntity(
                        id = "sample_hr_${date}_h$hour",
                        time = date.atTime(hour, Random.nextInt(50)).atZone(zone).toInstant(),
                        bpm = bpm,
                        isResting = false,
                        sourceApp = "com.sample.health"
                    )
                )
            }

            // --- 4. BLOOD OXYGEN (SpO2) ---
            for (hour in listOf(8, 14, 20, 3)) {
                val spo2 = 96.5 + Random.nextDouble(3.2) // 96.5% - 99.7%
                oxygenList.add(
                    OxygenSaturationRecordEntity(
                        id = "sample_spo2_${date}_h$hour",
                        time = date.atTime(hour, 15).atZone(zone).toInstant(),
                        percentage = (spo2 * 10.0).toInt() / 10.0,
                        sourceApp = "com.sample.health"
                    )
                )
            }

            // --- 5. WEIGHT & BODY COMPOSITION (measured every 1-2 days) ---
            if (dayOffset % 2 == 0 || dayOffset == 0) {
                runningWeight += (Random.nextDouble() - 0.52) * 0.25 // slight downward trend
                runningFat += (Random.nextDouble() - 0.53) * 0.08
                runningMuscle += (Random.nextDouble() - 0.48) * 0.05
                val heightM = 1.78
                val bmi = runningWeight / (heightM * heightM)

                val morningTime = date.atTime(7, 10).atZone(zone).toInstant()

                weightsList.add(
                    WeightRecordEntity(
                        id = "sample_weight_${date}",
                        time = morningTime,
                        weightKg = (runningWeight * 10.0).toInt() / 10.0,
                        bmi = (bmi * 10.0).toInt() / 10.0,
                        sourceApp = "com.sample.health"
                    )
                )

                bodyCompositionsList.add(
                    BodyCompositionEntity(
                        id = "sample_body_${date}",
                        time = morningTime,
                        bodyFatPercentage = (runningFat * 10.0).toInt() / 10.0,
                        bodyWaterPercentage = 58.5 + Random.nextDouble(2.0),
                        muscleMassKg = (runningMuscle * 10.0).toInt() / 10.0,
                        boneMassKg = 3.25 + Random.nextDouble(0.1),
                        sourceApp = "com.sample.health"
                    )
                )
            }

            // --- 6. STRESS & HRV ---
            for (hour in listOf(8, 11, 14, 17, 20, 22)) {
                val baseHrv = if (isWeekend) 58.0 else 46.0
                val rmssd = (baseHrv + (Random.nextDouble() - 0.4) * 22.0).coerceIn(28.0, 85.0)
                val stressScore = (100.0 - (rmssd * 1.15) + (if (hour in 14..17) 12.0 else -5.0))
                    .coerceIn(12.0, 92.0).toInt()

                stressList.add(
                    StressHrvEntity(
                        id = "sample_stress_${date}_h$hour",
                        time = date.atTime(hour, 30).atZone(zone).toInstant(),
                        rmssdMs = (rmssd * 10.0).toInt() / 10.0,
                        stressScore = stressScore,
                        sourceApp = "com.sample.health"
                    )
                )
            }
        }

        // Insert into Room
        dao.insertSteps(stepsList)
        dao.insertCalories(caloriesList)
        dao.insertHeartRates(heartRatesList)
        dao.insertOxygenRecords(oxygenList)
        dao.insertSleepSessions(sleepList)
        dao.insertWeights(weightsList)
        dao.insertBodyCompositions(bodyCompositionsList)
        dao.insertStressHrv(stressList)

        // Metadata
        dao.insertSyncMeta(
            SyncMetaEntity(
                category = "ALL",
                lastSyncTime = now,
                status = "SUCCESS",
                errorMessage = null
            )
        )
    }
}
