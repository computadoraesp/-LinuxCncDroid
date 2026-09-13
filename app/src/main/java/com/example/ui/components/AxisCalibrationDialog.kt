package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.model.AxisCalibrationPoint
import com.example.model.AxisCalibrationSession
import com.example.ui.theme.CncActiveGreen
import com.example.ui.theme.CncBackground
import com.example.ui.theme.CncCardBorder
import com.example.ui.theme.CncCyberCyan
import com.example.ui.theme.CncDroDigits
import com.example.ui.theme.CncEstopRed
import com.example.ui.theme.CncSurface
import com.example.ui.theme.CncSurfaceVariant
import com.example.ui.theme.CncTextMuted
import com.example.ui.theme.CncTextPrimary
import com.example.ui.theme.CncTextSecondary
import com.example.ui.theme.CncWarningAmber
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AxisCalibrationDialog(
    session: AxisCalibrationSession?,
    onDismiss: () -> Unit,
    onStartSession: (axis: String, travel: Double, intervalPercent: Double, instName: String, instUncertainty: Double) -> Unit,
    onRecordPoint: (stepIndex: Int, measuredValue: Double) -> Unit,
    onMoveToNominal: (stepIndex: Int) -> Unit,
    onGenerateCompTable: (session: AxisCalibrationSession) -> String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedAxis by remember { mutableStateOf("X") }
    var totalTravelText by remember { mutableStateOf("600.0") }
    var intervalPercent by remember { mutableDoubleStateOf(10.0) } // 10%
    var instrumentName by remember { mutableStateOf("Dial Indicator (0.001mm)") }
    var instrumentUncertaintyText by remember { mutableStateOf("0.003") }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    var inputMeasuredText by remember { mutableStateOf("") }
    var showCompTableExportDialog by remember { mutableStateOf(value = false) }
    var generatedCompText by remember { mutableStateOf("") }

    val activeSession = session ?: remember {
        AxisCalibrationSession(
            axis = "X",
            totalTravelMm = 600.0,
            stepIntervalPercent = 10.0,
            totalSteps = 11,
            points = (0..10).map { i ->
                AxisCalibrationPoint(
                    stepIndex = i,
                    percentOfTravel = i * 10.0,
                    nominalPositionMm = i * 60.0,
                )
            },
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CncSurface),
            border = BorderStroke(1.dp, CncCyberCyan.copy(alpha = 0.5f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CncCyberCyan.copy(alpha = 0.15f))
                                .border(1.dp, CncCyberCyan, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = "Metrology",
                                tint = CncCyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.calib_header),
                                color = CncCyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = stringResource(R.string.calib_subtitle),
                                color = CncTextSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(CncSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.common_close),
                            tint = CncTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = CncCardBorder,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- SECTION 1: CALIBRATION CONFIGURATION & PRESETS ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CncSurfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.calib_section1_header),
                                    color = CncActiveGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Button(
                                    onClick = {
                                        val travel = totalTravelText.toDoubleOrNull() ?: 600.0
                                        val uInst = instrumentUncertaintyText.toDoubleOrNull() ?: 0.003
                                        onStartSession(selectedAxis, travel, intervalPercent, instrumentName, uInst)
                                        currentStepIndex = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CncCyberCyan),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = stringResource(R.string.common_init),
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.calib_init_btn), color = Color.Black, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Axis Selector
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.calib_axis_label), color = CncTextSecondary, fontSize = 9.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("X", "Y", "Z", "A").forEach { axis ->
                                            val isSel = selectedAxis == axis
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(32.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isSel) CncCyberCyan else CncSurface)
                                                    .border(1.dp, if (isSel) CncCyberCyan else CncCardBorder, RoundedCornerShape(4.dp))
                                                    .clickable { selectedAxis = axis },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = axis,
                                                    color = if (isSel) Color.Black else CncTextPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // Total Travel
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.calib_travel_label), color = CncTextSecondary, fontSize = 9.sp)
                                    OutlinedTextField(
                                        value = totalTravelText,
                                        onValueChange = { totalTravelText = it },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CncSurface,
                                            unfocusedContainerColor = CncSurface,
                                            focusedBorderColor = CncCyberCyan,
                                            unfocusedBorderColor = CncCardBorder,
                                            focusedTextColor = CncDroDigits,
                                            unfocusedTextColor = CncDroDigits
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    )
                                }

                                // Instrument Uncertainty
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(stringResource(R.string.calib_uncertainty_label), color = CncTextSecondary, fontSize = 9.sp)
                                    OutlinedTextField(
                                        value = instrumentUncertaintyText,
                                        onValueChange = { instrumentUncertaintyText = it },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CncSurface,
                                            unfocusedContainerColor = CncSurface,
                                            focusedBorderColor = CncCyberCyan,
                                            unfocusedBorderColor = CncCardBorder,
                                            focusedTextColor = CncWarningAmber,
                                            unfocusedTextColor = CncWarningAmber
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    )
                                }
                            }

                            // Quick instrument presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.calib_presets_label), color = CncTextMuted, fontSize = 8.5.sp)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CncSurface)
                                        .border(0.5.dp, CncCardBorder, RoundedCornerShape(4.dp))
                                        .clickable {
                                            instrumentName = context.getString(R.string.calib_preset_dial)
                                            instrumentUncertaintyText = "0.003"
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(stringResource(R.string.calib_preset_dial), color = CncTextSecondary, fontSize = 8.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CncSurface)
                                        .border(0.5.dp, CncCardBorder, RoundedCornerShape(4.dp))
                                        .clickable {
                                            instrumentName = context.getString(R.string.calib_preset_glass)
                                            instrumentUncertaintyText = "0.001"
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(stringResource(R.string.calib_preset_glass), color = CncTextSecondary, fontSize = 8.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CncSurface)
                                        .border(0.5.dp, CncCardBorder, RoundedCornerShape(4.dp))
                                        .clickable {
                                            instrumentName = context.getString(R.string.calib_preset_micrometer)
                                            instrumentUncertaintyText = "0.002"
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(stringResource(R.string.calib_preset_micrometer), color = CncTextSecondary, fontSize = 8.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CncSurface)
                                        .border(0.5.dp, CncCardBorder, RoundedCornerShape(4.dp))
                                        .clickable {
                                            instrumentName = context.getString(R.string.calib_preset_laser)
                                            instrumentUncertaintyText = "0.0005"
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(stringResource(R.string.calib_preset_laser), color = CncTextSecondary, fontSize = 8.sp)
                                }
                            }
                        }
                    }

                    // --- SECTION 2: STEP-BY-STEP OPERATOR PROCEDURE WIZARD ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CncSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CncCyberCyan.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.calib_section2_header),
                                color = CncActiveGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            // Step selector row
                            val pointsList = activeSession.points
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(pointsList) { pt ->
                                    val isCurrent = pt.stepIndex == currentStepIndex
                                    val isDone = pt.measuredPositionMm != null
                                    val err = pt.errorMm

                                    val bg = when {
                                        isCurrent -> CncCyberCyan
                                        isDone -> CncActiveGreen.copy(alpha = 0.2f)
                                        else -> CncSurface
                                    }

                                    val borderColor = when {
                                        isCurrent -> CncCyberCyan
                                        isDone -> CncActiveGreen
                                        else -> CncCardBorder
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bg)
                                            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                            .clickable {
                                                currentStepIndex = pt.stepIndex
                                                inputMeasuredText = pt.measuredPositionMm?.let { String.format(Locale.US, "%.4f", it) } ?: ""
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${pt.percentOfTravel.toInt()}%",
                                                color = if (isCurrent) Color.Black else CncTextPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${pt.nominalPositionMm.toInt()}mm",
                                                color = if (isCurrent) Color.Black else CncTextSecondary,
                                                fontSize = 8.sp
                                            )
                                            if (err != null) {
                                                val errFormatted = String.format(Locale.US, "%+.3f", err)
                                                Text(
                                                    text = errFormatted,
                                                    color = if (isCurrent) Color.Black else if (abs(
                                                            err
                                                        ) > 0.01) CncEstopRed else CncActiveGreen,
                                                    fontSize = 7.5.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Operator Instruction Box
                            val activePt = pointsList.getOrNull(currentStepIndex) ?: pointsList.firstOrNull()
                            if (activePt != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CncSurface),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(R.string.calib_point_info, activePt.stepIndex + 1, pointsList.size, activePt.percentOfTravel.toInt(), activePt.nominalPositionMm),
                                                color = CncCyberCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Button(
                                                onClick = { onMoveToNominal(activePt.stepIndex) },
                                                colors = ButtonDefaults.buttonColors(containerColor = CncSurfaceVariant),
                                                border = BorderStroke(1.dp, CncCyberCyan),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.GpsFixed,
                                                    contentDescription = stringResource(R.string.common_execute),
                                                    tint = CncCyberCyan,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(stringResource(R.string.calib_drive_axis_btn, activePt.nominalPositionMm.toString()), color = CncCyberCyan, fontSize = 8.5.sp)
                                            }
                                        }

                                        // Step instruction items
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(stringResource(R.string.calib_step1, activePt.nominalPositionMm.toString()), color = CncTextPrimary, fontSize = 9.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(stringResource(R.string.calib_step2), color = CncTextPrimary, fontSize = 9.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(stringResource(R.string.calib_step3), color = CncTextPrimary, fontSize = 9.sp)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = inputMeasuredText,
                                                onValueChange = { inputMeasuredText = it },
                                                label = { Text(stringResource(R.string.calib_measured_val_label), fontSize = 8.5.sp) },
                                                singleLine = true,
                                                placeholder = { Text(activePt.nominalPositionMm.toString(), fontSize = 9.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = CncSurfaceVariant,
                                                    unfocusedContainerColor = CncSurfaceVariant,
                                                    focusedBorderColor = CncActiveGreen,
                                                    unfocusedBorderColor = CncCardBorder,
                                                    focusedTextColor = CncDroDigits,
                                                    unfocusedTextColor = CncDroDigits
                                                ),
                                                modifier = Modifier
                                                    .weight(1.5f)
                                                    .height(52.dp)
                                            )

                                            Button(
                                                onClick = {
                                                    val measuredVal = inputMeasuredText.toDoubleOrNull() ?: activePt.nominalPositionMm
                                                    onRecordPoint(activePt.stepIndex, measuredVal)
                                                    if (currentStepIndex < (pointsList.size - 1)) {
                                                        currentStepIndex++
                                                        val nextPt = pointsList[currentStepIndex]
                                                        inputMeasuredText = nextPt.measuredPositionMm?.let { String.format(Locale.US, "%.4f", it) } ?: ""
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CncActiveGreen),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(52.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = stringResource(R.string.common_save),
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(stringResource(R.string.calib_save_next_btn), color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- SECTION 3: METROLOGICAL ANALYSIS & ERROR CURVE GRAPH ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CncSurfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.calib_section3_header),
                                    color = CncActiveGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Button(
                                    onClick = {
                                        generatedCompText = onGenerateCompTable(activeSession)
                                        showCompTableExportDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CncCyberCyan),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = stringResource(R.string.common_execute),
                                        tint = Color.Black,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.calib_export_btn), color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Metrology summary indicators
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricBox(
                                    label = stringResource(R.string.calib_max_error),
                                    value = String.format(Locale.US, "%+.4f mm", activeSession.maxErrorMm),
                                    color = if (activeSession.maxErrorMm > 0.02) CncEstopRed else CncActiveGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBox(
                                    label = stringResource(R.string.calib_uncertainty_band),
                                    value = String.format(Locale.US, "±%.4f mm", activeSession.expandedUncertaintyMm),
                                    color = CncCyberCyan,
                                    modifier = Modifier.weight(1.2f)
                                )
                                MetricBox(
                                    label = stringResource(R.string.calib_mean_bias),
                                    value = String.format(Locale.US, "%+.4f mm", activeSession.meanErrorMm),
                                    color = CncWarningAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Visual Error Curve Canvas
                            ErrorCurveVisualizer(
                                points = activeSession.points,
                                maxErrorMm = activeSession.maxErrorMm.coerceAtLeast(0.02),
                                expandedUncertaintyMm = activeSession.expandedUncertaintyMm,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CncBackground)
                                    .border(1.dp, CncCardBorder, RoundedCornerShape(6.dp))
                            )
                        }
                    }
                }
            }
        }
    }

    // Export Dialog for comp.tbl
    if (showCompTableExportDialog) {
        AlertDialog(
            onDismissRequest = { showCompTableExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = CncCyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.calib_export_title), color = CncCyberCyan, fontSize = 13.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.calib_export_text),
                        color = CncTextSecondary,
                        fontSize = 9.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(CncBackground)
                            .border(1.dp, CncCardBorder, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = generatedCompText,
                            color = CncActiveGreen,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("comp.tbl", generatedCompText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, context.getString(R.string.calib_toast_copied), Toast.LENGTH_SHORT).show()
                        showCompTableExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CncCyberCyan)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.calib_copy_clipboard), color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompTableExportDialog = false }) {
                    Text(stringResource(R.string.common_close), color = CncTextSecondary, fontSize = 9.sp)
                }
            },
            containerColor = CncSurface
        )
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CncSurface)
            .border(1.dp, CncCardBorder, RoundedCornerShape(6.dp))
            .padding(8.dp),
    ) {
        Column {
            Text(label, color = CncTextSecondary, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun ErrorCurveVisualizer(
    points: List<AxisCalibrationPoint>,
    maxErrorMm: Double,
    expandedUncertaintyMm: Double,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 24f
        val graphW = width - (padding * 2)
        val graphH = height - (padding * 2)
        val centerY = padding + (graphH / 2)

        // Draw center zero error line
        drawLine(
            color = Color(0xFF37474F),
            start = Offset(padding, centerY),
            end = Offset(width - padding, centerY),
            strokeWidth = 1.5f
        )

        // Uncertainty band bounds (±U)
        val scaleY = (graphH / 2) / (maxErrorMm * 1.5f).coerceAtLeast(0.01).toFloat()
        val uOffset = (expandedUncertaintyMm.toFloat() * scaleY).coerceAtMost(graphH / 2)

        // Draw Uncertainty Band (Upper & Lower bounds)
        drawLine(
            color = CncCyberCyan.copy(alpha = 0.35f),
            start = Offset(padding, centerY - uOffset),
            end = Offset(width - padding, centerY - uOffset),
            strokeWidth = 1f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
        drawLine(
            color = CncCyberCyan.copy(alpha = 0.35f),
            start = Offset(padding, centerY + uOffset),
            end = Offset(width - padding, centerY + uOffset),
            strokeWidth = 1f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )

        val measuredPts = points.filter { it.errorMm != null }
        if (measuredPts.size >= 2) {
            val path = Path()
            measuredPts.forEachIndexed { i, pt ->
                val x = padding + ((pt.percentOfTravel.toFloat() / 100f) * graphW)
                val y = centerY - ((pt.errorMm?.toFloat() ?: 0f) * scaleY)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = CncActiveGreen,
                style = Stroke(width = 2.5f)
            )
        }

        // Draw points
        points.forEach { pt ->
            val x = padding + ((pt.percentOfTravel.toFloat() / 100f) * graphW)
            val err = pt.errorMm
            if (err != null) {
                val y = centerY - (err.toFloat() * scaleY)
                drawCircle(
                    color = if (abs(err) > 0.015) CncEstopRed else CncActiveGreen,
                    radius = 4f,
                    center = Offset(x, y)
                )
            } else {
                drawCircle(
                    color = Color(0xFF546E7A),
                    radius = 2.5f,
                    center = Offset(x, centerY)
                )
            }
        }
    }
}
