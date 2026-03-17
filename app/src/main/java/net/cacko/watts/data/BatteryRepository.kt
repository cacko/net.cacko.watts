package net.cacko.watts.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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

        val capacityPercent = if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else 0

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = currentUa / 1000
        
        val chargeTimeRemainingMs = if (isCharging) {
            batteryManager.computeChargeTimeRemaining()
        } else {
            -1L
        }

        // Estimate discharge time
        val dischargeTimeRemainingMs = if (!isCharging && currentMa < 0) {
            val remainingMaH = (capacityPercent / 100f) * 4500 // Assuming 4500mAh for now, could be dynamic
            val dischargeCurrentMa = abs(currentMa.toFloat())
            if (dischargeCurrentMa > 0) {
                (remainingMaH / dischargeCurrentMa * 3600 * 1000).toLong()
            } else {
                -1L
            }
        } else {
            -1L
        }

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
            chargeTimeRemainingMs = chargeTimeRemainingMs,
            dischargeTimeRemainingMs = dischargeTimeRemainingMs
        )
    }

    private fun abs(value: Float): Float = if (value < 0) -value else value
}
