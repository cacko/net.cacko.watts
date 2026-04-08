package net.cacko.watts.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.telephony.TelephonyManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface BatteryRepository {
    fun getBatteryMetrics(): Flow<BatteryMetrics>
}

class BatteryRepositoryImpl(
    private val context: Context
) : BatteryRepository {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val designCapacityMah: Int? = try {
        val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
        val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
        val capacity = powerProfileClass.getMethod("getAveragePower", String::class.java).invoke(powerProfile, "battery.capacity") as Double
        capacity.toInt()
    } catch (e: Exception) {
        try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
            val capacity = powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile) as Double
            capacity.toInt()
        } catch (e2: Exception) {
            null
        }
    }

    override fun getBatteryMetrics(): Flow<BatteryMetrics> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(calculateMetrics(intent))
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        // Initial emission
        val lastIntent = context.registerReceiver(null, filter)
        if (lastIntent != null) {
            trySend(calculateMetrics(lastIntent))
        }

        // Start a polling loop for current_now because it's not included in the broadcast
        val pollingJob = launch {
            while (isActive) {
                val currentIntent = context.registerReceiver(null, filter)
                if (currentIntent != null) {
                    trySend(calculateMetrics(currentIntent))
                }
                delay(1000)
            }
        }

        awaitClose {
            context.unregisterReceiver(receiver)
            pollingJob.cancel()
        }
    }.distinctUntilChanged().conflate()

    private fun calculateMetrics(intent: Intent): BatteryMetrics {
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val temperatureRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val healthRaw = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

        val capacityPercent = if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else 0

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val currentUa = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } catch (e: SecurityException) {
            0
        }
        val currentMa = currentUa / 1000
        
        val chargeCounterUah = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        } catch (e: SecurityException) {
            0
        }
        
        // Calculate estimated full capacity: (current_mAh / current_percent) * 100
        val batteryCapacityMah = if (capacityPercent > 0 && chargeCounterUah > 0) {
            val currentMah = chargeCounterUah / 1000
            (currentMah * 100) / capacityPercent
        } else {
            null
        }

        val cycleCount = try {
            // BATTERY_PROPERTY_CYCLE_COUNT is 8
            batteryManager.getIntProperty(8)
        } catch (e: Exception) {
            null
        }

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val pluggedSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            4 -> "Dock" // BatteryManager.BATTERY_PLUGGED_DOCK
            else -> if (isCharging) "Unknown" else "Battery"
        }

        // BATTERY_PROPERTY_CHARGING_POLICY is 9, added in API 34
        val chargingPolicyRaw = try {
            batteryManager.getIntProperty(9)
        } catch (e: Exception) {
            0
        }

        val chargingPolicy = when (chargingPolicyRaw) {
            1 -> "Default"
            2 -> "Adaptive"
            3 -> "Longevity"
            else -> null
        }

        val isPowerSaveMode = powerManager.isPowerSaveMode
        val isInteractive = powerManager.isInteractive

        val health = when (healthRaw) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        return BatteryMetrics(
            currentMa = currentMa,
            voltageMv = voltageMv,
            temperatureC = temperatureRaw / 10f,
            capacityPercent = capacityPercent,
            isCharging = isCharging,
            health = health,
            chargingPolicy = chargingPolicy,
            isPowerSaveMode = isPowerSaveMode,
            isInteractive = isInteractive,
            batteryCapacityMah = batteryCapacityMah,
            designCapacityMah = designCapacityMah,
            cycleCount = cycleCount,
            technology = technology,
            pluggedSource = pluggedSource
        )
    }
}
