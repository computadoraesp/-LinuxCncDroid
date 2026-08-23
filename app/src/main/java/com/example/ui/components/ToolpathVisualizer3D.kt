package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AxisCoord
import com.example.model.GCodeSegment
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

enum class ViewPerspective {
    TOP_XY,
    FRONT_XZ,
    SIDE_YZ,
    ISO_3D
}

@Composable
fun ToolpathVisualizer3D(
    gcodeList: List<GCodeSegment>,
    activeLineIndex: Int,
    axes: Map<String, AxisCoord>,
    fileName: String = "face_pocket_contour.ngc",
    elapsedSeconds: Long = 0L,
    estimatedTotalSeconds: Long = 180L,
    feedRate: Double = 1500.0,
    spindleRpm: Double = 18000.0,
    activeToolDiameter: Double = 6.0,
    onOpenLoader: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var perspective by remember { mutableStateOf(ViewPerspective.ISO_3D) }
    var zoomScale by remember { mutableFloatStateOf(3.2f) }
    var panOffset by remember { mutableStateOf(Offset(180f, 190f)) }

    val listState = rememberLazyListState()

    // Auto-scroll G-Code tracker
    LaunchedEffect(activeLineIndex) {
        if (gcodeList.isNotEmpty() && activeLineIndex in gcodeList.indices) {
            listState.animateScrollToItem(max(0, activeLineIndex - 2))
        }
    }

    // Workpiece Bounding Box Calculation
    val boundingBox = remember(gcodeList) {
        if (gcodeList.isEmpty()) {
            Triple(0f to 50f, 0f to 50f, -2.5f to 10f)
        } else {
            var minX = Float.MAX_VALUE; var maxX = Float.MIN_VALUE
            var minY = Float.MAX_VALUE; var maxY = Float.MIN_VALUE
            var minZ = Float.MAX_VALUE; var maxZ = Float.MIN_VALUE

            gcodeList.forEach { seg ->
                minX = min(minX, min(seg.startX, seg.endX))
                maxX = max(maxX, max(seg.startX, seg.endX))
                minY = min(minY, min(seg.startY, seg.endY))
                maxY = max(maxY, max(seg.startY, seg.endY))
                minZ = min(minZ, min(seg.startZ, seg.endZ))
                maxZ = max(maxZ, max(seg.startZ, seg.endZ))
            }
            Triple(minX to maxX, minY to maxY, minZ to maxZ)
        }
    }

    val progressPct = remember(activeLineIndex, gcodeList) {
        if (gcodeList.isEmpty()) 0f else ((activeLineIndex + 1).toFloat() / gcodeList.size.toFloat()).coerceIn(0f, 1f)
    }

    val remainingSeconds = remember(progressPct, estimatedTotalSeconds, elapsedSeconds) {
        max(0L, (estimatedTotalSeconds * (1f - progressPct)).toLong())
    }

    val surfaceSpeedMMin = remember(activeToolDiameter, spindleRpm) {
        (PI * activeToolDiameter * spindleRpm) / 1000.0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CncCardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CncCardBorder)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ViewInAr, contentDescription = "3D View", tint = CncCyberCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("3D REAL-TIME TOOLPATH & TOOLHEAD", fontWeight = FontWeight.Black, fontSize = 11.5.sp, color = CncTextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[$fileName]",
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CncWarningAmber,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledTonalButton(
                        onClick = onOpenLoader,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CncSurfaceVariant,
                            contentColor = CncCyberCyan
                        ),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = "Scan & Load", modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LOAD / SCAN", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Perspective Buttons
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CncSurfaceVariant)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf(
                            ViewPerspective.TOP_XY to "TOP",
                            ViewPerspective.ISO_3D to "ISO 3D",
                            ViewPerspective.FRONT_XZ to "FRONT"
                        ).forEach { (view, label) ->
                            val isSelected = perspective == view
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) CncCyberCyan else Color.Transparent)
                                    .clickable { perspective = view }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF00363D) else CncTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Visualizer & GCode split
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 3D/2D Canvas Visualizer
                Box(
                    modifier = Modifier
                        .weight(1.55f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF080D16))
                        .border(1.dp, CncCardBorder, RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(1.0f, 15.0f)
                                panOffset += pan
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Grid lines
                        val gridSpacing = 20f * zoomScale
                        val startX = (panOffset.x % gridSpacing)
                        val startY = (panOffset.y % gridSpacing)

                        var curX = startX
                        while (curX < canvasWidth) {
                            drawLine(
                                color = Color(0xFF132238),
                                start = Offset(curX, 0f),
                                end = Offset(curX, canvasHeight),
                                strokeWidth = 1f
                            )
                            curX += gridSpacing
                        }

                        var curY = startY
                        while (curY < canvasHeight) {
                            drawLine(
                                color = Color(0xFF132238),
                                start = Offset(0f, curY),
                                end = Offset(canvasWidth, curY),
                                strokeWidth = 1f
                            )
                            curY += gridSpacing
                        }

                        // Origin Axis Crosshair (0,0)
                        val originX = panOffset.x
                        val originY = panOffset.y
                        drawLine(
                            color = AxisXColor.copy(alpha = 0.8f),
                            start = Offset(originX, originY),
                            end = Offset(originX + 35f, originY),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = AxisYColor.copy(alpha = 0.8f),
                            start = Offset(originX, originY),
                            end = Offset(originX, originY - 35f),
                            strokeWidth = 2f
                        )

                        // Render G-Code Toolpath Segments
                        gcodeList.forEachIndexed { index, seg ->
                            val isCurrent = index == activeLineIndex

                            val p1 = when (perspective) {
                                ViewPerspective.TOP_XY -> Offset(originX + seg.startX * zoomScale, originY - seg.startY * zoomScale)
                                ViewPerspective.FRONT_XZ -> Offset(originX + seg.startX * zoomScale, originY - seg.startZ * zoomScale)
                                ViewPerspective.SIDE_YZ -> Offset(originX + seg.startY * zoomScale, originY - seg.startZ * zoomScale)
                                ViewPerspective.ISO_3D -> {
                                    val isoX = originX + (seg.startX - seg.startY * 0.7f) * zoomScale * 0.8f
                                    val isoY = originY - (seg.startZ + (seg.startX + seg.startY) * 0.35f) * zoomScale * 0.8f
                                    Offset(isoX, isoY)
                                }
                            }

                            val p2 = when (perspective) {
                                ViewPerspective.TOP_XY -> Offset(originX + seg.endX * zoomScale, originY - seg.endY * zoomScale)
                                ViewPerspective.FRONT_XZ -> Offset(originX + seg.endX * zoomScale, originY - seg.endZ * zoomScale)
                                ViewPerspective.SIDE_YZ -> Offset(originX + seg.endY * zoomScale, originY - seg.endZ * zoomScale)
                                ViewPerspective.ISO_3D -> {
                                    val isoX = originX + (seg.endX - seg.endY * 0.7f) * zoomScale * 0.8f
                                    val isoY = originY - (seg.endZ + (seg.endX + seg.endY) * 0.35f) * zoomScale * 0.8f
                                    Offset(isoX, isoY)
                                }
                            }

                            if (seg.isRapid) {
                                drawLine(
                                    color = if (isCurrent) CncActiveGreen else CncWarningAmber.copy(alpha = 0.6f),
                                    start = p1,
                                    end = p2,
                                    strokeWidth = if (isCurrent) 3f else 1.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                )
                            } else if (seg.isCut) {
                                drawLine(
                                    color = if (isCurrent) CncActiveGreen else CncCyberCyan,
                                    start = p1,
                                    end = p2,
                                    strokeWidth = if (isCurrent) 4f else 2f
                                )
                            }
                        }

                        // Current Tool Location Marker & 3D Toolhead Rendering
                        val toolX = axes["X"]?.workPos?.toFloat() ?: 0f
                        val toolY = axes["Y"]?.workPos?.toFloat() ?: 0f
                        val toolZ = axes["Z"]?.workPos?.toFloat() ?: 0f

                        val toolPos = when (perspective) {
                            ViewPerspective.TOP_XY -> Offset(originX + toolX * zoomScale, originY - toolY * zoomScale)
                            ViewPerspective.FRONT_XZ -> Offset(originX + toolX * zoomScale, originY - toolZ * zoomScale)
                            ViewPerspective.SIDE_YZ -> Offset(originX + toolY * zoomScale, originY - toolZ * zoomScale)
                            ViewPerspective.ISO_3D -> {
                                val isoX = originX + (toolX - toolY * 0.7f) * zoomScale * 0.8f
                                val isoY = originY - (toolZ + (toolX + toolY) * 0.35f) * zoomScale * 0.8f
                                Offset(isoX, isoY)
                            }
                        }

                        // Draw Realistic 3D Toolholder & Carbide Endmill Flute
                        val toolRadiusPx = (activeToolDiameter.toFloat() / 2f * zoomScale).coerceIn(4f, 16f)

                        // 1. Toolholder ISO Cone Body (Silver/Steel Gradient)
                        val holderPath = Path().apply {
                            moveTo(toolPos.x - toolRadiusPx * 2.2f, toolPos.y - 45f)
                            lineTo(toolPos.x + toolRadiusPx * 2.2f, toolPos.y - 45f)
                            lineTo(toolPos.x + toolRadiusPx * 1.3f, toolPos.y - 20f)
                            lineTo(toolPos.x - toolRadiusPx * 1.3f, toolPos.y - 20f)
                            close()
                        }
                        drawPath(
                            path = holderPath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6B7280), Color(0xFFE5E7EB), Color(0xFF4B5563)),
                                startX = toolPos.x - 20f,
                                endX = toolPos.x + 20f
                            )
                        )

                        // 2. Collet Nut (ER20 / ISO30 Black Oxide)
                        drawRect(
                            color = Color(0xFF1F2937),
                            topLeft = Offset(toolPos.x - toolRadiusPx * 1.4f, toolPos.y - 20f),
                            size = Size(toolRadiusPx * 2.8f, 10f)
                        )

                        // 3. Carbide Cutter Shaft & Flute (Gold/Bronze TiN or Cyan Glow)
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFB45309), Color(0xFFFBBF24), Color(0xFF92400E)),
                                startX = toolPos.x - toolRadiusPx,
                                endX = toolPos.x + toolRadiusPx
                            ),
                            topLeft = Offset(toolPos.x - toolRadiusPx, toolPos.y - 10f),
                            size = Size(toolRadiusPx * 2f, 10f)
                        )

                        // 4. Cutting Tip & Spindle Contact Halo
                        drawCircle(
                            color = CncEstopRed,
                            radius = toolRadiusPx.coerceAtLeast(3f),
                            center = toolPos
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(CncCyberCyan.copy(alpha = 0.6f), Color.Transparent),
                                center = toolPos,
                                radius = 22f
                            ),
                            radius = 22f,
                            center = toolPos
                        )
                    }

                    // Zoom / Reset Overlay controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { zoomScale = (zoomScale * 1.3f).coerceAtMost(15f) },
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CncSurfaceVariant.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = CncTextPrimary, modifier = Modifier.size(15.dp))
                        }
                        IconButton(
                            onClick = { zoomScale = (zoomScale / 1.3f).coerceAtLeast(1f) },
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CncSurfaceVariant.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = CncTextPrimary, modifier = Modifier.size(15.dp))
                        }
                        IconButton(
                            onClick = {
                                zoomScale = 3.2f
                                panOffset = Offset(180f, 190f)
                            },
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CncSurfaceVariant.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset View", tint = CncTextPrimary, modifier = Modifier.size(15.dp))
                        }
                    }
                }

                // G-Code Line-By-Line Tracker List
                Surface(
                    color = CncSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CncCardBorder)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        itemsIndexed(gcodeList) { index, seg ->
                            val isCurrent = index == activeLineIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isCurrent) CncActiveGreen.copy(alpha = 0.25f) else Color.Transparent)
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%03d", seg.lineNumber),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isCurrent) CncActiveGreen else CncTextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = seg.rawText,
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isCurrent) CncTextPrimary else CncTextSecondary,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cycle Runtime & Cutting Telemetry Clock Bar
            Surface(
                color = CncSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = "Timer", tint = CncCyberCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "RUN: ${String.format(java.util.Locale.US, "%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)}",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CncTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REM: ${String.format(java.util.Locale.US, "%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)}",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CncWarningAmber
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BLOCK ${activeLineIndex + 1}/${gcodeList.size} (${(progressPct * 100).toInt()}%)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = CncCyberCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Vc: ${surfaceSpeedMMin.toInt()} m/min",
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CncTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progressPct },
                        color = CncActiveGreen,
                        trackColor = CncCardBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}
