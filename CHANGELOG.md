# Changelog

All notable changes to this project will be documented in this file.

## [1.2] - 2024-05-21

### Added
- **Radio Signal Strength**: Monitor cellular signal dBm as a proxy for radio power consumption.
- **Charging Policy**: Visibility into Android 14+ charging modes (Adaptive, Longevity, Default).
- **Power Status**: Real-time monitoring of Battery Saver and Device Interactivity (Awake/Idling) states.
- **Enhanced Metrics**: Expanded the dashboard with a new grid of power-related insights.

### Fixed
- Fixed `java.lang.SecurityException` on certain devices when accessing battery properties requiring `BATTERY_STATS` permission.
- Improved application stability by adding safe-guards around `BatteryManager` property access.

## [1.1] - 2024-05-20

### Fixed
- Improved application stability by adding safe-guards around `BatteryManager` property access.

### Changed
- Updated `README.md` with detailed feature descriptions and resiliency information.
- Refined Play Store metadata to reflect new metrics and device compatibility improvements.
- Enhanced battery health reporting.
