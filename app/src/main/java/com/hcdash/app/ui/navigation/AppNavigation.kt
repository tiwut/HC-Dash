package com.hcdash.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.hcdash.app.data.sync.HealthSyncManager
import com.hcdash.app.domain.model.MetricType
import com.hcdash.app.ui.components.AppIcons
import com.hcdash.app.ui.components.SyncStatusIndicator
import com.hcdash.app.ui.screens.activity.ActivityScreen
import com.hcdash.app.ui.screens.activity.ActivityViewModel
import com.hcdash.app.ui.screens.body.BodyCompositionViewModel
import com.hcdash.app.ui.screens.body_mind.BodyMindScreen
import com.hcdash.app.ui.screens.dashboard.DashboardScreen
import com.hcdash.app.ui.screens.dashboard.DashboardViewModel
import com.hcdash.app.ui.screens.detail.DetailOverviewScreen
import com.hcdash.app.ui.screens.detail.DetailViewModel
import com.hcdash.app.ui.screens.settings.SettingsScreen
import com.hcdash.app.ui.screens.settings.SettingsViewModel
import com.hcdash.app.ui.screens.sleep.SleepViewModel
import com.hcdash.app.ui.screens.stress.StressViewModel
import com.hcdash.app.ui.screens.vitals.VitalsViewModel
import com.hcdash.app.ui.screens.vitals_sleep.VitalsSleepScreen
import com.hcdash.app.ui.theme.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    syncManager: HealthSyncManager,
    dashboardViewModel: DashboardViewModel,
    activityViewModel: ActivityViewModel,
    vitalsViewModel: VitalsViewModel,
    sleepViewModel: SleepViewModel,
    bodyCompositionViewModel: BodyCompositionViewModel,
    stressViewModel: StressViewModel,
    detailViewModel: DetailViewModel,
    settingsViewModel: SettingsViewModel,
    onRequestPermissions: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val syncState by syncManager.syncState.collectAsState()

    // Listen to layer navigation changes to automatically trigger sync
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            if (!route.startsWith("detail/") && route != Screen.Settings.route) {
                syncManager.onLayerOpened(route)
            }
        }
    }

    val isTopLevel = currentRoute in BOTTOM_NAV_ITEMS.map { it.route }
    val showBottomBar = isTopLevel

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        topBar = {
            if (isTopLevel) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SyncStatusIndicator(
                        syncState = syncState,
                        onManualSync = { syncManager.triggerManualSync() }
                    )

                    IconButton(
                        onClick = { navController.navigate(Screen.Settings.route) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            painter = painterResource(id = AppIcons.Settings),
                            contentDescription = "Settings",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.statusBarsPadding())
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(68.dp)
                ) {
                    BOTTOM_NAV_ITEMS.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = screen.iconRes),
                                    contentDescription = screen.title,
                                    tint = if (isSelected) NeonCyan else TextTertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonCyan else TextTertiary
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = DarkSurfaceVariant
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(280)) + slideInHorizontally(animationSpec = tween(280)) { it / 3 } },
            exitTransition = { fadeOut(animationSpec = tween(220)) + slideOutHorizontally(animationSpec = tween(220)) { -it / 3 } },
            popEnterTransition = { fadeIn(animationSpec = tween(280)) + slideInHorizontally(animationSpec = tween(280)) { -it / 3 } },
            popExitTransition = { fadeOut(animationSpec = tween(220)) + slideOutHorizontally(animationSpec = tween(220)) { it / 3 } }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToMetric = { metric ->
                        navController.navigate(Screen.Detail.createRoute(metric))
                    },
                    onNavigateToLayer = { layerRoute ->
                        navController.navigate(layerRoute)
                    }
                )
            }

            composable(Screen.Activity.route) {
                ActivityScreen(
                    viewModel = activityViewModel,
                    onNavigateToMetric = { metric ->
                        navController.navigate(Screen.Detail.createRoute(metric))
                    }
                )
            }

            composable(Screen.VitalsSleep.route) {
                VitalsSleepScreen(
                    vitalsViewModel = vitalsViewModel,
                    sleepViewModel = sleepViewModel,
                    onNavigateToMetric = { metric ->
                        navController.navigate(Screen.Detail.createRoute(metric))
                    },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.BodyMind.route) {
                BodyMindScreen(
                    bodyCompositionViewModel = bodyCompositionViewModel,
                    stressViewModel = stressViewModel,
                    onNavigateToMetric = { metric ->
                        navController.navigate(Screen.Detail.createRoute(metric))
                    },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onRequestPermissions = onRequestPermissions
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("metricType") { type = NavType.StringType })
            ) { backStackEntry ->
                val metricTypeName = backStackEntry.arguments?.getString("metricType") ?: MetricType.STEPS.name
                val metricType = try {
                    MetricType.valueOf(metricTypeName)
                } catch (e: Exception) {
                    MetricType.STEPS
                }

                LaunchedEffect(metricType) {
                    detailViewModel.initMetric(metricType)
                }

                DetailOverviewScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
