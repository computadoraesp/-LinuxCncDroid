package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.model.CoolantInfo
import com.example.model.FeedInfo
import com.example.model.MachineStateEnum
import com.example.model.SpindleInfo
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SpindleFeedPanel(
    spindle: SpindleInfo,
    feed: FeedInfo,
    coolant: CoolantInfo,
    machineState: MachineStateEnum,
    onToggleSpindle: () -> Unit,
    onSetSpindleRpm: (Double) -> Unit,
    onSpindleOverride: (Int) -> Unit,
    onFeedOverride: (Int) -> Unit,
    onToggleMist: () -> Unit,
    onToggleFlood: () -> Unit,
    onCycleStart: () -> Unit,
    onFeedHold: () -> Unit,
    onCycleStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CncCardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Section 1: Cycle Execution Controls (START / PAUSE / STOP)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // CYCLE START
                val isRunning = machineState == MachineStateEnum.RUNNING
                Button(
                    onClick = onCycleStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) CncActiveGreen else Color(0xFF005327),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Cycle Start", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isRunning) "RUNNING" else "CYCLE START", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                // FEEDHOLD / PAUSE
                val isPaused = machineState == MachineStateEnum.PAUSED
                Button(
                    onClick = onFeedHold,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) CncWarningAmber else Color(0xFF5E4200),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Pause, contentDescription = "Feedhold", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "FEEDHOLD", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                // STOP / ABORT
                Button(
                    onClick = onCycleStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF930020),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = "Cycle Stop", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ABORT", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }

            Divider(color = CncCardBorder)

            // Section 2: Spindle Control & RPM
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.RotateRight, contentDescription = "Spindle", tint = CncWarningAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SPINDLE MOTOR", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CncTextPrimary)
                    }

                    // Spindle Toggle Button
                    FilledTonalButton(
                        onClick = onToggleSpindle,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (spindle.isEnabled) CncActiveGreen else CncSurfaceVariant,
                            contentColor = if (spindle.isEnabled) Color.Black else CncTextSecondary
                        ),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(if (spindle.isEnabled) "SPINDLE ON" else "SPINDLE OFF", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }

                // RPM Readout (Actual vs Commanded)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CncSurface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ACTUAL RPM", fontSize = 9.sp, color = CncTextMuted, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(Locale.US, "%05.0f", spindle.actualRpm),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (spindle.isEnabled) CncWarningAmber else CncTextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("SET: ${spindle.commandedRpm.toInt()} RPM", fontSize = 10.sp, color = CncTextSecondary, fontFamily = FontFamily.Monospace)
                        Text("OVERRIDE: ${spindle.overridePct}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CncCyberCyan, fontFamily = FontFamily.Monospace)
                    }
                }

                // Spindle Presets
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(3000.0, 6000.0, 12000.0, 18000.0, 24000.0).forEach { presetRpm ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CncSurfaceVariant)
                                .border(1.dp, CncCardBorder, RoundedCornerShape(4.dp))
                                .clickable { onSetSpindleRpm(presetRpm) }
                        ) {
                            Text("${(presetRpm / 1000).toInt()}k", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CncTextPrimary)
                        }
                    }
                }

                // Spindle Override Slider
                Slider(
                    value = spindle.overridePct.toFloat(),
                    onValueChange = { onSpindleOverride(it.toInt()) },
                    valueRange = 10f..200f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = CncWarningAmber,
                        activeTrackColor = CncWarningAmber,
                        inactiveTrackColor = CncSurfaceVariant
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }

            Divider(color = CncCardBorder)

            // Section 3: Feedrate Override & Coolant
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FEEDRATE OVERRIDE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CncTextPrimary)
                    Text(
                        "${feed.feedOverridePct}% (${(feed.commandedFeed * (feed.feedOverridePct / 100.0)).toInt()} mm/min)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CncCyberCyan
                    )
                }

                Slider(
                    value = feed.feedOverridePct.toFloat(),
                    onValueChange = { onFeedOverride(it.toInt()) },
                    valueRange = 0f..200f,
                    steps = 20,
                    colors = SliderDefaults.colors(
                        thumbColor = CncCyberCyan,
                        activeTrackColor = CncCyberCyan,
                        inactiveTrackColor = CncSurfaceVariant
                    ),
                    modifier = Modifier.height(24.dp)
                )

                // Coolant Switches (Mist M7 & Flood M8)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onToggleMist,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (coolant.mist) Color(0xFF004F58) else CncSurfaceVariant,
                            contentColor = if (coolant.mist) CncCyberCyan else CncTextSecondary
                        ),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "Mist", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (coolant.mist) "MIST ON (M7)" else "MIST OFF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = onToggleFlood,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (coolant.flood) Color(0xFF00363D) else CncSurfaceVariant,
                            contentColor = if (coolant.flood) CncCyberCyan else CncTextSecondary
                        ),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Waves, contentDescription = "Flood", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (coolant.flood) "FLOOD ON (M8)" else "FLOOD OFF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
