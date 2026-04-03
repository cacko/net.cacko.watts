# Changelog

All notable changes to this project will be documented in this file.

## [1.1] - 2024-05-20

### Fixed
- Fixed `java.lang.SecurityException` on certain devices when accessing battery properties requiring `BATTERY_STATS` permission.
- Improved application stability by adding safe-guards around `BatteryManager` property access.

### Changed
- Updated `README.md` with detailed feature descriptions and resiliency information.
- Refined Play Store metadata to reflect new metrics and device compatibility improvements.
- Enhanced battery health and cycle count reporting.
