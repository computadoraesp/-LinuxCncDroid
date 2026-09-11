package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CncNavigationTab
import com.example.model.LogSeverity
import com.example.ui.components.AlarmEventLogView
import com.example.ui.components.AppManualDialog
import com.example.ui.components.AxisCalibrationDialog
import com.example.ui.components.DroPanel
import com.example.ui.components.EtherCatTelemetryView
import com.example.ui.components.GCodeSecurityLoaderDialog
import com.example.ui.components.IndustrialCameraView
import com.example.ui.components.IndustrialTopBar
import com.example.ui.components.JogControlPad
import com.example.ui.components.MachineConfigView
import com.example.ui.components.MdiView
import com.example.ui.components.MiniDroBar
import com.example.ui.components.ProbingView
import com.example.ui.components.SpeedsFeedsCalculatorDialog
import com.example.ui.components.SpindleFeedPanel
import com.example.ui.components.ToolTableDialog
import com.example.ui.components.ToolpathVisualizer3D
import com.example.ui.theme.CncBackground
import com.example.ui.theme.CncCardBorder
import com.example.ui.theme.CncCyberCyan
import com.example.ui.theme.CncEstopRed
import com.example.ui.theme.CncSurface
import com.example.ui.theme.CncSurfaceVariant
import com.example.ui.theme.CncTextPrimary
import com.example.ui.theme.CncTextSecondary
import com.example.viewmodel.CncViewModel

fun CncNavigationTab.getTitle(): String = when (this) {
    CncNavigationTab.CONTROL -> "CONTROL"
    CncNavigationTab.TOOLPATH -> "TOOLPATH"
    CncNavigationTab.CAMERA -> "CÁMARA"
    CncNavigationTab.PROBING -> "PROBING"
    CncNavigationTab.ETHERCAT -> "ETHERCAT"
    CncNavigationTab.MDI -> "MDI"
    CncNavigationTab.LOGS -> "LOGS"
    CncNavigationTab.CONFIG -> "CONFIG"
}

@Composable
fun CncNavigationTab.getIcon(): ImageVector = when (this) {
    CncNavigationTab.CONTROL -> Icons.Default.Tune
    CncNavigationTab.TOOLPATH -> Icons.Default.ViewInAr
    CncNavigationTab.CAMERA -> Icons.Default.Videocam
    CncNavigationTab.PROBING -> Icons.Default.GpsFixed
    CncNavigationTab.ETHERCAT -> Icons.Default.Hub
    CncNavigationTab.MDI -> Icons.Default.Terminal
    CncNavigationTab.LOGS -> Icons.Default.Notifications
    CncNavigationTab.CONFIG -> Icons.Default.Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CncMainScreen(
    modifier: Modifier = Modifier,
    viewModel: CncViewModel,
) {
    val machineState by viewModel.machineState.collectAsStateWithLifecycle()
    val taskMode by viewModel.taskMode.collectAsStateWithLifecycle()
    val currentCoordSystem by viewModel.currentCoordSystem.collectAsStateWithLifecycle()
    val axes by viewModel.axes.collectAsStateWithLifecycle()
    val spindle by viewModel.spindle.collectAsStateWithLifecycle()
    val feed by viewModel.feed.collectAsStateWithLifecycle()
    val coolant by viewModel.coolant.collectAsStateWithLifecycle()
    val probe by viewModel.probe.collectAsStateWithLifecycle()
    val tool by viewModel.tool.collectAsStateWithLifecycle()
    val etherCatMaster by viewModel.etherCatMaster.collectAsStateWithLifecycle()
    val etherCatSlaves by viewModel.etherCatSlaves.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilities.collectAsStateWithLifecycle()
    val loadedGCode by viewModel.loadedGCode.collectAsStateWithLifecycle()
    val loadedFileName by viewModel.loadedFileName.collectAsStateWithLifecycle()
    val activeGCodeLine by viewModel.activeGCodeLine.collectAsStateWithLifecycle()
    val isSimulated by viewModel.isSimulatedMode.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val isContinuousJog by viewModel.isContinuousJog.collectAsStateWithLifecycle()
    val jogStep by viewModel.jogStep.collectAsStateWithLifecycle()
    val jogSpeed by viewModel.jogSpeedMmMin.collectAsStateWithLifecycle()
    val unitSystem by viewModel.unitSystem.collectAsStateWithLifecycle()
    val mdiText by viewModel.mdiCommandText.collectAsStateWithLifecycle()
    val mdiHistory by viewModel.mdiHistory.collectAsStateWithLifecycle()
    val profiles by viewModel.machineProfiles.collectAsStateWithLifecycle()
    val macros by viewModel.macros.collectAsStateWithLifecycle()
    val eventLogs by viewModel.eventLogs.collectAsStateWithLifecycle()
    val networkLatencyMs by viewModel.networkLatencyMs.collectAsStateWithLifecycle()
    val toolTable by viewModel.toolTable.collectAsStateWithLifecycle()
    val activeTool by viewModel.activeTool.collectAsStateWithLifecycle()
    val cycleElapsedSeconds by viewModel.cycleElapsedSeconds.collectAsStateWithLifecycle()
    val cycleEstimatedTotalSeconds by viewModel.cycleEstimatedTotalSeconds.collectAsStateWithLifecycle()
    val jogStyle by viewModel.jogStyle.collectAsStateWithLifecycle()
    val mpgAxis by viewModel.mpgAxis.collectAsStateWithLifecycle()
    val mpgMultiplier by viewModel.mpgMultiplier.collectAsStateWithLifecycle()
    val activeCalibrationSession by viewModel.activeCalibrationSession.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val showCyberScanDialog by viewModel.showCyberScanDialog.collectAsStateWithLifecycle()
    val showCalculatorDialog by viewModel.showCalculatorDialog.collectAsStateWithLifecycle()
    val showToolTableDialog by viewModel.showToolTableDialog.collectAsStateWithLifecycle()
    val showCalibrationDialog by viewModel.showCalibrationDialog.collectAsStateWithLifecycle()
    val showManualDialog by viewModel.showManualDialog.collectAsStateWithLifecycle()

    val errorCount = remember(eventLogs) {
        eventLogs.count { (it.severity == LogSeverity.ERROR) || (it.severity == LogSeverity.CRITICAL) }
    }

    Scaffold(
        topBar = {
            IndustrialTopBar(
                machineState = machineState,
                currentCoordSystem = currentCoordSystem,
                architecture = capabilities.architecture,
                userRole = userRole,
                isSimulated = isSimulated,
                latencyMs = networkLatencyMs,
                errorCount = errorCount,
                unitSystem = unitSystem,
                onToggleUnitSystem = { viewModel.toggleUnitSystem() },
                onToggleEstop = { viewModel.toggleEstop() },
                onPowerOn = { viewModel.powerOn() },
                onPowerOff = { viewModel.powerOff() },
                onSelectCoordSystem = { viewModel.engine.setCoordinateSystem(it) },
                onSelectRole = { viewModel.setUserRole(it) },
                onOpenCyberScanner = { viewModel.setShowCyberScanDialog(show = true) },
                onOpenCalculator = { viewModel.setShowCalculatorDialog(show = true) },
                onOpenToolTable = { viewModel.setShowToolTableDialog(show = true) },
                onOpenLogs = { viewModel.setSelectedTab(CncNavigationTab.LOGS) },
                onOpenAxisCalibration = { viewModel.setShowCalibrationDialog(show = true) },
            ) { viewModel.setShowManualDialog(show = true) }
        },
        bottomBar = {
            Surface(
                color = CncSurface,
                tonalElevation = 8.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(CncNavigationTab.entries.toTypedArray()) { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CncCyberCyan.copy(alpha = 0.18f) else CncSurfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CncCyberCyan else CncCardBorder,
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setSelectedTab(tab)
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if ((tab == CncNavigationTab.LOGS) && (errorCount > 0)) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = CncEstopRed, contentColor = Color.White) {
                                                Text(errorCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = tab.getIcon(),
                                            contentDescription = tab.getTitle(),
                                            tint = if (isSelected) CncCyberCyan else CncTextSecondary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = tab.getIcon(),
                                        contentDescription = tab.getTitle(),
                                        tint = if (isSelected) CncCyberCyan else CncTextSecondary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }

                                Text(
                                    text = tab.getTitle(),
                                    color = if (isSelected) CncCyberCyan else CncTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = CncBackground,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CncBackground),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when (selectedTab) {
                    CncNavigationTab.CONTROL -> {
                        // Digital Readout (DRO) Panel
                        DroPanel(
                            axes = axes,
                            currentCoordSystem = currentCoordSystem,
                            hasServoTorque = capabilities.hasServoTorque,
                            unitSystem = unitSystem,
                            onZeroAxis = { viewModel.zeroAxis(it) },
                            onZeroAll = { viewModel.zeroAllAxes() },
                            onHomeAxis = { viewModel.homeAxis(it) },
                        ) { viewModel.homeAllAxes() }

                        // Jogging Controls (Button Pad + Virtual MPG Handwheel)
                        JogControlPad(
                            axes = capabilities.axes,
                            axesMap = axes,
                            jogStyle = jogStyle,
                            taskMode = taskMode,
                            mpgAxis = mpgAxis,
                            mpgMultiplier = mpgMultiplier,
                            isContinuous = isContinuousJog,
                            selectedStepMm = jogStep,
                            jogSpeedMmMin = jogSpeed,
                            unitSystem = unitSystem,
                            onSelectJogStyle = { viewModel.setJogStyle(it) },
                            onSelectMpgAxis = { viewModel.setMpgAxis(it) },
                            onSelectMpgMultiplier = { viewModel.setMpgMultiplier(it) },
                            onMpgStep = { axis, dir, mult -> viewModel.sendMpgStep(axis, dir, mult) },
                            onZeroAxis = { viewModel.zeroAxis(it) },
                            onToggleContinuous = { viewModel.setJogMode(it) },
                            onSelectStep = { viewModel.setJogStep(it) },
                            onSpeedChange = { viewModel.setJogSpeed(it) },
                            onStartJog = { axis, dir, _ -> viewModel.startJog(axis, dir) },
                            onStopJog = { viewModel.stopJog() },
                        ) { axis, dir, _ -> viewModel.stepJog(axis, dir) }

                        // Spindle, Feedrate Overrides & Cycle Controls
                        SpindleFeedPanel(
                            spindle = spindle,
                            feed = feed,
                            coolant = coolant,
                            machineState = machineState,
                            unitSystem = unitSystem,
                            onToggleSpindle = { viewModel.engine.toggleSpindle() },
                            onSetSpindleRpm = { viewModel.engine.setSpindleRpm(it) },
                            onSpindleOverride = { viewModel.engine.setSpindleOverride(it) },
                            onFeedOverride = { viewModel.engine.setFeedOverride(it) },
                            onToggleMist = { viewModel.engine.toggleMistCoolant() },
                            onToggleFlood = { viewModel.engine.toggleFloodCoolant() },
                            onCycleStart = { viewModel.cycleStart() },
                            onFeedHold = { viewModel.feedHold() },
                            onCycleStop = { viewModel.cycleStop() },
                        )
                    }

                    CncNavigationTab.TOOLPATH -> {
                        ToolpathVisualizer3D(
                            gcodeList = loadedGCode,
                            activeLineIndex = activeGCodeLine,
                            axes = axes,
                            fileName = loadedFileName,
                            elapsedSeconds = cycleElapsedSeconds,
                            estimatedTotalSeconds = cycleEstimatedTotalSeconds,
                            feedRate = feed.actualFeed,
                            spindleRpm = spindle.actualRpm,
                            activeToolDiameter = activeTool.diameter,
                        ) { viewModel.setShowCyberScanDialog(show = true) }

                        // Compact Spindle / Cycle Control Bar below Toolpath
                        SpindleFeedPanel(
                            spindle = spindle,
                            feed = feed,
                            coolant = coolant,
                            machineState = machineState,
                            unitSystem = unitSystem,
                            onToggleSpindle = { viewModel.engine.toggleSpindle() },
                            onSetSpindleRpm = { viewModel.engine.setSpindleRpm(it) },
                            onSpindleOverride = { viewModel.engine.setSpindleOverride(it) },
                            onFeedOverride = { viewModel.engine.setFeedOverride(it) },
                            onToggleMist = { viewModel.engine.toggleMistCoolant() },
                            onToggleFlood = { viewModel.engine.toggleFloodCoolant() },
                            onCycleStart = { viewModel.cycleStart() },
                            onFeedHold = { viewModel.feedHold() },
                            onCycleStop = { viewModel.cycleStop() },
                        )
                    }

                    CncNavigationTab.CAMERA -> {
                        IndustrialCameraView(
                            machineState = machineState,
                            axes = axes,
                            currentWcs = currentCoordSystem,
                            unitSystem = unitSystem,
                            onJogAxis = { axis, delta -> viewModel.stepJog(axis, if (delta > 0) 1 else -1) },
                        ) { viewModel.zeroAxis(it) }
                    }

                    CncNavigationTab.PROBING -> {
                        MiniDroBar(
                            axes = axes,
                            currentCoordSystem = currentCoordSystem,
                            unitSystem = unitSystem,
                        ) { viewModel.zeroAxis(it) }

                        ProbingView(
                            probeInfo = probe,
                            onExecuteRoutine = { viewModel.triggerProbe(it) },
                        )
                    }

                    CncNavigationTab.ETHERCAT -> {
                        EtherCatTelemetryView(
                            masterInfo = etherCatMaster,
                            slaves = etherCatSlaves,
                        )
                    }

                    CncNavigationTab.MDI -> {
                        MiniDroBar(
                            axes = axes,
                            currentCoordSystem = currentCoordSystem,
                            unitSystem = unitSystem,
                        ) { viewModel.zeroAxis(it) }

                        MdiView(
                            commandText = mdiText,
                            history = mdiHistory,
                            macros = macros,
                            onCommandTextChange = { viewModel.setMdiText(it) },
                            onExecuteCommand = { viewModel.executeMdiCommand(it) },
                        )
                    }

                    CncNavigationTab.LOGS -> {
                        AlarmEventLogView(
                            logs = eventLogs,
                            onClearLogs = { viewModel.clearLogs() },
                            onSimulateAlarm = { viewModel.simulateDiagnosticAlarm() },
                        )
                    }

                    CncNavigationTab.CONFIG -> {
                        MachineConfigView(
                            capabilities = capabilities,
                            profiles = profiles,
                            onSwitchArchitecture = { viewModel.engine.switchArchitecture(it) },
                            onConnectHost = { ip, port -> viewModel.engine.connectToHost(ip, port) },
                            onSaveProfile = { name, ip, port, arch -> viewModel.saveProfile(name, ip, port, arch) },
                            onDeleteProfile = { viewModel.deleteProfile(it) },
                            onWipeAllData = { viewModel.wipeAllAppData() },
                            onOpenMetrologyCalibration = { viewModel.setShowCalibrationDialog(show = true) },
                        ) { viewModel.setShowManualDialog(show = true) }
                    }
                }
            }

            // Dialogs
            if (showCyberScanDialog) {
                GCodeSecurityLoaderDialog(
                    onDismiss = { viewModel.setShowCyberScanDialog(show = false) },
                ) { fileName, content ->
                    viewModel.loadAndScanGCode(fileName, content)
                    viewModel.setShowCyberScanDialog(show = false)
                }
            }

            if (showCalculatorDialog) {
                SpeedsFeedsCalculatorDialog(
                    onDismiss = { viewModel.setShowCalculatorDialog(show = false) },
                ) { rpm, feedRate ->
                    viewModel.applySpeedsFeeds(rpm, feedRate)
                    viewModel.setShowCalculatorDialog(show = false)
                }
            }

            if (showToolTableDialog) {
                ToolTableDialog(
                    tools = toolTable,
                    activeTool = activeTool,
                    currentSpindleZ = axes["Z"]?.workPos ?: 0.0,
                    onDismiss = { viewModel.setShowToolTableDialog(show = false) },
                    onMountTool = { viewModel.mountTool(it) },
                    onUpdateTool = { viewModel.updateTool(it) },
                    onDeleteTool = { viewModel.deleteTool(it) },
                ) {
                    viewModel.touchOffToolZ(it)
                    viewModel.engine.logEvent(LogSeverity.INFO, "TOOL", "Touch-off for T$it: Tool # ${tool.toolNumber}")
                }
            }

            if (showCalibrationDialog) {
                AxisCalibrationDialog(
                    session = activeCalibrationSession,
                    onDismiss = { viewModel.setShowCalibrationDialog(show = false) },
                    onStartSession = { axis, travel, interval, instName, instUncertainty ->
                        viewModel.startAxisCalibration(axis, travel, interval, instName, instUncertainty)
                    },
                    onRecordPoint = { stepIdx, measuredVal ->
                        viewModel.recordCalibrationMeasurement(stepIdx, measuredVal)
                    },
                    onMoveToNominal = { stepIdx ->
                        viewModel.moveAxisToNominal(stepIdx)
                    },
                    onGenerateCompTable = { sess ->
                        viewModel.generateLinuxCncCompTable(sess)
                    },
                )
            }

            if (showManualDialog) {
                AppManualDialog(
                    onDismiss = { viewModel.setShowManualDialog(show = false) },
                )
            }
        }
    }
}
