package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EtherCatMasterInfo
import com.example.model.EtherCatSlaveInfo
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun EtherCatTelemetryView(
    masterInfo: EtherCatMasterInfo,
    slaves: List<EtherCatSlaveInfo>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CncCardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Hub, contentDescription = "EtherCAT", tint = CncActiveGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ETHERCAT BUS & SERVO TELEMETRY (NIVEL 3)", fontWeight = FontWeight.Black, fontSize = 12.sp, color = CncTextPrimary)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CncActiveGreen.copy(alpha = 0.15f))
                        .border(1.dp, CncActiveGreen, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("100 Mbps DETERMINISTIC", fontSize = 9.sp, fontWeight = FontWeight.Black, color = CncActiveGreen)
                }
            }

            // Master Bus Statistics Bar
            Surface(
                color = CncSurface,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BUS CYCLE", fontSize = 9.sp, color = CncTextMuted, fontWeight = FontWeight.Bold)
                        Text("${masterInfo.busCycleTimeUs} µs (1kHz)", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CncCyberCyan)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DC SYNC JITTER", fontSize = 9.sp, color = CncTextMuted, fontWeight = FontWeight.Bold)
                        Text("±${masterInfo.dcOffsetNs} ns", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CncActiveGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SLAVE NODES", fontSize = 9.sp, color = CncTextMuted, fontWeight = FontWeight.Bold)
                        Text("${masterInfo.slaveCount} ONLINE", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CncTextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PACKET LOSS", fontSize = 9.sp, color = CncTextMuted, fontWeight = FontWeight.Bold)
                        Text("0.00 %", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CncActiveGreen)
                    }
                }
            }

            // Servo Drives Live Status Cards (Delta B3 / A3)
            Text("DELTA ASDA-B3 CiA 402 SMART DRIVES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CncTextSecondary)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                slaves.forEach { slave ->
                    val torque = slave.actualTorquePct
                    val isTorqueHigh = torque > 75.0
                    val isTorqueWarning = torque > 45.0

                    Surface(
                        color = CncSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (slave.isFault) CncEstopRed else CncActiveGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "NODE #${slave.slaveIndex}: ${slave.name}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CncTextPrimary
                                    )
                                }

                                Text(
                                    text = slave.state,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CncActiveGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Torque Load Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "TORQUE LOAD: ${String.format(Locale.US, "%.1f", torque)} %",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isTorqueHigh) CncEstopRed else if (isTorqueWarning) CncWarningAmber else CncCyberCyan
                                )

                                Text(
                                    text = "DRIVE TEMP: ${String.format(Locale.US, "%.1f", slave.driveTempC)} °C",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CncTextSecondary
                                )
                            }

                            LinearProgressIndicator(
                                progress = { (torque / 100.0).toFloat().coerceIn(0f, 1f) },
                                color = if (isTorqueHigh) CncEstopRed else if (isTorqueWarning) CncWarningAmber else CncCyberCyan,
                                trackColor = CncSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )

                            // Fault / Alarm Code
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("DIAGNOSTIC STATUS:", fontSize = 9.sp, color = CncTextMuted)
                                Text(
                                    text = slave.alarmCode,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (slave.isFault) CncEstopRed else CncActiveGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
