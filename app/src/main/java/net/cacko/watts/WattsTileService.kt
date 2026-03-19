package net.cacko.watts

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.cacko.watts.data.BatteryRepositoryImpl
import java.util.Locale
import kotlin.math.abs

class WattsTileService : TileService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var listeningJob: Job? = null
    private lateinit var batteryRepository: BatteryRepositoryImpl

    override fun onCreate() {
        super.onCreate()
        batteryRepository = BatteryRepositoryImpl(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        
        // Ensure we are working with the latest tile state
        val tile = qsTile ?: return
        
        listeningJob?.cancel()
        listeningJob = batteryRepository.getBatteryMetrics()
            .onEach { metrics ->
                val watts = abs(metrics.watts)
                val labelText = String.format(Locale.getDefault(), "%.1f W", watts)
                val statusText = if (metrics.isCharging) "Charging" else "Discharging"
                
                tile.apply {
                    state = Tile.STATE_ACTIVE
                    // In some Android versions, 'label' is the primary text.
                    // In others, 'label' is the app name and 'subtitle' is the value.
                    // We set both to ensure the value is displayed.
                    label = labelText
                    subtitle = statusText
                    
                    // stateDescription is often used for the second line of text in newer QS designs
                    stateDescription = labelText
                    
                    // contentDescription for accessibility
                    contentDescription = "$labelText ($statusText)"

                    updateTile()
                }
            }
            .launchIn(serviceScope)
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        startActivityAndCollapse(pendingIntent)
    }

    override fun onStopListening() {
        super.onStopListening()
        listeningJob?.cancel()
        listeningJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
