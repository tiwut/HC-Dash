package com.hcdash.app.ui.components

import androidx.annotation.DrawableRes
import com.hcdash.app.R
import com.hcdash.app.domain.model.MetricType

object AppIcons {
    @DrawableRes val Dashboard = R.drawable.ic_dashboard
    @DrawableRes val Activity = R.drawable.ic_activity
    @DrawableRes val Footprints = R.drawable.ic_footprints
    @DrawableRes val Flame = R.drawable.ic_flame
    @DrawableRes val HeartPulse = R.drawable.ic_heart_pulse
    @DrawableRes val Heart = R.drawable.ic_heart
    @DrawableRes val Droplet = R.drawable.ic_droplet
    @DrawableRes val Moon = R.drawable.ic_moon
    @DrawableRes val Scale = R.drawable.ic_scale
    @DrawableRes val Biceps = R.drawable.ic_biceps
    @DrawableRes val Bone = R.drawable.ic_bone
    @DrawableRes val Brain = R.drawable.ic_brain
    @DrawableRes val Settings = R.drawable.ic_settings
    @DrawableRes val Refresh = R.drawable.ic_refresh
    @DrawableRes val ShieldCheck = R.drawable.ic_shield_check
    @DrawableRes val ChevronLeft = R.drawable.ic_chevron_left
    @DrawableRes val ChevronRight = R.drawable.ic_chevron_right
    @DrawableRes val Palette = R.drawable.ic_palette
    @DrawableRes val Send = R.drawable.ic_send
    @DrawableRes val Info = R.drawable.ic_info
    @DrawableRes val Target = R.drawable.ic_target
    @DrawableRes val Github = R.drawable.ic_github
    @DrawableRes val TrendingUp = R.drawable.ic_arrow_up_right
    @DrawableRes val TrendingDown = R.drawable.ic_arrow_down_right
    @DrawableRes val TrendingFlat = R.drawable.ic_arrow_right

    @DrawableRes
    fun forMetric(type: MetricType): Int = when (type) {
        MetricType.STEPS -> Footprints
        MetricType.CALORIES -> Flame
        MetricType.HEART_RATE -> HeartPulse
        MetricType.RESTING_HEART_RATE -> Heart
        MetricType.OXYGEN_SATURATION -> Droplet
        MetricType.SLEEP -> Moon
        MetricType.WEIGHT -> Scale
        MetricType.BMI -> Scale
        MetricType.BODY_FAT -> Biceps
        MetricType.BODY_WATER -> Droplet
        MetricType.MUSCLE_MASS -> Biceps
        MetricType.BONE_MASS -> Bone
        MetricType.STRESS_HRV -> Brain
    }
}
