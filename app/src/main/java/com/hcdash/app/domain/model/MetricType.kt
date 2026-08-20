package com.hcdash.app.domain.model



enum class MetricType(
    val title: String,
    val unit: String,
    val category: MetricCategory,
    val primaryColor: Long,
    val secondaryColor: Long
) {
    STEPS(
        title = "Steps",
        unit = "steps",
        category = MetricCategory.ACTIVITY,
        primaryColor = 0xFF00E676,
        secondaryColor = 0xFF69F0AE
    ),
    CALORIES(
        title = "Active Calories",
        unit = "kcal",
        category = MetricCategory.ACTIVITY,
        primaryColor = 0xFFFF6D00,
        secondaryColor = 0xFFFFAB40
    ),
    HEART_RATE(
        title = "Heart Rate",
        unit = "bpm",
        category = MetricCategory.VITALS,
        primaryColor = 0xFFFF1744,
        secondaryColor = 0xFFFF5252
    ),
    RESTING_HEART_RATE(
        title = "Resting Heart Rate",
        unit = "bpm",
        category = MetricCategory.VITALS,
        primaryColor = 0xFFE91E63,
        secondaryColor = 0xFFF48FB1
    ),
    OXYGEN_SATURATION(
        title = "Blood Oxygen (SpO2)",
        unit = "%",
        category = MetricCategory.VITALS,
        primaryColor = 0xFF00B0FF,
        secondaryColor = 0xFF40C4FF
    ),
    SLEEP(
        title = "Sleep Duration",
        unit = "hrs",
        category = MetricCategory.SLEEP,
        primaryColor = 0xFF7C4DFF,
        secondaryColor = 0xFFB388FF
    ),
    WEIGHT(
        title = "Weight",
        unit = "kg",
        category = MetricCategory.BODY,
        primaryColor = 0xFF00E5FF,
        secondaryColor = 0xFF18FFFF
    ),
    BMI(
        title = "Body Mass Index",
        unit = "kg/m²",
        category = MetricCategory.BODY,
        primaryColor = 0xFF26A69A,
        secondaryColor = 0xFF80CBC4
    ),
    BODY_FAT(
        title = "Body Fat",
        unit = "%",
        category = MetricCategory.BODY,
        primaryColor = 0xFFFFAB00,
        secondaryColor = 0xFFFFD740
    ),
    BODY_WATER(
        title = "Body Water",
        unit = "%",
        category = MetricCategory.BODY,
        primaryColor = 0xFF0288D1,
        secondaryColor = 0xFF29B6F6
    ),
    MUSCLE_MASS(
        title = "Muscle Mass",
        unit = "kg",
        category = MetricCategory.BODY,
        primaryColor = 0xFF8E24AA,
        secondaryColor = 0xFFBA68C8
    ),
    BONE_MASS(
        title = "Bone Mass",
        unit = "kg",
        category = MetricCategory.BODY,
        primaryColor = 0xFF78909C,
        secondaryColor = 0xFFB0BEC5
    ),
    STRESS_HRV(
        title = "Stress & HRV",
        unit = "ms",
        category = MetricCategory.STRESS,
        primaryColor = 0xFFFF5252,
        secondaryColor = 0xFFFF8A80
    )
}

enum class MetricCategory(val label: String) {
    ACTIVITY("Activity"),
    VITALS("Vitals"),
    SLEEP("Sleep"),
    BODY("Body Composition"),
    STRESS("Stress & Recovery")
}

enum class TimeRange(val label: String, val days: Int) {
    DAY_1("24H", 1),
    DAYS_7("7D", 7),
    DAYS_30("30D", 30),
    DAYS_90("90D", 90)
}
