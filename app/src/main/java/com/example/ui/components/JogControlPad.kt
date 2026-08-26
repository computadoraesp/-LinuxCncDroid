package com.example.ui.components

import android.view.MotionEvent
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AxisCoord
import com.example.model.JogControlStyle
import com.example.model.MpgMultiplier
import com.example.model.UnitSystem
import com.example.ui.theme.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun JogControlPad(
    axes: List<String>,
    axesMap: Map<String, AxisCoord> = emptyMap(),
    jogStyle: JogControlStyle = JogControlStyle.BUTTON_PAD,
    mpgAxis: String = "X",
    mpgMultiplier: MpgMultiplier = MpgMultiplier.X100,
    isContinuous: Boolean,
    selectedStepMm: Double,
    jogSpeedMmMin: Double,
    unitSystem: UnitSystem = UnitSystem.METRIC,
    onSelectJogStyle: (JogControlStyle) -> Unit = {},
    onSelectMpgAxis: (String) -> Unit = {},
    onSelectMpgMultiplier: (MpgMultiplier) -> Unit = {},
    onMpgStep: (String, Int, MpgMultiplier) -> Unit = { _, _, _ -> },
    onZeroAxis: (String) -> Unit = {},
    onToggleContinuous: (Boolean) -> Unit,
    onSelectStep: (Double) -> Unit,
    onSpeedChange: (Double) -> Unit,
    onStartJog: (String, Int, Double) -> Unit,
    onStopJog: () -> Unit,
    onStepJog: (String, Int, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    if (jogStyle == JogControlStyle.VIRTUAL_MPG) {
        Column(modifier = modifier) {
            // Style Selector Bar
            JogStyleHeader(
                currentStyle = jogStyle,
                onSelectStyle = onSelectJogStyle
            )
            Spacer(modifier = Modifier.height(6.dp))
            VirtualMpgWheel(
                axes = axesMap,
                selectedAxis = mpgAxis,
                selectedMultiplier = mpgMultiplier,
                unitSystem = unitSystem,
                onSelectAxis = onSelectMpgAxis,
                onSelectMultiplier = onSelectMpgMultiplier,
                onMpgStep = onMpgStep,
                onZeroSelectedAxis = onZeroAxis
            )
        }
        return
    }

    val stepResolutions = if (unitSystem == UnitSystem.IMPERIAL) {
        listOf(0.0001, 0.001, 0.010, 0.100, 1.000)
    } else {
        listOf(0.001, 0.010, 0.100, 1.000, 10.000)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CncCardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CncCardBorder)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header & Style Switch + Continuous vs Step
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = "Jogging",
                        tint = CncWarningAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MANUAL JOG CONTROL",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = CncTextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Style Selector Pill (PAD vs MPG)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CncSurfaceVariant)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (jogStyle == JogControlStyle.BUTTON_PAD) CncCyberCyan else Color.Transparent)
                                .clickable { onSelectJogStyle(JogControlStyle.BUTTON_PAD) }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "PAD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (jogStyle == JogControlStyle.BUTTON_PAD) Color(0xFF00363D) else CncTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (jogStyle == JogControlStyle.VIRTUAL_MPG) CncCyberCyan else Color.Transparent)
                                .clickable { onSelectJogStyle(JogControlStyle.VIRTUAL_MPG) }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "MPG WHEEL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (jogStyle == JogControlStyle.VIRTUAL_MPG) Color(0xFF00363D) else CncTextSecondary
                            )
                        }
                    }

                    // Mode Toggle (Continuous vs Incremental)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CncSurfaceVariant)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isContinuous) CncWarningAmber else Color.Transparent)
                                .clickable { onToggleContinuous(true) }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "CONT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isContinuous) Color.Black else CncTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (!isContinuous) CncCyberCyan else Color.Transparent)
                                .clickable { onToggleContinuous(false) }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "STEP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isContinuous) Color.Black else CncTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step Size Selector Pills (if in Step Mode)
            if (!isContinuous) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    stepResolutions.forEach { step ->
                        val isSelected = selectedStepMm == step
                        val label = if (unitSystem == UnitSystem.IMPERIAL) {
                            if (step < 0.01) "${step}in" else "${step}in"
                        } else {
                            if (step >= 1.0) "${step.toInt()}mm" else "${step}mm"
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CncCyberCyan else CncSurfaceVariant)
                                .border(1.dp, if (isSelected) CncCyberCyan else CncCardBorder, RoundedCornerShape(6.dp))
                                .clickable { onSelectStep(step) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) Color(0xFF00363D) else CncTextPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                // Feed speed slider (if in Continuous Mode)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jog Feedrate:", fontSize = 10.sp, color = CncTextSecondary)
                        Text(
                            text = unitSystem.formatSpeed(jogSpeedMmMin),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CncWarningAmber
                        )
                    }
                    Slider(
                        value = jogSpeedMmMin.toFloat(),
                        onValueChange = { onSpeedChange(it.toDouble()) },
                        valueRange = 100f..5000f,
                        colors = SliderDefaults.colors(
                            thumbColor = CncWarningAmber,
                            activeTrackColor = CncWarningAmber,
                            inactiveTrackColor = CncCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Directional Jogging Pads (XY Plane + Z-Axis + 4th Axis A)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // XY Planar Cross Pad
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(CncSurfaceBg, RoundedCornerShape(12.dp))
                        .border(1.dp, CncCardBorder, RoundedCornerShape(12.dp))
                ) {
                    // Center XY Label
                    Text(
                        text = "XY",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = CncTextMuted,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Y+ Button (Top)
                    JogTouchButton(
                        label = "Y+",
                        color = AxisYColor,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp),
                        onDown = {
                            if (isContinuous) onStartJog("Y", 1, jogSpeedMmMin)
                            else onStepJog("Y", 1, selectedStepMm)
                        },
                        onUp = { if (isContinuous) onStopJog() }
                    )

                    // Y- Button (Bottom)
                    JogTouchButton(
                        label = "Y-",
                        color = AxisYColor,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
                        onDown = {
                            if (isContinuous) onStartJog("Y", -1, jogSpeedMmMin)
                            else onStepJog("Y", -1, selectedStepMm)
                        },
                        onUp = { if (isContinuous) onStopJog() }
                    )

                    // X- Button (Left)
                    JogTouchButton(
                        label = "X-",
                        color = AxisXColor,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp),
                        onDown = {
                            if (isContinuous) onStartJog("X", -1, jogSpeedMmMin)
                            else onStepJog("X", -1, selectedStepMm)
                        },
                        onUp = { if (isContinuous) onStopJog() }
                    )

                    // X+ Button (Right)
                    JogTouchButton(
                        label = "X+",
                        color = AxisXColor,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp),
                        onDown = {
                            if (isContinuous) onStartJog("X", 1, jogSpeedMmMin)
                            else onStepJog("X", 1, selectedStepMm)
                        },
                        onUp = { if (isContinuous) onStopJog() }
                    )
                }

                // Z-Axis Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Z-AXIS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = AxisZColor)

                    JogTouchButton(
                        label = "Z+",
                        color = AxisZColor,
                        size = 48.dp,
                        onDown = {
                            if (isContinuous) onStartJog("Z", 1, jogSpeedMmMin)
                            else onStepJog("Z", 1, selectedStepMm)
                        },
                        onUp = { if (isContinuous) onStopJog() }
                    )

                    JogTouchButton(
                        label = "Z-",
                        color = AxisZColor,
                        size = 48.dp,
                        onDown = {
                            if (isContinuous) onStartJog("Z", -1, jogSpeedMmMin)
                            else onStepJog("Z", -1, selectedStepMm)
                        },
                        onUp = { if (isContinuous) onStopJog() }
                    )
                }

                // 4th Axis A-Axis (if available in configuration)
                if (axes.contains("A")) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("A-AXIS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = AxisAColor)

                        JogTouchButton(
                            label = "A+",
                            color = AxisAColor,
                            size = 48.dp,
                            onDown = {
                                if (isContinuous) onStartJog("A", 1, jogSpeedMmMin)
                                else onStepJog("A", 1, selectedStepMm)
                            },
                            onUp = { if (isContinuous) onStopJog() }
                        )

                        JogTouchButton(
                            label = "A-",
                            color = AxisAColor,
                            size = 48.dp,
                            onDown = {
                                if (isContinuous) onStartJog("A", -1, jogSpeedMmMin)
                                else onStepJog("A", -1, selectedStepMm)
                            },
                            onUp = { if (isContinuous) onStopJog() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JogStyleHeader(
    currentStyle: JogControlStyle,
    onSelectStyle: (JogControlStyle) -> Unit
) {
    Surface(
        color = CncSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "JOG MODE: VIRTUAL MPG",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = CncCyberCyan,
                modifier = Modifier.padding(start = 8.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (currentStyle == JogControlStyle.BUTTON_PAD) CncCyberCyan else Color.Transparent)
                        .clickable { onSelectStyle(JogControlStyle.BUTTON_PAD) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PAD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStyle == JogControlStyle.BUTTON_PAD) Color(0xFF00363D) else CncTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (currentStyle == JogControlStyle.VIRTUAL_MPG) CncCyberCyan else Color.Transparent)
                        .clickable { onSelectStyle(JogControlStyle.VIRTUAL_MPG) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "MPG WHEEL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStyle == JogControlStyle.VIRTUAL_MPG) Color(0xFF00363D) else CncTextSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun JogTouchButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    onDown: () -> Unit,
    onUp: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) color else CncSurfaceVariant)
            .border(1.5.dp, color, RoundedCornerShape(8.dp))
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onDown()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onUp()
                        true
                    }
                    else -> false
                }
            }
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = if (isPressed) Color.Black else color
        )
    }
}
