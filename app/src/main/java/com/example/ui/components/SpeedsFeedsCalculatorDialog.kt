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
import com.example.model.MaterialPreset
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt

val STANDARD_MATERIALS = listOf(
    MaterialPreset("mat_al6061", "Aluminium 6061-T6", "Non-Ferrous", 220.0, 0.045, 0.8),
    MaterialPreset("mat_steel1018", "Mild Steel (AISI 1018)", "Ferrous", 90.0, 0.035, 1.8),
    MaterialPreset("mat_ss304", "Stainless Steel 304", "Exotic / Tough", 55.0, 0.025, 2.2),
    MaterialPreset("mat_wood", "Hardwood / Birch Plywood", "Wood & Composites", 350.0, 0.080, 0.3),
    MaterialPreset("mat_pom", "POM / Delrin / Acetal", "Plastics", 180.0, 0.060, 0.4)
)

@Composable
fun SpeedsFeedsCalculatorDialog(
    onDismiss: () -> Unit,
    onApplyToCnc: (rpm: Double, feedMmMin: Double) -> Unit
) {
    var selectedMaterial by remember { mutableStateOf(STANDARD_MATERIALS[0]) }
    var toolDiameter by remember { mutableStateOf(6.0) }
    var flutes by remember { mutableStateOf(2) }
    var isRoughing by remember { mutableStateOf(false) }

    // Formula Calculations
    val vc = if (isRoughing) selectedMaterial.surfaceSpeedMMin * 0.85 else selectedMaterial.surfaceSpeedMMin
    val calculatedRpm = ((vc * 1000.0) / (PI * toolDiameter)).coerceIn(500.0, 24000.0)
    val fz = if (isRoughing) selectedMaterial.feedPerToothMm * 1.3 else selectedMaterial.feedPerToothMm
    val calculatedFeed = (calculatedRpm * flutes * fz).coerceIn(50.0, 10000.0)
    val docAp = if (isRoughing) toolDiameter * 0.75 else toolDiameter * 0.25
    val wocAe = if (isRoughing) toolDiameter * 0.4 else toolDiameter * 0.1
    val estimatedPowerKw = (calculatedFeed * docAp * wocAe * selectedMaterial.powerFactor / 60000.0).coerceAtLeast(0.1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = CncCyberCyan, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SPEEDS & FEEDS CALCULATOR", fontWeight = FontWeight.Black, fontSize = 14.sp, color = CncTextPrimary)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Material Selector
                Text("TARGET WORKPIECE MATERIAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CncTextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    STANDARD_MATERIALS.forEach { mat ->
                        val isSelected = selectedMaterial.id == mat.id
                        Surface(
                            color = if (isSelected) CncSurfaceVariant else CncSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) CncCyberCyan else CncCardBorder)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMaterial = mat }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(mat.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CncTextPrimary)
                                    Text("Vc: ${mat.surfaceSpeedMMin.toInt()} m/min • ${mat.category}", fontSize = 9.sp, color = CncTextSecondary)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = CncActiveGreen, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Tool Diameter and Flutes Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DIAMETER (mm)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CncTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(3.0, 6.0, 8.0, 12.0).forEach { dia ->
                                val isSel = toolDiameter == dia
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSel) CncCyberCyan else CncSurface)
                                        .border(1.dp, CncCardBorder, RoundedCornerShape(4.dp))
                                        .clickable { toolDiameter = dia }
                                ) {
                                    Text("${dia.toInt()}mm", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF00363D) else CncTextPrimary)
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("FLUTES (Z)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CncTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1, 2, 3, 4).forEach { f ->
                                val isSel = flutes == f
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSel) CncCyberCyan else CncSurface)
                                        .border(1.dp, CncCardBorder, RoundedCornerShape(4.dp))
                                        .clickable { flutes = f }
                                ) {
                                    Text("${f}F", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF00363D) else CncTextPrimary)
                                }
                            }
                        }
                    }
                }

                // Results Summary Display Card
                Surface(
                    color = CncSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCyberCyan)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SPINDLE SPEED", fontSize = 9.sp, color = CncWarningAmber, fontWeight = FontWeight.Bold)
                                Text("${calculatedRpm.roundToInt()} RPM", fontSize = 14.sp, fontWeight = FontWeight.Black, color = CncTextPrimary, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FEEDRATE (F)", fontSize = 9.sp, color = CncActiveGreen, fontWeight = FontWeight.Bold)
                                Text("${calculatedFeed.roundToInt()} mm/min", fontSize = 14.sp, fontWeight = FontWeight.Black, color = CncTextPrimary, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Divider(color = CncCardBorder)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Depth (Ap): ${String.format(Locale.US, "%.2f", docAp)} mm", fontSize = 10.sp, color = CncTextSecondary)
                            Text("Stepover (Ae): ${String.format(Locale.US, "%.2f", wocAe)} mm", fontSize = 10.sp, color = CncTextSecondary)
                            Text("Power: ${String.format(Locale.US, "%.2f", estimatedPowerKw)} kW", fontSize = 10.sp, color = CncCyberCyan)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyToCnc(calculatedRpm, calculatedFeed)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CncCyberCyan,
                    contentColor = Color(0xFF00363D)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = "Apply")
                Spacer(modifier = Modifier.width(4.dp))
                Text("APPLY TO CNC", fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = CncTextSecondary)
            }
        },
        containerColor = CncCardBg
    )
}
