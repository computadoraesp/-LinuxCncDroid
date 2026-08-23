package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProbeInfo
import com.example.ui.theme.*
import java.util.Locale

data class ProbeRoutineItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gcodeMacro: String
)

@Composable
fun ProbingView(
    probeInfo: ProbeInfo,
    onExecuteRoutine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val routines = listOf(
        ProbeRoutineItem("bore_center", "Internal Bore Center", "Finds (X, Y) center of circle using 4-point probing", Icons.Default.RadioButtonUnchecked, "O100 CALL [BORE_CENTER]"),
        ProbeRoutineItem("boss_center", "External Boss Center", "Finds external circular boss center", Icons.Default.Adjust, "O101 CALL [BOSS_CENTER]"),
        ProbeRoutineItem("corner_out", "Outside Corner Finder", "Probes X+ and Y+ to locate corner zero", Icons.Default.CropFree, "O102 CALL [CORNER_OUT]"),
        ProbeRoutineItem("corner_in", "Inside Pocket Corner", "Finds inside pocket origin vertex", Icons.Default.FullscreenExit, "O103 CALL [CORNER_IN]"),
        ProbeRoutineItem("edge_x", "X-Axis Edge Touch", "Single touch on X face to set X zero", Icons.Default.CompareArrows, "O104 CALL [EDGE_X]"),
        ProbeRoutineItem("edge_y", "Y-Axis Edge Touch", "Single touch on Y face to set Y zero", Icons.Default.SwapVert, "O105 CALL [EDGE_Y]"),
        ProbeRoutineItem("toolsetter_z", "Toolsetter Z-Touch", "Auto tool length measurement with reference puck", Icons.Default.VerticalAlignBottom, "O106 CALL [TOOLSETTER_Z]")
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CncCardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header & Sensor Tripped Status LED
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Sensors, contentDescription = "Probe", tint = CncCyberCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("3D PROBE & METROLOGY (NIVEL 2)", fontWeight = FontWeight.Black, fontSize = 12.sp, color = CncTextPrimary)
                }

                // Probe Tripped Sensor Indicator
                val isTripped = probeInfo.isTripped
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isTripped) CncEstopRed.copy(alpha = 0.2f) else CncActiveGreen.copy(alpha = 0.15f))
                        .border(1.dp, if (isTripped) CncEstopRed else CncActiveGreen, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isTripped) CncEstopRed else CncActiveGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTripped) "PROBE TRIPPED" else "PROBE READY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTripped) CncEstopRed else CncActiveGreen
                    )
                }
            }

            // Last Contact Coords Card
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
                        Text("LAST CONTACT X", fontSize = 9.sp, color = AxisXColor, fontWeight = FontWeight.Bold)
                        Text(
                            String.format(Locale.US, "%+07.3f", probeInfo.lastContactX),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CncDroDigits
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LAST CONTACT Y", fontSize = 9.sp, color = AxisYColor, fontWeight = FontWeight.Bold)
                        Text(
                            String.format(Locale.US, "%+07.3f", probeInfo.lastContactY),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CncDroDigits
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LAST CONTACT Z", fontSize = 9.sp, color = AxisZColor, fontWeight = FontWeight.Bold)
                        Text(
                            String.format(Locale.US, "%+07.3f", probeInfo.lastContactZ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CncDroDigits
                        )
                    }
                }
            }

            // Routine Selection Grid
            Text("AUTOMATED PROBING CYCLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CncTextSecondary)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                routines.forEach { routine ->
                    val isExecuting = probeInfo.activeRoutine == routine.id
                    Surface(
                        color = if (isExecuting) CncSurfaceVariant else CncSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isExecuting) CncCyberCyan else CncCardBorder)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExecuteRoutine(routine.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CncSurfaceVariant)
                                        .border(1.dp, CncCardBorder, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = routine.icon, contentDescription = routine.title, tint = CncCyberCyan, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(routine.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CncTextPrimary)
                                    Text(routine.description, fontSize = 10.sp, color = CncTextSecondary)
                                }
                            }

                            FilledTonalButton(
                                onClick = { onExecuteRoutine(routine.id) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isExecuting) CncWarningAmber else CncSurfaceVariant,
                                    contentColor = if (isExecuting) Color.Black else CncCyberCyan
                                ),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(if (isExecuting) "PROBING..." else "EXECUTE", fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
