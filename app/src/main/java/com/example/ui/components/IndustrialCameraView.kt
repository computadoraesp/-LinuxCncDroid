package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.AxisCoord
import com.example.model.MachineStateEnum
import com.example.model.UnitSystem
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

enum class ReticleType(val label: String) {
    CROSSHAIR("CRUCIFORME (0.01mm)"),
    CONCENTRIC_CIRCLES("CÍRCULOS CONCÉNTRICOS"),
    METROLOGY_GRID("CUADRÍCULA METROLÓGICA"),
    CORNER_FINDER("BUSCADOR DE ESQUINA (EDGE)"),
    NONE("SIN RETÍCULA")
}

@Composable
fun IndustrialCameraView(
    machineState: MachineStateEnum,
    axes: Map<String, AxisCoord>,
    currentWcs: String,
    unitSystem: UnitSystem = UnitSystem.METRIC,
    onJogAxis: (String, Double) -> Unit,
    onZeroAxis: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Camera Controls State
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isFlashOn by remember { mutableStateOf(false) }
    var zoomRatio by remember { mutableFloatStateOf(1.0f) }
    var maxZoomRatio by remember { mutableFloatStateOf(5.0f) }
    var minZoomRatio by remember { mutableFloatStateOf(1.0f) }

    // Reticle & Overlay Configuration
    var selectedReticle by remember { mutableStateOf(ReticleType.CROSSHAIR) }
    var reticleColor by remember { mutableStateOf(CncCyberCyan) }
    var reticleScaleMm by remember { mutableFloatStateOf(10f) } // mm equivalent scale
    var reticleOffsetX by remember { mutableFloatStateOf(0f) }
    var reticleOffsetY by remember { mutableFloatStateOf(0f) }
    var showTelemetryOverlay by remember { mutableStateOf(true) }
    var showEdgeDetectionFilter by remember { mutableStateOf(false) }
    var lastCapturedSnapshotMessage by remember { mutableStateOf<String?>(null) }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CncSurface),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CncCardBorder),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = CncCyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "SISTEMA DE VISIÓN ÓPTICA INDUSTRIAL & CENTRADO (CNC CAM)",
                        color = CncTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Flash / Torch toggle
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraControl?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isFlashOn) CncWarningAmber else CncTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Flip camera
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Flip Camera",
                            tint = CncTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Telemetry toggle
                    IconButton(
                        onClick = { showTelemetryOverlay = !showTelemetryOverlay },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (showTelemetryOverlay) Icons.Default.Layers else Icons.Default.LayersClear,
                            contentDescription = "Telemetry",
                            tint = if (showTelemetryOverlay) CncCyberCyan else CncTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Snapshot capture
                    IconButton(
                        onClick = {
                            val cap = imageCapture
                            if (cap != null) {
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                val photoFile = File(context.cacheDir, "CNC_ALIGN_${timeStamp}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                cap.takePicture(
                                    outputOptions,
                                    cameraExecutor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            lastCapturedSnapshotMessage = "Captura guardada: CNC_ALIGN_${timeStamp}.jpg"
                                        }

                                        override fun onError(exc: ImageCaptureException) {
                                            Log.e("CncCamera", "Snapshot error: ${exc.message}", exc)
                                            lastCapturedSnapshotMessage = "Error en captura: ${exc.message}"
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Take Snapshot",
                            tint = CncActiveGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (!hasCameraPermission) {
                // Request Permission State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CncBackground)
                        .border(1.dp, CncCardBorder, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = null,
                            tint = CncWarningAmber,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "ACCESO A CÁMARA INDUSTRIAL REQUERIDO",
                            color = CncTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Se utiliza la cámara para inspección óptica de filo, centrado de cero de pieza (Optical WCS Alignment), verificación de ranuras y calibración visual de ejes.",
                            color = CncTextSecondary,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = CncCyberCyan, contentColor = CncBackground),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CONCEDER PERMISO DE CÁMARA", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                // Main Camera Feed Box with Overlays
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black)
                        .border(1.dp, CncCyberCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newZoom = (zoomRatio * zoom).coerceIn(minZoomRatio, maxZoomRatio)
                                zoomRatio = newZoom
                                cameraControl?.setZoomRatio(newZoom)

                                reticleOffsetX += pan.x * 0.5f
                                reticleOffsetY += pan.y * 0.5f
                            }
                        }
                ) {
                    // Android CameraX Preview View
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val capture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .build()
                                imageCapture = capture

                                val selector = CameraSelector.Builder()
                                    .requireLensFacing(lensFacing)
                                    .build()

                                try {
                                    cameraProvider.unbindAll()
                                    val cam = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        selector,
                                        preview,
                                        capture
                                    )
                                    cameraControl = cam.cameraControl
                                    cam.cameraInfo.zoomState.observe(lifecycleOwner) { zState ->
                                        if (zState != null) {
                                            minZoomRatio = zState.minZoomRatio
                                            maxZoomRatio = zState.maxZoomRatio.coerceAtMost(8f)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("CncCamera", "Failed to bind camera: ${e.message}", e)
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Reticle Canvas Overlay
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val centerX = size.width / 2f + reticleOffsetX
                        val centerY = size.height / 2f + reticleOffsetY

                        when (selectedReticle) {
                            ReticleType.CROSSHAIR -> {
                                // Full Axis Crosshair
                                drawLine(
                                    color = reticleColor,
                                    start = Offset(0f, centerY),
                                    end = Offset(size.width, centerY),
                                    strokeWidth = 1.5f
                                )
                                drawLine(
                                    color = reticleColor,
                                    start = Offset(centerX, 0f),
                                    end = Offset(centerX, size.height),
                                    strokeWidth = 1.5f
                                )

                                // Micro-tick graduation marks
                                val tickSpacingPx = 40f
                                for (i in -10..10) {
                                    if (i != 0) {
                                        val tickH = if (i % 5 == 0) 18f else 8f
                                        // X ticks
                                        drawLine(
                                            color = reticleColor.copy(alpha = 0.8f),
                                            start = Offset(centerX + i * tickSpacingPx, centerY - tickH / 2),
                                            end = Offset(centerX + i * tickSpacingPx, centerY + tickH / 2),
                                            strokeWidth = 1f
                                        )
                                        // Y ticks
                                        drawLine(
                                            color = reticleColor.copy(alpha = 0.8f),
                                            start = Offset(centerX - tickH / 2, centerY + i * tickSpacingPx),
                                            end = Offset(centerX + tickH / 2, centerY + i * tickSpacingPx),
                                            strokeWidth = 1f
                                        )
                                    }
                                }

                                // Center target ring
                                drawCircle(
                                    color = reticleColor,
                                    radius = 16f,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 1.5f)
                                )
                                drawCircle(
                                    color = reticleColor,
                                    radius = 2.5f,
                                    center = Offset(centerX, centerY)
                                )
                            }

                            ReticleType.CONCENTRIC_CIRCLES -> {
                                val radii = listOf(30f, 60f, 100f, 160f, 240f)
                                radii.forEachIndexed { idx, r ->
                                    val isMajor = idx % 2 == 1
                                    drawCircle(
                                        color = reticleColor.copy(alpha = if (isMajor) 0.9f else 0.5f),
                                        radius = r,
                                        center = Offset(centerX, centerY),
                                        style = Stroke(
                                            width = if (isMajor) 1.5f else 1.0f,
                                            pathEffect = if (!isMajor) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                                        )
                                    )
                                }
                                drawLine(
                                    color = reticleColor.copy(alpha = 0.7f),
                                    start = Offset(centerX - 120f, centerY),
                                    end = Offset(centerX + 120f, centerY),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = reticleColor.copy(alpha = 0.7f),
                                    start = Offset(centerX, centerY - 120f),
                                    end = Offset(centerX, centerY + 120f),
                                    strokeWidth = 1f
                                )
                            }

                            ReticleType.METROLOGY_GRID -> {
                                val gridSize = 50f
                                var gx = (centerX % gridSize)
                                while (gx < size.width) {
                                    drawLine(
                                        color = reticleColor.copy(alpha = 0.25f),
                                        start = Offset(gx, 0f),
                                        end = Offset(gx, size.height),
                                        strokeWidth = 0.75f
                                    )
                                    gx += gridSize
                                }
                                var gy = (centerY % gridSize)
                                while (gy < size.height) {
                                    drawLine(
                                        color = reticleColor.copy(alpha = 0.25f),
                                        start = Offset(0f, gy),
                                        end = Offset(size.width, gy),
                                        strokeWidth = 0.75f
                                    )
                                    gy += gridSize
                                }

                                // Main axes highlight
                                drawLine(
                                    color = reticleColor,
                                    start = Offset(centerX, 0f),
                                    end = Offset(centerX, size.height),
                                    strokeWidth = 1.5f
                                )
                                drawLine(
                                    color = reticleColor,
                                    start = Offset(0f, centerY),
                                    end = Offset(size.width, centerY),
                                    strokeWidth = 1.5f
                                )
                            }

                            ReticleType.CORNER_FINDER -> {
                                // 90-degree corner alignment reticle for part square edge
                                val armLen = 140f
                                drawLine(
                                    color = reticleColor,
                                    start = Offset(centerX, centerY),
                                    end = Offset(centerX + armLen, centerY),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = reticleColor,
                                    start = Offset(centerX, centerY),
                                    end = Offset(centerX, centerY + armLen),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = CncWarningAmber,
                                    start = Offset(centerX, centerY),
                                    end = Offset(centerX - 40f, centerY - 40f),
                                    strokeWidth = 1f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )
                                drawCircle(
                                    color = CncWarningAmber,
                                    radius = 5f,
                                    center = Offset(centerX, centerY)
                                )
                            }

                            ReticleType.NONE -> {}
                        }
                    }

                    // Live Telemetry Overlay HUD (Top-Left)
                    if (showTelemetryOverlay) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .border(1.dp, CncCardBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "OPTICAL ALIGNMENT TELEMETRY",
                                color = CncCyberCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "WCS: $currentWcs | ZOOM: ${String.format(Locale.US, "%.1fx", zoomRatio)}",
                                color = CncTextPrimary,
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            axes.forEach { (axis, data) ->
                                val isRotary = axis in listOf("A", "B", "C")
                                val formattedVal = if (isRotary) {
                                    "${String.format(Locale.US, "%+08.3f", data.workPos)}°"
                                } else {
                                    "${unitSystem.formatPosition(data.workPos)} ${unitSystem.lengthUnit}"
                                }
                                Text(
                                    text = "$axis: $formattedVal",
                                    color = if (axis == "Z") CncWarningAmber else CncDroDigits,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Quick Reticle Centering & Reset (Top-Right)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (reticleOffsetX != 0f || reticleOffsetY != 0f) {
                            Button(
                                onClick = {
                                    reticleOffsetX = 0f
                                    reticleOffsetY = 0f
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CncSurfaceVariant, contentColor = CncWarningAmber),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RE-CENTRAR RETÍCULA", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Notification banner for captured photo
                    lastCapturedSnapshotMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CncActiveGreen.copy(alpha = 0.9f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = msg, color = CncBackground, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Control Toolbar: Reticle Selector, Color, Zoom Slider & Direct Micro-Jogging
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reticle Type Selector Chips
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(ReticleType.values()) { rType ->
                            val isSel = selectedReticle == rType
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) CncCyberCyan else CncSurfaceVariant)
                                    .border(1.dp, if (isSel) CncCyberCyan else CncCardBorder, RoundedCornerShape(4.dp))
                                    .clickable { selectedReticle = rType }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = rType.label,
                                    color = if (isSel) CncBackground else CncTextSecondary,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Color Switcher
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(CncCyberCyan, CncActiveGreen, CncWarningAmber, Color.Red, Color.White).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(if (reticleColor == c) 2.dp else 0.5.dp, if (reticleColor == c) Color.White else Color.DarkGray, CircleShape)
                                    .clickable { reticleColor = c }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Micro-Jogging for Optical Part Edge Alignment
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CncSurfaceVariant)
                        .border(1.dp, CncCardBorder, RoundedCornerShape(6.dp))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val microStepMm = if (unitSystem == UnitSystem.IMPERIAL) 0.002 * 25.4 else 0.05
                    val microLabelNeg = if (unitSystem == UnitSystem.IMPERIAL) "-0.002\"" else "-0.05"
                    val microLabelPos = if (unitSystem == UnitSystem.IMPERIAL) "+0.002\"" else "+0.05"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("MICRO-ALINEACIÓN ÓPTICA:", color = CncTextSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)

                        // X Axis Micro Steps
                        Button(
                            onClick = { onJogAxis("X", -microStepMm) },
                            colors = ButtonDefaults.buttonColors(containerColor = CncSurface, contentColor = CncDroDigits),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("X $microLabelNeg", fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { onJogAxis("X", microStepMm) },
                            colors = ButtonDefaults.buttonColors(containerColor = CncSurface, contentColor = CncDroDigits),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("X $microLabelPos", fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }

                        // Y Axis Micro Steps
                        Button(
                            onClick = { onJogAxis("Y", -microStepMm) },
                            colors = ButtonDefaults.buttonColors(containerColor = CncSurface, contentColor = CncDroDigits),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Y $microLabelNeg", fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { onJogAxis("Y", microStepMm) },
                            colors = ButtonDefaults.buttonColors(containerColor = CncSurface, contentColor = CncDroDigits),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Y $microLabelPos", fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Zero X/Y from Camera crosshair center
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = { onZeroAxis("X") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CncCyberCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CncCyberCyan),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("CERO X", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onZeroAxis("Y") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CncCyberCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CncCyberCyan),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("CERO Y", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
