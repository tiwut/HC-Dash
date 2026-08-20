package com.hcdash.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.health.connect.client.PermissionController
import androidx.navigation.compose.rememberNavController
import com.hcdash.app.data.healthconnect.HealthConnectPermissions
import com.hcdash.app.ui.navigation.AppNavigation
import com.hcdash.app.ui.screens.activity.ActivityViewModel
import com.hcdash.app.ui.screens.body.BodyCompositionViewModel
import com.hcdash.app.ui.screens.dashboard.DashboardViewModel
import com.hcdash.app.ui.screens.detail.DetailViewModel
import com.hcdash.app.ui.screens.settings.SettingsViewModel
import com.hcdash.app.ui.screens.sleep.SleepViewModel
import com.hcdash.app.ui.screens.stress.StressViewModel
import com.hcdash.app.ui.screens.vitals.VitalsViewModel
import com.hcdash.app.ui.theme.HCDashTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HCDashApplication
        val repository = app.repository
        val syncManager = app.syncManager

        // Instantiate ViewModels
        val dashboardViewModel = DashboardViewModel(repository, syncManager)
        val activityViewModel = ActivityViewModel(repository, syncManager)
        val vitalsViewModel = VitalsViewModel(repository, syncManager)
        val sleepViewModel = SleepViewModel(repository, syncManager)
        val bodyCompositionViewModel = BodyCompositionViewModel(repository, syncManager)
        val stressViewModel = StressViewModel(repository, syncManager)
        val detailViewModel = DetailViewModel(repository, syncManager)
        val settingsViewModel = SettingsViewModel(repository, syncManager)

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsState()

            HCDashTheme(
                themeMode = settingsState.settings.themeMode,
                dynamicColor = settingsState.settings.dynamicColor
            ) {
                val navController = rememberNavController()

                // Health Connect Permissions Launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = PermissionController.createRequestPermissionResultContract()
                ) { grantedPermissions ->
                    settingsViewModel.refreshStatus()
                    if (grantedPermissions.isNotEmpty()) {
                        syncManager.onAppStart()
                    }
                }

                AppNavigation(
                    navController = navController,
                    syncManager = syncManager,
                    dashboardViewModel = dashboardViewModel,
                    activityViewModel = activityViewModel,
                    vitalsViewModel = vitalsViewModel,
                    sleepViewModel = sleepViewModel,
                    bodyCompositionViewModel = bodyCompositionViewModel,
                    stressViewModel = stressViewModel,
                    detailViewModel = detailViewModel,
                    settingsViewModel = settingsViewModel,
                    onRequestPermissions = {
                        permissionLauncher.launch(HealthConnectPermissions.READ_PERMISSIONS)
                    }
                )
            }
        }
    }
}
