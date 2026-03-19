package net.cacko.watts.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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
                title = { 
                    Text(
                        text = "WATTS", 
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = MajorMonoDisplayFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetricCard(
                            label = "Health",
                            value = metrics.health,
                            icon = Icons.Default.Favorite,
                            modifier = Modifier.weight(1f)
                        )
                        
                        val timeLabel = if (metrics.isCharging) "Charged In" else "Remaining"
                        val timeValue = if (metrics.isCharging) {
                            if (metrics.chargeTimeRemainingMs > 0) formatRemainingTime(metrics.chargeTimeRemainingMs) else "Calculating..."
                        } else {
                            if (metrics.dischargeTimeRemainingMs > 0) formatRemainingTime(metrics.dischargeTimeRemainingMs) else "Calculating..."
                        }
                        val timeIcon = if (metrics.isCharging) Icons.Default.HourglassEmpty else Icons.Default.HourglassFull

                        MetricCard(
                            label = timeLabel,
                            value = timeValue,
                            icon = timeIcon,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatRemainingTime(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
    } else {
        String.format(Locale.getDefault(), "%dm", minutes)
    }
}

@Composable
fun WattageWidget(watts: Float, color: Color, isCharging: Boolean) {
    val displayWatts = abs(watts)
    
    val infiniteTransition = rememberInfiniteTransition(label = "ChargingAnimation")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCharging) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = if (isCharging) 0.3f else 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
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
            if (isCharging) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(color.copy(alpha = pulseAlpha))
                )
                
                // Rotating ring
                Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
                    drawCircle(
                        color = color,
                        radius = size.minDimension / 2 - 4.dp.toPx(),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 40f), 0f)
                        ),
                        alpha = 0.5f
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
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isCharging) "CHARGING" else "DISCHARGING",
                            style = MaterialTheme.typography.labelMedium,
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
                    chargeTimeRemainingMs = 3600000
                )
            )
        }
    }
}
