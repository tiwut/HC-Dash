package com.hcdash.app.data.healthconnect

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import kotlin.reflect.KClass

object HealthConnectPermissions {

    val REQUIRED_RECORD_TYPES: Set<KClass<out Record>> = setOf(
        StepsRecord::class,
        TotalCaloriesBurnedRecord::class,
        ActiveCaloriesBurnedRecord::class,
        HeartRateRecord::class,
        RestingHeartRateRecord::class,
        OxygenSaturationRecord::class,
        SleepSessionRecord::class,
        WeightRecord::class,
        HeightRecord::class,
        BodyFatRecord::class,
        BodyWaterMassRecord::class,
        BoneMassRecord::class,
        LeanBodyMassRecord::class,
        HeartRateVariabilityRmssdRecord::class
    )

    val READ_PERMISSIONS: Set<String> = REQUIRED_RECORD_TYPES.map {
        HealthPermission.getReadPermission(it)
    }.toSet()

    val ALL_PERMISSIONS: Set<String> = READ_PERMISSIONS
}
