# HC-Dash ⚡

> **Native Android Health Connect Dashboard & Biometric Analytics Engine**  
> Built with Kotlin, Jetpack Compose, Android Health Connect API, Room Database, and Material 3.

---

## 🌟 Overview

**HC-Dash** is a privacy-first, offline-capable health dashboard that aggregates biometrics directly from Android's **Health Connect API** into a local **Room SQLite Database**. It calculates trends, moving averages, standard deviations, and regression forecasts while rendering interactive Canvas visualizations with smooth micro-animations.

---

## ✨ Key Features

- **🔄 Automated Multi-Layer Syncing**:
  - **App Launch**: Syncs the latest health data automatically upon opening.
  - **Tab / Layer Navigation**: Refreshes biometrics when navigating across views (with debounce & concurrency locking).
  - **Detail Overview Open**: Targeted metric synchronization when inspecting a specific vital.
  - **Manual Sync**: One-tap instant pull-to-refresh.

- **💾 100% Offline & Private (Room SQLite)**:
  - All records from Health Connect are stored locally in Room.
  - Fast analytical queries and historical trends without internet connection or third-party cloud servers.

- **📊 Comprehensive Biometric Analytics**:
  - **🏃 Activity**: Daily steps, step goals, hourly distribution, active calories burned.
  - **🫀 Vitals**: Heart rate timeline, resting heart rate (RHR), and blood oxygen saturation ($SpO_2$).
  - **🌙 Sleep & Recovery**: Sleep duration, efficiency score, and multi-stage hypnogram (*Deep*, *REM*, *Light*, *Awake*).
  - **⚖️ Body Composition**: Weight, BMI, body fat percentage, body water percentage, muscle mass, and bone mass.
  - **🧠 Stress & Autonomic State**: Stress score gauge and Heart Rate Variability ($HRV\ RMSSD$) timeline.

- **🎨 100% Custom Vector Iconography**:
  - Designed exclusively with custom vector icons from [tiwut / Icon-Library](https://github.com/tiwut/Icon-Library).

- **📱 Streamlined 4-Tab Navigation**:
  - 🏠 **Overview**: Central wellness score gauge, vital highlights, and quick access cards.
  - 🏃 **Activity**: Steps and active calorie burn dynamics.
  - 🫁 **Vitals & Sleep**: Segmented switcher between Heart Rate / $SpO_2$ and Sleep Hypnogram.
  - 🧘 **Body & Mind**: Segmented switcher between Body Composition and Stress / $HRV$.
  - ⚙️ **Settings**: Top app bar button for goals, sync preferences, and export tools.

- **🪄 Smooth Micro-Animations**:
  - Bézier curve path transitions and gradient reveals on line charts.
  - Animated capsule bar height scaling on bar charts.
  - Smooth sweep angle progression on radial wellness gauges.
  - Spring scale and sliding pill selectors for time ranges.

- **🎨 Native Android 12+ Material You Dynamic Theming**:
  - Dynamic Color support extracted from system wallpaper on Android 12+ (API 31+).
  - Customizable theme mode: **Dark**, **Light**, or **System Default**.
  - Proper edge-to-edge support with safe system bar insets.

- **⚙️ Customizable Settings & Tools**:
  - **Daily Goals**: Interactive sliders for Step Goal (3k–25k), Calorie Goal (200–1500 kcal), Sleep Target (5h–10h), and Weight Goal.
  - **Sync Automation**: Toggles for auto-refresh on layer switch and on app startup.
  - **Export Report**: Share a 7-day health summary report via Android Share sheet.
  - **Health Connect Launcher**: Direct button to configure permissions in Android settings.
  - **Demo Tools**: 1-tap generator to seed 30 days of circadian sample biometrics for testing.

---

## 🏗️ Architecture

```
com.hcdash.app
├── data
│   ├── generator          # 30-day realistic circadian sample data generator
│   ├── healthconnect      # Health Connect API client & permissions manager
│   ├── local              # Room database, DAOs, and entity schemas
│   ├── repository         # HealthRepository unifying Room & Health Connect
│   └── sync               # HealthSyncManager (concurrency mutex & debouncing)
├── domain
│   ├── analytics          # HealthAnalyticsEngine (linear regression & stats)
│   ├── model              # Domain models, MetricType, TimeRange, Summaries
│   └── utils              # FormatUtils (number formatting & rounding)
└── ui
    ├── components         # Canvas charts, gauges, hypnograms, AppIcons
    ├── navigation         # AppNavigation, NavRoutes (4-tab bar)
    ├── screens
    │   ├── activity       # Steps & calorie burn view
    │   ├── body           # Body composition & weight view
    │   ├── body_mind      # Unified Body & Stress tab
    │   ├── dashboard      # Central Overview & Wellness Score
    │   ├── detail         # Universal metric deep-dive overview
    │   ├── settings       # Goals, themes, permissions & about
    │   ├── sleep          # Sleep hypnogram & recovery view
    │   ├── stress         # Stress level & HRV RMSSD view
    │   ├── vitals         # Heart rate & SpO2 view
    │   └── vitals_sleep   # Unified Vitals & Sleep tab
    └── theme              # Material 3 color schemes, typography & theme
```

---

## 🛠️ Build & Setup Instructions

### Prerequisites
- **Android Studio** Ladybug (2024.2+) or IntelliJ IDEA with Android Plugin
- **JDK 17** (e.g. OpenJDK 17)
- **Android SDK** API 34 (Android 14) / API 35 (Android 15)
- Android device or emulator with **Health Connect** installed

### Commands

```bash
# Clean build artifacts and caches
./clean.sh

# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📜 Permissions

HC-Dash declares Health Connect read permissions in `AndroidManifest.xml`:
- `READ_STEPS`, `READ_TOTAL_CALORIES_BURNED`, `READ_ACTIVE_CALORIES_BURNED`
- `READ_HEART_RATE`, `READ_RESTING_HEART_RATE`, `READ_OXYGEN_SATURATION`
- `READ_SLEEP`, `READ_WEIGHT`, `READ_BODY_FAT`, `READ_BODY_WATER_MASS`
- `READ_BONE_MASS`, `READ_LEAN_BODY_MASS`, `READ_HEART_RATE_VARIABILITY`

---

## 👨‍💻 Creator & Attribution

- **Created by**: [tiwut](https://github.com/tiwut)
- **Icon Library**: Custom vector drawables from [tiwut / Icon-Library](https://github.com/tiwut/Icon-Library)

---

## 📄 License

This project is licensed under the MIT License.
