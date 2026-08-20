package com.hcdash.app.ui.navigation

import androidx.annotation.DrawableRes
import com.hcdash.app.domain.model.MetricType
import com.hcdash.app.ui.components.AppIcons

sealed class Screen(val route: String, val title: String, @DrawableRes val iconRes: Int) {
    data object Dashboard : Screen("dashboard", "Overview", AppIcons.Dashboard)
    data object Activity : Screen("activity", "Activity", AppIcons.Activity)
    data object VitalsSleep : Screen("vitals_sleep", "Vitals", AppIcons.HeartPulse)
    data object BodyMind : Screen("body_mind", "Body", AppIcons.Scale)
    data object Settings : Screen("settings", "Settings", AppIcons.Settings)

    data object Detail : Screen("detail/{metricType}", "Detail", AppIcons.Activity) {
        fun createRoute(metricType: MetricType): String = "detail/${metricType.name}"
    }
}

val BOTTOM_NAV_ITEMS = listOf(
    Screen.Dashboard,
    Screen.Activity,
    Screen.VitalsSleep,
    Screen.BodyMind
)
