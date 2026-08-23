package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CncToolItem
import com.example.model.ToolType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolTableDialog(
    tools: List<CncToolItem>,
    activeTool: CncToolItem,
    currentSpindleZ: Double,
    onDismiss: () -> Unit,
    onMountTool: (Int) -> Unit,
    onUpdateTool: (CncToolItem) -> Unit,
    onDeleteTool: (Int) -> Unit,
    onTouchOffZ: (Int) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<ToolType?>(null) }
    var editingTool by remember { mutableStateOf<CncToolItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredTools = remember(tools, selectedFilter) {
        if (selectedFilter == null) tools else tools.filter { it.toolType == selectedFilter }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CncSurfaceBg),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CncCardBorder)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Construction,
                            contentDescription = "Tool Table",
                            tint = CncCyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TOOL TABLE & POCKET MANAGER (tool.tbl)",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = CncTextPrimary
                            )
                            Text(
                                text = "Mounted: T${activeTool.id} • ${activeTool.description} (G43 H${activeTool.id})",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CncWarningAmber
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = CncSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tool", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("NEW TOOL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CncCyberCyan)
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = CncTextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("ALL (${tools.size})", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CncCyberCyan,
                            selectedLabelColor = Color(0xFF00363D)
                        )
                    )

                    ToolType.values().take(4).forEach { type ->
                        val count = tools.count { it.toolType == type }
                        if (count > 0) {
                            FilterChip(
                                selected = selectedFilter == type,
                                onClick = { selectedFilter = if (selectedFilter == type) null else type },
                                label = { Text("${type.displayName} ($count)", fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CncCyberCyan,
                                    selectedLabelColor = Color(0xFF00363D)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tool Table List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTools, key = { it.id }) { toolItem ->
                        ToolCardItem(
                            tool = toolItem,
                            isCurrentlyActive = (toolItem.id == activeTool.id),
                            currentSpindleZ = currentSpindleZ,
                            onMount = { onMountTool(toolItem.id) },
                            onEdit = { editingTool = toolItem },
                            onDelete = { onDeleteTool(toolItem.id) },
                            onTouchOff = { onTouchOffZ(toolItem.id) }
                        )
                    }
                }
            }
        }
    }

    // Edit Tool Dialog
    if (editingTool != null) {
        EditToolDetailsDialog(
            tool = editingTool!!,
            onDismiss = { editingTool = null },
            onSave = { updated ->
                onUpdateTool(updated)
                editingTool = null
            }
        )
    }

    // Add New Tool Dialog
    if (showAddDialog) {
        val nextId = (tools.maxOfOrNull { it.id } ?: 0) + 1
        EditToolDetailsDialog(
            tool = CncToolItem(
                id = nextId,
                pocket = nextId,
                description = "New Carbide Cutter",
                diameter = 6.000,
                lengthOffset = 40.000,
                toolType = ToolType.ENDMILL,
                flutes = 3,
                maxRpm = 24000.0,
                lifeMinutesCurrent = 0.0,
                lifeMinutesMax = 120.0
            ),
            isNew = true,
            onDismiss = { showAddDialog = false },
            onSave = { newTool ->
                onUpdateTool(newTool)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ToolCardItem(
    tool: CncToolItem,
    isCurrentlyActive: Boolean,
    currentSpindleZ: Double,
    onMount: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTouchOff: () -> Unit
) {
    val lifePct = (tool.lifeMinutesCurrent / tool.lifeMinutesMax).toFloat().coerceIn(0f, 1f)
    val lifeColor = if (lifePct > 0.85f) CncEstopRed else if (lifePct > 0.65f) CncWarningAmber else CncRunningGreen

    Surface(
        color = if (isCurrentlyActive) CncSurfaceVariant else CncCardBg,
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isCurrentlyActive) CncCyberCyan else CncCardBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isCurrentlyActive) CncCyberCyan else CncSurfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "T${tool.id}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isCurrentlyActive) Color(0xFF00363D) else CncTextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tool.description,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CncTextPrimary
                            )
                            if (isCurrentlyActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = CncRunningGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "SPINDLE ACTIVE (M6)",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CncRunningGreen,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Pocket #${tool.pocket} • ${tool.toolType.displayName} • ${tool.flutes} Flutes • Max ${tool.maxRpm.toInt()} RPM",
                            fontSize = 9.5.sp,
                            color = CncTextSecondary
                        )
                    }
                }

                // Mount / Edit Actions
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!isCurrentlyActive) {
                        FilledTonalButton(
                            onClick = onMount,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = CncSurfaceVariant),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Mount", modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("MOUNT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CncCyberCyan)
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Tool", tint = CncTextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tool Geometry & Wear Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Diameter
                Surface(
                    color = CncSurfaceBg,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("DIAMETER (D)", fontSize = 8.sp, color = CncTextSecondary)
                        Text("Ø${String.format(java.util.Locale.US, "%.3f", tool.diameter)} mm", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CncTextPrimary)
                    }
                }

                // Length Offset (H)
                Surface(
                    color = CncSurfaceBg,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("LENGTH OFFSET (H)", fontSize = 8.sp, color = CncTextSecondary)
                        Text("${String.format(java.util.Locale.US, "%.3f", tool.lengthOffset)} mm", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CncWarningAmber)
                    }
                }

                // Touch-Off Tool Z Button
                Surface(
                    color = CncSurfaceBg,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable { onTouchOff() }
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TOUCH-OFF (G43.1)", fontSize = 8.sp, color = CncCyberCyan, fontWeight = FontWeight.Bold)
                            Text("SET CURRENT Z", fontSize = 9.sp, fontWeight = FontWeight.Black, color = CncTextPrimary)
                        }
                        Icon(imageVector = Icons.Default.VerticalAlignBottom, contentDescription = "Touch Off", tint = CncCyberCyan, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tool Wear & Life Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tool Life: ${String.format(java.util.Locale.US, "%.1f", tool.lifeMinutesCurrent)} / ${tool.lifeMinutesMax.toInt()} min (${(lifePct * 100).toInt()}%)",
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CncTextSecondary
                )
                LinearProgressIndicator(
                    progress = { lifePct },
                    color = lifeColor,
                    trackColor = CncSurfaceVariant,
                    modifier = Modifier
                        .width(100.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
fun EditToolDetailsDialog(
    tool: CncToolItem,
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (CncToolItem) -> Unit
) {
    var toolNumberStr by remember { mutableStateOf(tool.id.toString()) }
    var pocketStr by remember { mutableStateOf(tool.pocket.toString()) }
    var description by remember { mutableStateOf(tool.description) }
    var diameterStr by remember { mutableStateOf(tool.diameter.toString()) }
    var lengthOffsetStr by remember { mutableStateOf(tool.lengthOffset.toString()) }
    var flutesStr by remember { mutableStateOf(tool.flutes.toString()) }
    var maxRpmStr by remember { mutableStateOf(tool.maxRpm.toInt().toString()) }
    var selectedType by remember { mutableStateOf(tool.toolType) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CncSurfaceBg),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CncCardBorder)),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isNew) "ADD NEW TOOL (tool.tbl)" else "EDIT TOOL T${tool.id}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = CncTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = toolNumberStr,
                        onValueChange = { toolNumberStr = it },
                        label = { Text("Tool (T#)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pocketStr,
                        onValueChange = { pocketStr = it },
                        label = { Text("Pocket (P#)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = diameterStr,
                        onValueChange = { diameterStr = it },
                        label = { Text("Diameter (mm)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lengthOffsetStr,
                        onValueChange = { lengthOffsetStr = it },
                        label = { Text("Length Offset H", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = flutesStr,
                        onValueChange = { flutesStr = it },
                        label = { Text("Flutes", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxRpmStr,
                        onValueChange = { maxRpmStr = it },
                        label = { Text("Max RPM", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = CncTextSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val id = toolNumberStr.toIntOrNull() ?: tool.id
                            val pocket = pocketStr.toIntOrNull() ?: tool.pocket
                            val diam = diameterStr.toDoubleOrNull() ?: tool.diameter
                            val len = lengthOffsetStr.toDoubleOrNull() ?: tool.lengthOffset
                            val flutes = flutesStr.toIntOrNull() ?: tool.flutes
                            val rpm = maxRpmStr.toDoubleOrNull() ?: tool.maxRpm

                            onSave(
                                tool.copy(
                                    id = id,
                                    pocket = pocket,
                                    description = description,
                                    diameter = diam,
                                    lengthOffset = len,
                                    toolType = selectedType,
                                    flutes = flutes,
                                    maxRpm = rpm
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CncCyberCyan)
                    ) {
                        Text("SAVE TOOL", color = Color(0xFF00363D), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
