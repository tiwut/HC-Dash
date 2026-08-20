package com.hcdash.app

import android.app.Application
import com.hcdash.app.data.healthconnect.HealthConnectManager
import com.hcdash.app.data.local.HealthDatabase
import com.hcdash.app.data.repository.HealthRepository
import com.hcdash.app.data.sync.HealthSyncManager

class HCDashApplication : Application() {

    lateinit var database: HealthDatabase
        private set

    lateinit var healthConnectManager: HealthConnectManager
        private set

    lateinit var repository: HealthRepository
        private set

    lateinit var syncManager: HealthSyncManager
        private set

    override fun onCreate() {
        super.onCreate()

        database = HealthDatabase.getDatabase(this)
        healthConnectManager = HealthConnectManager(this)
        repository = HealthRepository(
            healthConnectManager = healthConnectManager,
            dao = database.healthMetricsDao()
        )
        syncManager = HealthSyncManager(repository = repository)

        // Trigger automatic sync on application start!
        syncManager.onAppStart()
    }
}
