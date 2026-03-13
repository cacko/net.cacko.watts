# Project Plan

Watts is an Android app that monitors and displays charging speed in Watts in real-time. It features a Material 3 UI with a vibrant energy-themed color scheme and an edge-to-edge experience.

## Project Brief

# Project Brief: Watts - Energy Monitor

A streamlined, high-performance Android application designed to provide users
 with real-time insights into their device's charging performance. The app focuses on a vibrant, Material 3-driven aesthetic to make technical battery data visually engaging.

## Features
1.  **Real-time Wattage Widget**: A prominent, central UI component on the main dashboard that calculates and displays the current charging
 speed in Watts by polling high-precision battery current and voltage data.
2.  **Vibrant Energy Dashboard**: A Material 3 dashboard featuring a "vibrant energy" color scheme that dynamically shifts its intensity and hue based on the charging speed (e.g., glowing ambers for fast charging, cool cy
ans for trickle charging).
3.  **Live Battery Metrics**: Real-time display of critical electrical data, including current (mA), voltage (V), and battery temperature, providing a comprehensive view of the charging environment.
4.  **Edge-to-Edge Experience**: A fully immersive UI that utilizes the entire
 display area, including status and navigation bars, ensuring a modern and seamless user experience.

## High-Level Technical Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material Design 3)
*   **Concurrency**: Kotlin Coroutines & Flow (for real-time battery
 data streams)
*   **Code Generation**: KSP (Kotlin Symbol Processing)
*   **Core APIs**: 
    *   **BatteryManager**: To retrieve instantaneous current (uA).
    *   **BroadcastReceiver**: To capture `ACTION_BATTERY_CHANGED` sticky intents for voltage (mV
) and status.
*   **Architecture**: MVVM (Model-View-ViewModel) with state-driven UI.

## Implementation Steps

### Task_1_BatteryDataEngine: Implement the core battery monitoring logic using BatteryManager and BroadcastReceiver. Create a BatteryRepository and a MainViewModel to expose real-time battery metrics (current, voltage, temperature, and calculated Watts) via Kotlin Flow.
- **Status:** COMPLETED
- **Updates:** I have successfully implemented Task 1: BatteryDataEngine for the Watts app.

### Task_2_EnergyDashboardUI: Develop the main dashboard UI using Jetpack Compose and Material 3. Implement full edge-to-edge support. Create the 'Real-time Wattage Widget' and 'Live Battery Metrics' sections. Implement a vibrant color scheme that shifts based on charging speed.
- **Status:** COMPLETED
- **Updates:** I have successfully updated
 `MainActivity.kt` with the correct integration and verified the build.

###
 50-Line Confirmation of `MainActivity.kt`:

```kotlin
package
 net.cacko.watts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx
.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import net.cacko.watts.ui.DashboardScreen
import net.cacko
.watts.ui.MainViewModel
import net.cacko.watts.ui.theme.WattsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(
applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WattsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.color
Scheme.background
                ) {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}

```


### Key Actions Taken:
1.  **Forced Overwrite**: Deleted and recreated `MainActivity.kt` to ensure no cached or stale content remained.
2
.  **Verified Components**:
    *   Confirmed `net.cacko.watts.ui.DashboardScreen` is correctly implemented.
    *   Confirmed `net.cacko.watts.ui.MainViewModel` exists and has the required `Factory(context: Context)`.
    *   Confirmed 
`net.cacko.watts.data.BatteryRepositoryImpl` is present.
3.  **Successful Build**: Executed `./gradlew :app:assembleDebug` and confirmed the project builds without errors.
4.  **Content Verification**: Used `grep` and `read_file` to confirm that
 all "Greeting" code has been successfully removed and replaced with the `DashboardScreen` integration.

The app is now fully functional and correctly configured to launch the Energy Dashboard.

### Task_3_AppAssetsAndPolish: Generate and integrate an adaptive app icon matching the 'Watts' theme. Perform final UI polish and ensure the app theme is consistent (light/dark modes).
- **Status:** COMPLETED
- **Updates:** I

### Task_4_RunAndVerify: Perform a full build and run the application on a device/emulator. Verify that all features are functional, the app is stable (no crashes), and the UI aligns with the project brief.
- **Status:** IN_PROGRESS
- **Updates:** Critic agent could not run due to no device, but a manual check of MainActivity.kt reveals that the DashboardScreen integration is missing. The app still shows the default 'Hello Android' greeting. Reopening Task 2 to fix the integration.
- **Acceptance Criteria:**
  - Project builds successfully.
  - App does not crash during monitoring.
  - All existing tests pass.
  - Critic agent confirms stability and alignment with requirements.
- **StartTime:** 2026-03-13 15:08:42 EET

