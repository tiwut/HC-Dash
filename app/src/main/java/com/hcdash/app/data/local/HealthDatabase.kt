package com.hcdash.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hcdash.app.data.local.dao.HealthMetricsDao
import com.hcdash.app.data.local.entities.*

@Database(
    entities = [
        StepRecordEntity::class,
        CalorieRecordEntity::class,
        HeartRateRecordEntity::class,
        OxygenSaturationRecordEntity::class,
        SleepSessionEntity::class,
        WeightRecordEntity::class,
        BodyCompositionEntity::class,
        StressHrvEntity::class,
        SyncMetaEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HealthDatabase : RoomDatabase() {

    abstract fun healthMetricsDao(): HealthMetricsDao

    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        fun getDatabase(context: Context): HealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "hcdash_health_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
