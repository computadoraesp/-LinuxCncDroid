package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.MachineProfileEntity
import com.example.model.CapabilitiesManifest
import com.example.model.HardwareArchitecture
import com.example.ui.theme.*

@Composable
fun MachineConfigView(
    capabilities: CapabilitiesManifest,
    profiles: List<MachineProfileEntity>,
    onSwitchArchitecture: (HardwareArchitecture) -> Unit,
    onConnectHost: (String, Int) -> Unit,
    onSaveProfile: (String, String, Int, String) -> Unit,
    onOpenMetrologyCalibration: () -> Unit = {},
    onOpenManual: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var hostIpText by remember { mutableStateOf(capabilities.hostIp) }
    var portText by remember { mutableStateOf(capabilities.port.toString()) }
    var profileNameText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CncCardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Quick Tools Bar (Metrology & Manual)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenMetrologyCalibration,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CncActiveGreen, containerColor = CncSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CncActiveGreen.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Straighten, contentDescription = null, tint = CncActiveGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CALIBRACIÓN METROLÓGICA (ISO 230-2)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenManual,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CncCyberCyan, containerColor = CncSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CncCyberCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = CncCyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MANUAL TÉCNICO & SOPS", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SettingsEthernet, contentDescription = "Config", tint = CncCyberCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("HARDWARE ABSTRACTION & PROFILES", fontWeight = FontWeight.Black, fontSize = 12.sp, color = CncTextPrimary)
                }

                FilledTonalButton(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CncSurfaceVariant,
                        contentColor = CncCyberCyan
                    ),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Profile", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("NEW PROFILE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // App Identity & Icon Banner
            Surface(
                color = CncSurface,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.linuxcnc_droid_icon_1787496771237),
                        contentDescription = "LinuxCNC Droid Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, CncCyberCyan, RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("LinuxCNC Droid HMI", fontWeight = FontWeight.Black, fontSize = 14.sp, color = CncTextPrimary)
                        Text("Universal Industrial Controller • Level 1-3", fontSize = 11.sp, color = CncCyberCyan, fontWeight = FontWeight.Bold)
                        Text("LinuxCNC NML / HAL • EtherCAT • Delta ASDA-B3", fontSize = 10.sp, color = CncTextSecondary)
                    }
                }
            }

            // Section 1: Active Connection Settings (IP / Port / Connect)
            Surface(
                color = CncSurface,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("LINUXCNC MIDDLEWARE SERVER (WEBSOCKET / REST)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CncTextSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = hostIpText,
                            onValueChange = { hostIpText = it },
                            label = { Text("Host IP / Hostname", fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CncCyberCyan,
                                unfocusedTextColor = CncTextPrimary
                            ),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(2f)
                        )

                        OutlinedTextField(
                            value = portText,
                            onValueChange = { portText = it },
                            label = { Text("Port", fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CncCyberCyan,
                                unfocusedTextColor = CncTextPrimary
                            ),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { onConnectHost(hostIpText, portText.toIntOrNull() ?: 8000) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CncCyberCyan,
                                contentColor = Color(0xFF00363D)
                            ),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("CONNECT", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Section 2: Hardware Architecture Simulation & Overrides
            Text("TARGET HARDWARE ARCHITECTURE (CAPABILITIES DISCOVERY)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CncTextSecondary)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HardwareArchitecture.values().forEach { arch ->
                    val isSelected = capabilities.architecture == arch

                    Surface(
                        color = if (isSelected) CncSurfaceVariant else CncSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) CncCyberCyan else CncCardBorder)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSwitchArchitecture(arch) }
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
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) CncCyberCyan else CncCardBorder)
                                ) {
                                    Text("L${arch.level}", fontWeight = FontWeight.Black, fontSize = 11.sp, color = if (isSelected) Color(0xFF00363D) else CncTextPrimary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(arch.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CncTextPrimary)
                                    Text(arch.description, fontSize = 10.sp, color = CncTextSecondary)
                                }
                            }

                            if (isSelected) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Active", tint = CncActiveGreen, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Section 3: Saved Machine Profiles List
            Text("SAVED MACHINE PROFILES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CncTextSecondary)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                profiles.forEach { profile ->
                    Surface(
                        color = CncSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                hostIpText = profile.hostIp
                                portText = profile.port.toString()
                                val archEnum = HardwareArchitecture.values().find { it.name == profile.architecture } ?: HardwareArchitecture.ETHERCAT_DELTA
                                onSwitchArchitecture(archEnum)
                                onConnectHost(profile.hostIp, profile.port)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CncTextPrimary)
                                Text("${profile.hostIp}:${profile.port} • ${profile.architecture}", fontSize = 10.sp, color = CncTextSecondary, fontFamily = FontFamily.Monospace)
                            }

                            Icon(imageVector = Icons.Default.PlayCircleOutline, contentDescription = "Load Profile", tint = CncCyberCyan)
                        }
                    }
                }
            }
        }
    }

    // Add Profile Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Machine Profile", fontWeight = FontWeight.Bold, color = CncTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = profileNameText,
                        onValueChange = { profileNameText = it },
                        label = { Text("Machine Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hostIpText,
                        onValueChange = { hostIpText = it },
                        label = { Text("IP Address (e.g. 192.168.1.100 or 10.42.0.1)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (profileNameText.isNotBlank()) {
                            onSaveProfile(profileNameText, hostIpText, portText.toIntOrNull() ?: 8000, capabilities.architecture.name)
                            showAddDialog = false
                            profileNameText = ""
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = CncCardBg
        )
    }
}
