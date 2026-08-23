package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HardwareArchitecture
import com.example.model.MachineStateEnum
import com.example.model.UserRole
import com.example.ui.theme.*

@Composable
fun IndustrialTopBar(
    machineState: MachineStateEnum,
    currentCoordSystem: String,
    architecture: HardwareArchitecture,
    userRole: UserRole,
    isSimulated: Boolean,
    latencyMs: Int = 2,
    errorCount: Int = 0,
    onToggleEstop: () -> Unit,
    onPowerOn: () -> Unit,
    onPowerOff: () -> Unit,
    onSelectCoordSystem: (String) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onOpenConfig: () -> Unit,
    onOpenCyberScanner: () -> Unit = {},
    onOpenCalculator: () -> Unit = {},
    onOpenToolTable: () -> Unit = {},
    onOpenLogs: () -> Unit = {},
    onOpenAxisCalibration: () -> Unit = {},
    onOpenManual: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var coordMenuExpanded by remember { mutableStateOf(false) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    val coordSystems = listOf("G54", "G55", "G56", "G57", "G58", "G59", "G59.1", "G59.2", "G59.3")

    Surface(
        color = CncSurface,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = CncCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Group: ESTOP & Power Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Emergency Stop Big Button
                val isEstop = machineState == MachineStateEnum.ESTOP
                val estopBg by animateColorAsState(
                    targetValue = if (isEstop) CncEstopRed else Color(0xFF3E1218),
                    label = "estop_color"
                )

                Button(
                    onClick = onToggleEstop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = estopBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .border(
                            width = 2.dp,
                            color = if (isEstop) Color.White else CncEstopRed,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isEstop) Icons.Default.Warning else Icons.Default.Block,
                        contentDescription = "ESTOP Button",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEstop) "E-STOP TRIPPED" else "E-STOP",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Power ON / OFF Button
                if (!isEstop) {
                    val isPowerOn = machineState == MachineStateEnum.ON || machineState == MachineStateEnum.RUNNING || machineState == MachineStateEnum.IDLE || machineState == MachineStateEnum.PAUSED
                    IconButton(
                        onClick = { if (isPowerOn) onPowerOff() else onPowerOn() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPowerOn) Color(0xFF003919) else Color(0xFF263238))
                            .border(
                                1.dp,
                                if (isPowerOn) CncActiveGreen else CncTextMuted,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Machine Power",
                            tint = if (isPowerOn) CncActiveGreen else CncTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // State Pill
                val stateColor = when (machineState) {
                    MachineStateEnum.ESTOP -> CncEstopRed
                    MachineStateEnum.RUNNING -> CncActiveGreen
                    MachineStateEnum.PAUSED -> CncWarningAmber
                    MachineStateEnum.ON, MachineStateEnum.IDLE -> CncCyberCyan
                    else -> CncTextMuted
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(stateColor.copy(alpha = 0.15f))
                        .border(1.dp, stateColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(stateColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = machineState.displayName,
                            color = stateColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Center / Right Group: Tools, Latency, WCS, Role & Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Latency Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CncSurfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Text(
                        "${latencyMs}ms",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CncActiveGreen
                    )
                }

                // Cybersecurity & G-Code Scanner Button
                IconButton(
                    onClick = onOpenCyberScanner,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CncSurfaceVariant)
                        .border(1.dp, CncCyberCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Cybersecurity Scanner",
                        tint = CncCyberCyan,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Speeds & Feeds Calculator Button
                IconButton(
                    onClick = onOpenCalculator,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CncSurfaceVariant)
                        .border(1.dp, CncCardBorder, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Speeds and Feeds",
                        tint = CncWarningAmber,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Tool Table & Pocket Manager Button
                IconButton(
                    onClick = onOpenToolTable,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CncSurfaceVariant)
                        .border(1.dp, CncCyberCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = "Tool Table Manager",
                        tint = CncCyberCyan,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Metrological Axis Calibration (ISO 230-2) Button
                IconButton(
                    onClick = onOpenAxisCalibration,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CncSurfaceVariant)
                        .border(1.dp, CncActiveGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Straighten,
                        contentDescription = "Axis Metrology Calibration",
                        tint = CncActiveGreen,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // In-App User Manual & Technical Reference Button
                IconButton(
                    onClick = onOpenManual,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CncSurfaceVariant)
                        .border(1.dp, CncCyberCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "CNC Manual & SOPs",
                        tint = CncCyberCyan,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Event & Alarm Logs Button with Badge
                IconButton(
                    onClick = onOpenLogs,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (errorCount > 0) CncEstopRed.copy(alpha = 0.2f) else CncSurfaceVariant)
                        .border(1.dp, if (errorCount > 0) CncEstopRed else CncCardBorder, RoundedCornerShape(6.dp))
                ) {
                    BadgedBox(
                        badge = {
                            if (errorCount > 0) {
                                Badge(
                                    containerColor = CncEstopRed,
                                    contentColor = Color.White
                                ) {
                                    Text("$errorCount", fontSize = 8.sp)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Event Logs",
                            tint = if (errorCount > 0) CncEstopRed else CncTextPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Coordinate System Dropdown (G54 - G59.3)
                Box {
                    OutlinedButton(
                        onClick = { coordMenuExpanded = true },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CncCyberCyan,
                            containerColor = CncSurfaceVariant
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CncCyberCyan.copy(alpha = 0.6f))),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = currentCoordSystem,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    DropdownMenu(
                        expanded = coordMenuExpanded,
                        onDismissRequest = { coordMenuExpanded = false },
                        modifier = Modifier.background(CncCardBg)
                    ) {
                        coordSystems.forEach { gCoord ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "$gCoord Work Coordinate",
                                        color = if (gCoord == currentCoordSystem) CncCyberCyan else CncTextPrimary,
                                        fontWeight = if (gCoord == currentCoordSystem) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSelectCoordSystem(gCoord)
                                    coordMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // User Role Selector
                Box {
                    IconButton(
                        onClick = { roleMenuExpanded = true },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CncSurfaceVariant)
                            .border(1.dp, CncCardBorder, RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            imageVector = when (userRole) {
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                UserRole.OPERATOR -> Icons.Default.Engineering
                                UserRole.VIEWER -> Icons.Default.Visibility
                            },
                            contentDescription = "User Role",
                            tint = when (userRole) {
                                UserRole.ADMIN -> CncWarningAmber
                                UserRole.OPERATOR -> CncActiveGreen
                                UserRole.VIEWER -> CncInfoBlue
                            },
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false },
                        modifier = Modifier.background(CncCardBg)
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = role.displayName,
                                        color = if (role == userRole) CncActiveGreen else CncTextPrimary
                                    )
                                },
                                onClick = {
                                    onSelectRole(role)
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Settings / Config Button
                IconButton(
                    onClick = onOpenConfig,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CncSurfaceVariant)
                        .border(1.dp, CncCardBorder, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CncTextPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
