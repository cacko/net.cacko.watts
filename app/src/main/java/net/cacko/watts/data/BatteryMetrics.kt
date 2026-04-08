package net.cacko.watts.data

data class BatteryMetrics(
    val currentMa: Int = 0,
    val voltageMv: Int = 0,
    val temperatureC: Float = 0f,
    val capacityPercent: Int = 0,
    val isCharging: Boolean = false,
    val health: String = "Unknown",
    val radioSignalDbm: Int? = null,
    val chargingPolicy: String? = null,
    val isPowerSaveMode: Boolean = false,
    val isInteractive: Boolean = true,
    val batteryCapacityMah: Int? = null,
    val designCapacityMah: Int? = null,
    val cycleCount: Int? = null,
    val technology: String? = null,
    val pluggedSource: String? = null
) {
    val watts: Float
        get() = (voltageMv / 1000f) * (currentMa / 1000f)
}
