# Watts

Watts is a modern Android application built with Jetpack Compose that provides real-time battery analytics and power metrics. It helps users understand their device's charging and discharging behavior with a clean, dynamic UI.

## Features

- **Real-time Wattage Tracking**: See exactly how many watts your device is pulling while charging or losing while discharging.
- **Quick Settings Tile**: Monitor live wattage and charging status directly from your device's Quick Settings panel. Click the tile to quickly open the app.
- **Charging Animations**: Dynamic pulsing and rotating indicators that activate when the device is plugged in.
- **Dynamic UI**: The interface background and colors adapt based on your charging state and speed (Slow, Medium, Fast, Super Fast).
- **Detailed Metrics**:
    - Current (mA)
    - Voltage (V)
    - Battery Temperature (°C)
    - Battery Capacity (%)
    - Battery Health (Good, Overheat, etc.)
    - Charge Cycles (On supported devices)
- **Material 3 Design**: Fully leverages Material 3 components and dynamic styling.
- **Resilient to Permissions**: Gracefully handles restricted system-level battery metrics (like `BATTERY_STATS`) by providing fallback values instead of crashing.
- **Edge-to-Edge**: Modern immersive display support.

## Project Structure

- **Jetpack Compose**: UI built entirely with declarative components and advanced animations.
- **Kotlin Coroutines & Flow**: For reactive state management of battery metrics.
- **Material 3**: The latest Android design system.
- **Go-Task Integration**: Automated build and deployment tasks.

## Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- JDK 11+.
- [Go-Task](https://taskfile.dev/) (Optional, for automation).

### Building the Project
You can build the project using Gradle:
```bash
./gradlew assembleDebug
```

Or using the provided Taskfile:
```bash
task build
```

## Automation with Taskfile
The project includes a `Taskfile.yml` to simplify common development tasks:
- `task build`: Compiles the debug APK.
- `task commit -- "message"`: Stages all changes and commits them to Git.
- `task push`: Pushes the current branch to GitHub.
- `task all`: Runs build, commit, and push in sequence.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
