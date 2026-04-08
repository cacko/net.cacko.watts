package net.cacko.watts.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.cacko.watts.data.BatteryMetrics
import net.cacko.watts.ui.theme.MajorMonoDisplayFontFamily
import net.cacko.watts.ui.theme.WattsTheme
import java.util.*
import kotlin.math.abs

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val metrics by viewModel.batteryMetrics.collectAsStateWithLifecycle()
    
    DashboardContent(metrics)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(metrics: BatteryMetrics) {
    var selectedMetricInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    val energyColor by animateColorAsState(
        targetValue = getEnergyColor(metrics),
        label = "EnergyColor"
    )

    if (selectedMetricInfo != null) {
        AlertDialog(
            onDismissRequest = { selectedMetricInfo = null },
            title = { Text(text = selectedMetricInfo!!.first) },
            text = { Text(text = selectedMetricInfo!!.second) },
            confirmButton = {
                TextButton(onClick = { selectedMetricInfo = null }) {
                    Text("Got it")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "TitleAnimation")
    val titleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (metrics.isCharging) 1000 else 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TitleAlpha"
    )

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
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = "WATTS", 
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = MajorMonoDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (metrics.isCharging) energyColor else MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.alpha(if (metrics.isCharging) 1f else titleAlpha)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    windowInsets = WindowInsets.statusBars
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Central Wattage Widget
                WattageWidget(metrics.watts, energyColor, metrics.isCharging)

                // Battery Metrics Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetricCard(
                            label = "Current",
                            value = "${metrics.currentMa} mA",
                            icon = Icons.Default.ElectricBolt,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedMetricInfo = "Current" to "The amount of electric current flowing into or out of the battery. Positive values indicate charging, while negative values indicate the device is using battery power."
                            }
                        )
                        MetricCard(
                            label = "Voltage",
                            value = String.format(Locale.getDefault(), "%.2f V", metrics.voltageMv / 1000f),
                            icon = Icons.Default.Bolt,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedMetricInfo = "Voltage" to "The electrical pressure provided by the battery. A typical smartphone battery operates between 3.2V (empty) and 4.4V (full)."
                            }
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
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedMetricInfo = "Temperature" to "The current temperature of the battery. Keeping your battery cool (below 35°C / 95°F) helps prolong its total lifespan."
                            }
                        )
                        MetricCard(
                            label = "Capacity",
                            value = "${metrics.capacityPercent}%",
                            icon = Icons.Default.FlashOn,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedMetricInfo = "Capacity" to "The current amount of energy stored in the battery as a percentage of its estimated full capacity."
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetricCard(
                            label = "Battery Size",
                            value = metrics.batteryCapacityMah?.let { "$it mAh" } ?: "--",
                            icon = Icons.Default.BatteryFull,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedMetricInfo = "Battery Size" to "The estimated total energy capacity of your battery. This value is calculated by comparing the current charge counter with the percentage level."
                            }
                        )
                        MetricCard(
                            label = "Health",
                            value = metrics.health,
                            icon = Icons.Default.Favorite,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedMetricInfo = "Health" to "The system's report on the physical condition of the battery. Most devices report 'Good' unless there is a hardware failure or significant degradation."
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetricCard(
                            label = "Technology",
                            value = metrics.technology ?: "--",
                            icon = Icons.Default.Memory,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedMetricInfo = "Technology" to "The chemical technology used in the battery. Most modern smartphones use Lithium-ion (Li-ion) or Lithium-polymer (Li-poly) cells."
                            }
                        )
                        MetricCard(
                            label = "Powered",
                            value = metrics.pluggedSource ?: "--",
                            icon = Icons.Default.Power,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedMetricInfo = "Powered" to "The source of power currently connected to the device. 'Battery' means the device is not plugged in."
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
fun WattageWidget(watts: Float, color: Color, isCharging: Boolean) {
    val displayWatts = abs(watts)
    
    val infiniteTransition = rememberInfiniteTransition(label = "EnergyAnimation")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCharging) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isCharging) 1500 else 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = if (isCharging) 0.3f else 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isCharging) 1500 else 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isCharging) 360f else -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isCharging) 3000 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Animated background pulse
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(color.copy(alpha = pulseAlpha))
            )
            
            // Rotating ring (dashed for charging, solid/slow for discharging)
            Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
                if (isCharging) {
                    drawCircle(
                        color = color,
                        radius = size.minDimension / 2 - 4.dp.toPx(),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 40f), 0f)
                        ),
                        alpha = 0.5f
                    )
                } else {
                    drawCircle(
                        color = color,
                        radius = size.minDimension / 2 - 4.dp.toPx(),
                        style = Stroke(
                            width = 1.dp.toPx()
                        ),
                        alpha = 0.2f
                    )
                }
            }
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(210.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", displayWatts),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black
                        ),
                        letterSpacing = 0.5.sp,
                        color = color
                    )
                    Text(
                        text = "WATTS",
                        style = MaterialTheme.typography.labelLarge,
                        color = color.copy(alpha = 0.7f),
                        letterSpacing = 4.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isCharging) Icons.Default.Power else Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(22.dp)
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
    }
}

@Composable
fun MetricCard(
    label: String, 
    value: String, 
    icon: ImageVector, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
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
    WattsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            DashboardContent(
                metrics = BatteryMetrics(
                    currentMa = 4500,
                    voltageMv = 4200,
                    temperatureC = 35.5f,
                    capacityPercent = 85,
                    isCharging = true,
                    health = "Good",
                    batteryCapacityMah = 4850,
                    technology = "Li-ion",
                    pluggedSource = "AC"
                )
            )
        }
    }
}
