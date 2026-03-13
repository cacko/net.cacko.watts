package net.cacko.watts.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import net.cacko.watts.data.BatteryMetrics
import net.cacko.watts.data.BatteryRepository
import net.cacko.watts.data.BatteryRepositoryImpl

class MainViewModel(
    private val batteryRepository: BatteryRepository
) : ViewModel() {

    val batteryMetrics: StateFlow<BatteryMetrics> = batteryRepository.getBatteryMetrics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BatteryMetrics()
        )

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(BatteryRepositoryImpl(context)) as T
        }
    }
}
