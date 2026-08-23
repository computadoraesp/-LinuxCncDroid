package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.CncViewModel

enum class CncNavigationTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CONTROL("CONTROL", Icons.Default.Tune),
    TOOLPATH("TOOLPATH", Icons.Default.ViewInAr),
    CAMERA("CÁMARA", Icons.Default.Videocam),
    PROBING("PROBING", Icons.Default.GpsFixed),
    ETHERCAT("ETHERCAT", Icons.Default.Hub),
    MDI("MDI", Icons.Default.Terminal),
    LOGS("LOGS", Icons.Default.Notifications),
    CONFIG("CONFIG", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CncMainScreen(
    viewModel: CncViewModel,
    modifier: Modifier = Modifier
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

    var selectedTab by remember { mutableStateOf(CncNavigationTab.CONTROL) }
    var showCyberScanDialog by remember { mutableStateOf(false) }
    var showCalculatorDialog by remember { mutableStateOf(false) }
    var showToolTableDialog by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }

    val errorCount = remember(eventLogs) {
        eventLogs.count { it.severity == LogSeverity.ERROR || it.severity == LogSeverity.CRITICAL }
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
                onToggleEstop = { viewModel.toggleEstop() },
                onPowerOn = { viewModel.powerOn() },
                onPowerOff = { viewModel.powerOff() },
                onSelectCoordSystem = { viewModel.engine.setCoordinateSystem(it) },
                onSelectRole = { viewModel.setUserRole(it) },
                onOpenConfig = { selectedTab = CncNavigationTab.CONFIG },
                onOpenCyberScanner = { showCyberScanDialog = true },
                onOpenCalculator = { showCalculatorDialog = true },
                onOpenToolTable = { showToolTableDialog = true },
                onOpenLogs = { selectedTab = CncNavigationTab.LOGS },
                onOpenAxisCalibration = { showCalibrationDialog = true },
                onOpenManual = { showManualDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CncSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                CncNavigationTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            viewModel.feedbackManager.triggerActionClick()
                            selectedTab = tab
                        },
                        icon = {
                            if (tab == CncNavigationTab.LOGS && errorCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = CncEstopRed, contentColor = Color.White) {
                                            Text("$errorCount", fontSize = 8.sp)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 8.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = CncCyberCyan,
                            indicatorColor = CncCyberCyan,
                            unselectedIconColor = CncTextMuted,
                            unselectedTextColor = CncTextSecondary
                        )
                    )
                }
            }
        },
        containerColor = CncBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CncBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedTab) {
                    CncNavigationTab.CONTROL -> {
                        // Digital Readout (DRO) Panel
                        DroPanel(
                            axes = axes,
                            currentCoordSystem = currentCoordSystem,
                            hasServoTorque = capabilities.hasServoTorque,
                            onZeroAxis = { viewModel.zeroAxis(it) },
                            onZeroAll = { viewModel.zeroAllAxes() },
                            onHomeAxis = { viewModel.homeAxis(it) },
                            onHomeAll = { viewModel.homeAllAxes() }
                        )

                        // Jogging Controls (Button Pad + Virtual MPG Handwheel)
                        JogControlPad(
                            axes = capabilities.axes,
                            axesMap = axes,
                            jogStyle = jogStyle,
                            mpgAxis = mpgAxis,
                            mpgMultiplier = mpgMultiplier,
                            isContinuous = isContinuousJog,
                            selectedStepMm = jogStep,
                            jogSpeedMmMin = jogSpeed,
                            onSelectJogStyle = { viewModel.setJogStyle(it) },
                            onSelectMpgAxis = { viewModel.setMpgAxis(it) },
                            onSelectMpgMultiplier = { viewModel.setMpgMultiplier(it) },
                            onMpgStep = { axis, dir, mult -> viewModel.sendMpgStep(axis, dir, mult) },
                            onZeroAxis = { viewModel.zeroAxis(it) },
                            onToggleContinuous = { viewModel.setJogMode(it) },
                            onSelectStep = { viewModel.setJogStep(it) },
                            onSpeedChange = { viewModel.setJogSpeed(it) },
                            onStartJog = { axis, dir, speed -> viewModel.startJog(axis, dir) },
                            onStopJog = { viewModel.stopJog() },
                            onStepJog = { axis, dir, step -> viewModel.stepJog(axis, dir) }
                        )

                        // Spindle, Feedrate Overrides & Cycle Controls
                        SpindleFeedPanel(
                            spindle = spindle,
                            feed = feed,
                            coolant = coolant,
                            machineState = machineState,
                            onToggleSpindle = { viewModel.engine.toggleSpindle() },
                            onSetSpindleRpm = { viewModel.engine.setSpindleRpm(it) },
                            onSpindleOverride = { viewModel.engine.setSpindleOverride(it) },
                            onFeedOverride = { viewModel.engine.setFeedOverride(it) },
                            onToggleMist = { viewModel.engine.toggleMistCoolant() },
                            onToggleFlood = { viewModel.engine.toggleFloodCoolant() },
                            onCycleStart = { viewModel.cycleStart() },
                            onFeedHold = { viewModel.feedHold() },
                            onCycleStop = { viewModel.cycleStop() }
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
                            onOpenLoader = { showCyberScanDialog = true }
                        )

                        // Compact Spindle / Cycle Control Bar below Toolpath
                        SpindleFeedPanel(
                            spindle = spindle,
                            feed = feed,
                            coolant = coolant,
                            machineState = machineState,
                            onToggleSpindle = { viewModel.engine.toggleSpindle() },
                            onSetSpindleRpm = { viewModel.engine.setSpindleRpm(it) },
                            onSpindleOverride = { viewModel.engine.setSpindleOverride(it) },
                            onFeedOverride = { viewModel.engine.setFeedOverride(it) },
                            onToggleMist = { viewModel.engine.toggleMistCoolant() },
                            onToggleFlood = { viewModel.engine.toggleFloodCoolant() },
                            onCycleStart = { viewModel.cycleStart() },
                            onFeedHold = { viewModel.feedHold() },
                            onCycleStop = { viewModel.cycleStop() }
                        )
                    }

                    CncNavigationTab.CAMERA -> {
                        IndustrialCameraView(
                            machineState = machineState,
                            axes = axes,
                            currentWcs = currentCoordSystem,
                            onJogAxis = { axis, delta -> viewModel.stepJog(axis, if (delta > 0) 1 else -1) },
                            onZeroAxis = { viewModel.zeroAxis(it) }
                        )
                    }

                    CncNavigationTab.PROBING -> {
                        MiniDroBar(
                            axes = axes,
                            currentCoordSystem = currentCoordSystem,
                            onZeroAxis = { viewModel.zeroAxis(it) }
                        )

                        ProbingView(
                            probeInfo = probe,
                            onExecuteRoutine = { viewModel.triggerProbe(it) }
                        )
                    }

                    CncNavigationTab.ETHERCAT -> {
                        EtherCatTelemetryView(
                            masterInfo = etherCatMaster,
                            slaves = etherCatSlaves
                        )
                    }

                    CncNavigationTab.MDI -> {
                        MiniDroBar(
                            axes = axes,
                            currentCoordSystem = currentCoordSystem,
                            onZeroAxis = { viewModel.zeroAxis(it) }
                        )

                        MdiView(
                            commandText = mdiText,
                            history = mdiHistory,
                            macros = macros,
                            onCommandTextChange = { viewModel.setMdiText(it) },
                            onExecuteCommand = { viewModel.executeMdiCommand(it) }
                        )
                    }

                    CncNavigationTab.LOGS -> {
                        AlarmEventLogView(
                            logs = eventLogs,
                            onClearLogs = { viewModel.clearLogs() },
                            onSimulateAlarm = { viewModel.simulateDiagnosticAlarm() }
                        )
                    }

                    CncNavigationTab.CONFIG -> {
                        MachineConfigView(
                            capabilities = capabilities,
                            profiles = profiles,
                            onSwitchArchitecture = { viewModel.engine.switchArchitecture(it) },
                            onConnectHost = { ip, port -> viewModel.engine.connectToHost(ip, port) },
                            onSaveProfile = { name, ip, port, arch -> viewModel.saveProfile(name, ip, port, arch) },
                            onOpenMetrologyCalibration = { showCalibrationDialog = true },
                            onOpenManual = { showManualDialog = true }
                        )
                    }
                }
            }

            // Dialogs
            if (showCyberScanDialog) {
                GCodeSecurityLoaderDialog(
                    onDismiss = { showCyberScanDialog = false },
                    onLoadValidatedGCode = { fileName, content ->
                        viewModel.loadAndScanGCode(fileName, content)
                    }
                )
            }

            if (showCalculatorDialog) {
                SpeedsFeedsCalculatorDialog(
                    onDismiss = { showCalculatorDialog = false },
                    onApplyToCnc = { rpm, feedRate ->
                        viewModel.applySpeedsFeeds(rpm, feedRate)
                    }
                )
            }

            if (showToolTableDialog) {
                ToolTableDialog(
                    tools = toolTable,
                    activeTool = activeTool,
                    currentSpindleZ = axes["Z"]?.workPos ?: 0.0,
                    onDismiss = { showToolTableDialog = false },
                    onMountTool = { viewModel.mountTool(it) },
                    onUpdateTool = { viewModel.updateTool(it) },
                    onDeleteTool = { viewModel.deleteTool(it) },
                    onTouchOffZ = { viewModel.touchOffToolZ(it) }
                )
            }

            if (showCalibrationDialog) {
                AxisCalibrationDialog(
                    session = activeCalibrationSession,
                    onDismiss = { showCalibrationDialog = false },
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
                    }
                )
            }

            if (showManualDialog) {
                AppManualDialog(
                    onDismiss = { showManualDialog = false }
                )
            }
        }
    }
}
