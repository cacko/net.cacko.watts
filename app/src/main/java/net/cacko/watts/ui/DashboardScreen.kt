package net.cacko.watts.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.cacko.watts.data.BatteryMetrics
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val metrics by viewModel.batteryMetrics.collectAsStateWithLifecycle()
    
    DashboardContent(metrics)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(metrics: BatteryMetrics) {
    val energyColor by animateColorAsState(
        targetValue = getEnergyColor(metrics),
        label = "EnergyColor"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Watts", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            energyColor.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Central Wattage Widget
                WattageWidget(metrics.watts, energyColor, metrics.isCharging)

                // Battery Metrics Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Live Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetricCard(
                            label = "Current",
                            value = "${metrics.currentMa} mA",
                            icon = Icons.Default.ElectricBolt,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Voltage",
                            value = String.format(Locale.getDefault(), "%.2f V", metrics.voltageMv / 1000f),
                            icon = Icons.Default.Bolt,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetricCard(
                            label = "Temperature",
                            value = "${metrics.temperatureC} °C",
                            icon = Icons.Default.DeviceThermostat,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Capacity",
                            value = "${metrics.capacityPercent}%",
                            icon = Icons.Default.FlashOn,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WattageWidget(watts: Float, color: Color, isCharging: Boolean) {
    val displayWatts = abs(watts)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", displayWatts),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = color
                )
                Text(
                    text = "WATTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = color.copy(alpha = 0.7f),
                    letterSpacing = 4.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.clip(RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isCharging) Icons.Default.Power else Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = color
                )
                Text(
                    text = if (isCharging) "CHARGING" else "DISCHARGING",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

fun getEnergyColor(metrics: BatteryMetrics): Color {
    return if (!metrics.isCharging) {
        // Discharging - Cool Blue
        Color(0xFF00B0FF)
    } else {
        val watts = abs(metrics.watts)
        when {
            watts < 5f -> Color(0xFF4CAF50) // Slow - Green
            watts < 15f -> Color(0xFFFFC107) // Medium - Amber
            watts < 30f -> Color(0xFFFF9800) // Fast - Orange
            else -> Color(0xFFFF5252) // Super Fast - Red
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardPreview() {
    MaterialTheme {
        DashboardContent(
            metrics = BatteryMetrics(
                currentMa = 4500,
                voltageMv = 4200,
                temperatureC = 35.5f,
                capacityPercent = 85,
                isCharging = true
            )
        )
    }
}
