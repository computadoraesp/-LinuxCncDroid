package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.model.MachineStateEnum
import com.example.data.local.MdiMacroEntity
import com.example.ui.theme.*

@Composable
fun MdiView(
    machineState: MachineStateEnum = MachineStateEnum.IDLE,
    commandText: String,
    history: List<String>,
    macros: List<MdiMacroEntity>,
    onCommandTextChange: (String) -> Unit,
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEnabled = machineState != MachineStateEnum.RUNNING && machineState != MachineStateEnum.ESTOP && machineState != MachineStateEnum.ERROR

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
                    Icon(imageVector = Icons.Default.Terminal, contentDescription = stringResource(R.string.mdi_title), tint = CncCyberCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.mdi_title), fontWeight = FontWeight.Black, fontSize = 12.sp, color = CncTextPrimary)
                }
            }

            // Command Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commandText,
                    onValueChange = onCommandTextChange,
                    enabled = isEnabled,
                    placeholder = { Text(stringResource(R.string.mdi_placeholder), color = CncTextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CncSurface,
                        unfocusedContainerColor = CncSurface,
                        focusedTextColor = CncCyberCyan,
                        unfocusedTextColor = CncTextPrimary,
                        focusedBorderColor = CncCyberCyan,
                        unfocusedBorderColor = CncCardBorder
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { onExecuteCommand(commandText) },
                    enabled = isEnabled && commandText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CncCyberCyan,
                        contentColor = Color(0xFF00363D)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.mdi_execute))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.mdi_execute), fontWeight = FontWeight.Black)
                }
            }

            // Quick G-Code Helper Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("G0 X0 Y0", "G0 Z10", "M3 S12000", "M5", "G28", "G54", "G90", "G91").forEach { gcode ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CncSurfaceVariant)
                            .border(1.dp, CncCardBorder, RoundedCornerShape(4.dp))
                            .clickable(enabled = isEnabled) { onCommandTextChange(gcode) }
                    ) {
                        Text(gcode, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CncCyberCyan)
                    }
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, color = CncCardBorder)

            // Programmable Macros Grid
            Text(stringResource(R.string.mdi_macros_header), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CncTextSecondary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                macros.take(4).forEach { macro ->
                    Surface(
                        color = CncSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = isEnabled) { onExecuteCommand(macro.command) }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(macro.label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CncWarningAmber)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(macro.description, fontSize = 9.sp, color = CncTextSecondary, maxLines = 2)
                        }
                    }
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, color = CncCardBorder)

            // Command Execution History
            Text(stringResource(R.string.mdi_history_header), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CncTextSecondary)

            Surface(
                color = CncSurface,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(6.dp)) {
                    items(history) { cmd ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(enabled = isEnabled) { onCommandTextChange(cmd) }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(">", color = CncActiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(cmd, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CncTextPrimary)
                        }
                    }
                }
            }
        }
    }
}
