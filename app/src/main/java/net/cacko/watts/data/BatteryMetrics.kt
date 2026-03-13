package net.cacko.watts.data

data class BatteryMetrics(
    val currentMa: Int = 0,
    val voltageMv: Int = 0,
    val temperatureC: Float = 0f,
    val capacityPercent: Int = 0,
    val isCharging: Boolean = false
) {
    val watts: Float
        get() = (voltageMv / 1000f) * (currentMa / 1000f)
}
