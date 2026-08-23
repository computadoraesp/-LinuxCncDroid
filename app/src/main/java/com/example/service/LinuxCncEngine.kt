package com.example.service

import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.*

class LinuxCncEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Machine State
    private val _machineState = MutableStateFlow(MachineStateEnum.IDLE)
    val machineState: StateFlow<MachineStateEnum> = _machineState.asStateFlow()

    private val _taskMode = MutableStateFlow(TaskMode.MANUAL)
    val taskMode: StateFlow<TaskMode> = _taskMode.asStateFlow()

    private val _currentCoordSystem = MutableStateFlow("G54")
    val currentCoordSystem: StateFlow<String> = _currentCoordSystem.asStateFlow()

    // Axes
    private val _axes = MutableStateFlow<Map<String, AxisCoord>>(
        mapOf(
            "X" to AxisCoord(name = "X", machinePos = 120.450, workPos = 20.450, dtgPos = 0.0, loadTorquePct = 14.5, motorTempC = 36.2, driveTempC = 41.0),
            "Y" to AxisCoord(name = "Y", machinePos = 85.320, workPos = 15.320, dtgPos = 0.0, loadTorquePct = 12.8, motorTempC = 35.8, driveTempC = 39.5),
            "Z" to AxisCoord(name = "Z", machinePos = -42.100, workPos = -12.100, dtgPos = 0.0, loadTorquePct = 18.2, motorTempC = 38.0, driveTempC = 42.1),
            "A" to AxisCoord(name = "A", machinePos = 0.000, workPos = 0.000, dtgPos = 0.0, loadTorquePct = 8.5, motorTempC = 32.5, driveTempC = 35.0)
        )
    )
    val axes: StateFlow<Map<String, AxisCoord>> = _axes.asStateFlow()

    // Spindle & Feed
    private val _spindle = MutableStateFlow(SpindleInfo())
    val spindle: StateFlow<SpindleInfo> = _spindle.asStateFlow()

    private val _feed = MutableStateFlow(FeedInfo())
    val feed: StateFlow<FeedInfo> = _feed.asStateFlow()

    private val _coolant = MutableStateFlow(CoolantInfo())
    val coolant: StateFlow<CoolantInfo> = _coolant.asStateFlow()

    private val _probe = MutableStateFlow(ProbeInfo())
    val probe: StateFlow<ProbeInfo> = _probe.asStateFlow()

    private val defaultTools = listOf(
        CncToolItem(
            id = 1,
            pocket = 1,
            description = "6mm 3-Flute Carbide Endmill",
            diameter = 6.000,
            lengthOffset = 45.230,
            toolType = ToolType.ENDMILL,
            flutes = 3,
            maxRpm = 24000.0,
            lifeMinutesCurrent = 42.0,
            lifeMinutesMax = 180.0,
            isActive = true
        ),
        CncToolItem(
            id = 2,
            pocket = 2,
            description = "3mm 2-Flute Ball Nose 3D",
            diameter = 3.000,
            lengthOffset = 38.110,
            toolType = ToolType.BALLNOSE,
            flutes = 2,
            maxRpm = 24000.0,
            lifeMinutesCurrent = 18.5,
            lifeMinutesMax = 120.0,
            isActive = false
        ),
        CncToolItem(
            id = 3,
            pocket = 3,
            description = "50mm 4-Insert Face Mill",
            diameter = 50.000,
            lengthOffset = 62.450,
            toolType = ToolType.FACE_MILL,
            flutes = 4,
            maxRpm = 10000.0,
            lifeMinutesCurrent = 65.0,
            lifeMinutesMax = 240.0,
            isActive = false
        ),
        CncToolItem(
            id = 4,
            pocket = 4,
            description = "4.2mm HSS Twist Drill (M5 Prep)",
            diameter = 4.200,
            lengthOffset = 52.800,
            toolType = ToolType.DRILL,
            flutes = 2,
            maxRpm = 6000.0,
            lifeMinutesCurrent = 12.0,
            lifeMinutesMax = 90.0,
            isActive = false
        ),
        CncToolItem(
            id = 5,
            pocket = 5,
            description = "M5x0.8 Spiral Point Machine Tap",
            diameter = 5.000,
            lengthOffset = 48.300,
            toolType = ToolType.TAP,
            flutes = 3,
            maxRpm = 1200.0,
            lifeMinutesCurrent = 8.0,
            lifeMinutesMax = 60.0,
            isActive = false
        ),
        CncToolItem(
            id = 6,
            pocket = 6,
            description = "10mm 45° Chamfer Deburr Mill",
            diameter = 10.000,
            lengthOffset = 41.500,
            toolType = ToolType.CHAMFER,
            flutes = 4,
            maxRpm = 18000.0,
            lifeMinutesCurrent = 29.0,
            lifeMinutesMax = 150.0,
            isActive = false
        ),
        CncToolItem(
            id = 7,
            pocket = 7,
            description = "Renishaw 3D Touch Probe",
            diameter = 4.000,
            lengthOffset = 75.000,
            toolType = ToolType.TOUCH_PROBE,
            flutes = 1,
            maxRpm = 0.0,
            lifeMinutesCurrent = 150.0,
            lifeMinutesMax = 9999.0,
            isActive = false
        )
    )

    private val _toolTable = MutableStateFlow<List<CncToolItem>>(defaultTools)
    val toolTable: StateFlow<List<CncToolItem>> = _toolTable.asStateFlow()

    private val _activeTool = MutableStateFlow(defaultTools.first())
    val activeTool: StateFlow<CncToolItem> = _activeTool.asStateFlow()

    private val _tool = MutableStateFlow(
        ToolInfo(
            toolNumber = 1,
            description = "6mm 3-Flute Carbide Endmill",
            lengthOffset = 45.230,
            diameterOffset = 6.000,
            atcSlot = 1
        )
    )
    val tool: StateFlow<ToolInfo> = _tool.asStateFlow()

    // Program execution timing
    private val _cycleElapsedSeconds = MutableStateFlow(0L)
    val cycleElapsedSeconds: StateFlow<Long> = _cycleElapsedSeconds.asStateFlow()

    private val _cycleEstimatedTotalSeconds = MutableStateFlow(185L)
    val cycleEstimatedTotalSeconds: StateFlow<Long> = _cycleEstimatedTotalSeconds.asStateFlow()

    // EtherCAT & Hardware Diagnostics
    private val _etherCatMaster = MutableStateFlow(EtherCatMasterInfo())
    val etherCatMaster: StateFlow<EtherCatMasterInfo> = _etherCatMaster.asStateFlow()

    private val _etherCatSlaves = MutableStateFlow<List<EtherCatSlaveInfo>>(
        listOf(
            EtherCatSlaveInfo(0, "Delta ASDA-B3-E Axis X", actualTorquePct = 14.5, driveTempC = 41.0),
            EtherCatSlaveInfo(1, "Delta ASDA-B3-E Axis Y", actualTorquePct = 12.8, driveTempC = 39.5),
            EtherCatSlaveInfo(2, "Delta ASDA-B3-E Axis Z (Brake)", actualTorquePct = 18.2, driveTempC = 42.1),
            EtherCatSlaveInfo(3, "Delta ASDA-B3-E Axis A", actualTorquePct = 8.5, driveTempC = 35.0)
        )
    )
    val etherCatSlaves: StateFlow<List<EtherCatSlaveInfo>> = _etherCatSlaves.asStateFlow()

    // Capabilities
    private val _capabilities = MutableStateFlow(CapabilitiesManifest())
    val capabilities: StateFlow<CapabilitiesManifest> = _capabilities.asStateFlow()

    // GCode & Program execution
    private val _activeGCodeLine = MutableStateFlow(0)
    val activeGCodeLine: StateFlow<Int> = _activeGCodeLine.asStateFlow()

    private val _loadedGCode = MutableStateFlow<List<GCodeSegment>>(emptyList())
    val loadedGCode: StateFlow<List<GCodeSegment>> = _loadedGCode.asStateFlow()

    private val _loadedFileName = MutableStateFlow("pocket_demo.ngc")
    val loadedFileName: StateFlow<String> = _loadedFileName.asStateFlow()

    private val _eventLogs = MutableStateFlow<List<CncEventLog>>(
        listOf(
            CncEventLog(severity = LogSeverity.INFO, tag = "KERNEL", message = "LinuxCNC Motion Kernel initialized in real-time mode"),
            CncEventLog(severity = LogSeverity.INFO, tag = "ETHERCAT", message = "EtherCAT Master [OP]: 4 slaves operational, DC synch 1000us"),
            CncEventLog(severity = LogSeverity.SECURITY, tag = "SECURITY", message = "Cybersecurity monitor active: Trojan & macro inspection enabled"),
            CncEventLog(severity = LogSeverity.INFO, tag = "SYSTEM", message = "Machine in IDLE state. Ready for operation")
        )
    )
    val eventLogs: StateFlow<List<CncEventLog>> = _eventLogs.asStateFlow()

    private val _isSimulatedMode = MutableStateFlow(true)
    val isSimulatedMode: StateFlow<Boolean> = _isSimulatedMode.asStateFlow()

    private val _networkLatencyMs = MutableStateFlow(2)
    val networkLatencyMs: StateFlow<Int> = _networkLatencyMs.asStateFlow()

    private val _activeJogAxis = MutableStateFlow<String?>(null)
    private var jogDirection = 0
    private var jogSpeed = 1000.0

    // OkHttp WebSocket client for real hardware
    private var okHttpClient: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var isConnectedToRealServer = false

    init {
        loadSampleGCode()
        startKinematicsLoop()
    }

    fun logEvent(severity: LogSeverity, tag: String, message: String) {
        val updated = _eventLogs.value.toMutableList()
        updated.add(0, CncEventLog(severity = severity, tag = tag, message = message))
        _eventLogs.value = updated.take(100)
    }

    fun clearEventLogs() {
        _eventLogs.value = emptyList()
    }

    fun loadGCodeContent(fileName: String, content: String) {
        _loadedFileName.value = fileName
        val lines = content.lines()
        val segments = mutableListOf<GCodeSegment>()

        var currentX = 0f
        var currentY = 0f
        var currentZ = 0f

        lines.forEachIndexed { index, rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isNotBlank()) {
                val isRapid = trimmed.startsWith("G0", ignoreCase = true) || trimmed.contains(" G0 ", ignoreCase = true)
                val isCut = trimmed.startsWith("G1", ignoreCase = true) || trimmed.startsWith("G2", ignoreCase = true) || trimmed.startsWith("G3", ignoreCase = true)

                val xMatch = Regex("""\bX\s*([-+]?[0-9]*\.?[0-9]+)""", RegexOption.IGNORE_CASE).find(trimmed)
                val yMatch = Regex("""\bY\s*([-+]?[0-9]*\.?[0-9]+)""", RegexOption.IGNORE_CASE).find(trimmed)
                val zMatch = Regex("""\bZ\s*([-+]?[0-9]*\.?[0-9]+)""", RegexOption.IGNORE_CASE).find(trimmed)

                val nextX = xMatch?.groupValues?.get(1)?.toFloatOrNull() ?: currentX
                val nextY = yMatch?.groupValues?.get(1)?.toFloatOrNull() ?: currentY
                val nextZ = zMatch?.groupValues?.get(1)?.toFloatOrNull() ?: currentZ

                segments.add(
                    GCodeSegment(
                        lineNumber = index + 1,
                        rawText = trimmed,
                        isRapid = isRapid,
                        isCut = isCut,
                        startX = currentX,
                        startY = currentY,
                        startZ = currentZ,
                        endX = nextX,
                        endY = nextY,
                        endZ = nextZ
                    )
                )

                currentX = nextX
                currentY = nextY
                currentZ = nextZ
            }
        }

        _loadedGCode.value = segments
        _activeGCodeLine.value = 0
        logEvent(LogSeverity.INFO, "GCODE", "Loaded program '$fileName' (${segments.size} blocks)")
    }


    private fun startKinematicsLoop() {
        scope.launch {
            while (isActive) {
                if (_isSimulatedMode.value) {
                    stepSimulation()
                }
                delay(33) // ~30Hz Telemetry Rate
            }
        }
    }

    private fun stepSimulation() {
        val currentAxes = _axes.value.toMutableMap()
        val currentState = _machineState.value
        val activeAxis = _activeJogAxis.value

        // Handle Active Jogging
        if (activeAxis != null && (currentState == MachineStateEnum.ON || currentState == MachineStateEnum.IDLE)) {
            val axis = currentAxes[activeAxis]
            if (axis != null) {
                val delta = (jogDirection * jogSpeed * 0.033) / 60.0 // mm per tick
                val newMachinePos = (axis.machinePos + delta).coerceIn(axis.minLimit, axis.maxLimit)
                val newWorkPos = (axis.workPos + delta)
                val dynamicTorque = 10.0 + (abs(jogSpeed) / 5000.0) * 45.0 + (Math.random() * 3.0)
                val dynamicTemp = 36.0 + (dynamicTorque / 100.0) * 8.0

                currentAxes[activeAxis] = axis.copy(
                    machinePos = round(newMachinePos * 1000.0) / 1000.0,
                    workPos = round(newWorkPos * 1000.0) / 1000.0,
                    loadTorquePct = round(dynamicTorque * 10.0) / 10.0,
                    motorTempC = round(dynamicTemp * 10.0) / 10.0,
                    driveTempC = round((dynamicTemp + 4.5) * 10.0) / 10.0
                )
            }
        }

        // Handle G-Code Program Execution in AUTO mode
        if (currentState == MachineStateEnum.RUNNING) {
            val gcodeList = _loadedGCode.value
            val currentLine = _activeGCodeLine.value
            if (gcodeList.isNotEmpty()) {
                if (currentLine < gcodeList.size) {
                    val seg = gcodeList[currentLine]
                    // Interpolate towards end point
                    val x = currentAxes["X"]
                    val y = currentAxes["Y"]
                    val z = currentAxes["Z"]
                    if (x != null && y != null && z != null) {
                        val dx = seg.endX - x.workPos
                        val dy = seg.endY - y.workPos
                        val dz = seg.endZ - z.workPos
                        val dist = sqrt(dx * dx + dy * dy + dz * dz)

                        if (dist > 0.5) {
                            val step = 0.8
                            val nextX = x.workPos + (dx / dist) * step
                            val nextY = y.workPos + (dy / dist) * step
                            val nextZ = z.workPos + (dz / dist) * step
                            currentAxes["X"] = x.copy(
                                workPos = round(nextX * 1000.0) / 1000.0,
                                machinePos = round((nextX + 100.0) * 1000.0) / 1000.0,
                                dtgPos = round(dist * 1000.0) / 1000.0,
                                loadTorquePct = 25.0 + (Math.random() * 8.0)
                            )
                            currentAxes["Y"] = y.copy(
                                workPos = round(nextY * 1000.0) / 1000.0,
                                machinePos = round((nextY + 70.0) * 1000.0) / 1000.0,
                                dtgPos = round(dist * 1000.0) / 1000.0,
                                loadTorquePct = 22.0 + (Math.random() * 6.0)
                            )
                            currentAxes["Z"] = z.copy(
                                workPos = round(nextZ * 1000.0) / 1000.0,
                                machinePos = round((nextZ - 30.0) * 1000.0) / 1000.0,
                                dtgPos = round(abs(dz) * 1000.0) / 1000.0,
                                loadTorquePct = 30.0 + (Math.random() * 5.0)
                            )
                        } else {
                            _activeGCodeLine.value = (currentLine + 1) % gcodeList.size
                        }
                    }
                }
            }
        }

        _axes.value = currentAxes

        // Spindle dynamics
        val currentSpindle = _spindle.value
        if (currentSpindle.isEnabled) {
            val target = currentSpindle.commandedRpm * (currentSpindle.overridePct / 100.0)
            val currentActual = currentSpindle.actualRpm
            val newActual = currentActual + (target - currentActual) * 0.15 + (Math.random() * 20.0 - 10.0)
            _spindle.value = currentSpindle.copy(actualRpm = round(newActual))
        } else {
            val currentActual = currentSpindle.actualRpm
            if (currentActual > 50) {
                _spindle.value = currentSpindle.copy(actualRpm = round(currentActual * 0.85))
            } else {
                _spindle.value = currentSpindle.copy(actualRpm = 0.0)
            }
        }

        // Slight jitter in EtherCAT Telemetry
        _etherCatMaster.value = _etherCatMaster.value.copy(
            dcOffsetNs = (10L + (Math.random() * 8.0).toLong())
        )
    }

    // Safety and State Commands
    fun toggleEstop() {
        if (_machineState.value == MachineStateEnum.ESTOP) {
            _machineState.value = MachineStateEnum.OFF
            logEvent(LogSeverity.INFO, "SAFETY", "E-Stop circuit reset. Machine in OFF state")
        } else {
            _machineState.value = MachineStateEnum.ESTOP
            stopJog()
            _spindle.value = _spindle.value.copy(isEnabled = false)
            logEvent(LogSeverity.CRITICAL, "SAFETY", "EMERGENCY STOP (ESTOP) TRIPPED - Motion Aborted")
        }
        sendRemoteCommand("ESTOP_TOGGLE", emptyMap())
    }

    fun powerOn() {
        if (_machineState.value != MachineStateEnum.ESTOP) {
            _machineState.value = MachineStateEnum.ON
            logEvent(LogSeverity.INFO, "POWER", "Main Servo Drive Bus ON. Machine Ready")
            sendRemoteCommand("POWER_ON", emptyMap())
        } else {
            logEvent(LogSeverity.WARNING, "SAFETY", "Cannot power ON while ESTOP is engaged")
        }
    }

    fun powerOff() {
        _machineState.value = MachineStateEnum.OFF
        _spindle.value = _spindle.value.copy(isEnabled = false)
        stopJog()
        logEvent(LogSeverity.INFO, "POWER", "Servo Drives Powered OFF")
        sendRemoteCommand("POWER_OFF", emptyMap())
    }

    fun setTaskMode(mode: TaskMode) {
        _taskMode.value = mode
        sendRemoteCommand("SET_MODE", mapOf("mode" to mode.name))
    }

    fun setCoordinateSystem(gSystem: String) {
        _currentCoordSystem.value = gSystem
        sendRemoteCommand("SET_G_COORD", mapOf("coord" to gSystem))
    }

    // Motion & Jog Commands
    fun startJog(axis: String, direction: Int, speedMmMin: Double = 1500.0) {
        if (_machineState.value == MachineStateEnum.ON || _machineState.value == MachineStateEnum.IDLE) {
            _activeJogAxis.value = axis
            jogDirection = direction
            jogSpeed = speedMmMin
            sendRemoteCommand("JOG", mapOf("axis" to axis, "direction" to direction, "speed" to speedMmMin))
        }
    }

    fun stopJog() {
        val axis = _activeJogAxis.value
        _activeJogAxis.value = null
        jogDirection = 0
        if (axis != null) {
            sendRemoteCommand("JOG_STOP", mapOf("axis" to axis))
        }
    }

    fun stepJog(axis: String, direction: Int, stepSizeMm: Double) {
        if (_machineState.value == MachineStateEnum.ON || _machineState.value == MachineStateEnum.IDLE) {
            val currentMap = _axes.value.toMutableMap()
            val axisObj = currentMap[axis]
            if (axisObj != null) {
                val delta = direction * stepSizeMm
                val newMachinePos = (axisObj.machinePos + delta).coerceIn(axisObj.minLimit, axisObj.maxLimit)
                val newWorkPos = axisObj.workPos + delta
                currentMap[axis] = axisObj.copy(
                    machinePos = round(newMachinePos * 1000.0) / 1000.0,
                    workPos = round(newWorkPos * 1000.0) / 1000.0
                )
                _axes.value = currentMap
            }
            sendRemoteCommand("JOG_STEP", mapOf("axis" to axis, "direction" to direction, "step" to stepSizeMm))
        }
    }

    fun zeroAxis(axis: String) {
        val currentMap = _axes.value.toMutableMap()
        val axisObj = currentMap[axis]
        if (axisObj != null) {
            currentMap[axis] = axisObj.copy(workPos = 0.0)
            _axes.value = currentMap
        }
        sendRemoteCommand("ZERO_AXIS", mapOf("axis" to axis, "coord" to _currentCoordSystem.value))
    }

    fun zeroAllAxes() {
        val currentMap = _axes.value.toMutableMap()
        currentMap.keys.forEach { key ->
            val obj = currentMap[key]
            if (obj != null) {
                currentMap[key] = obj.copy(workPos = 0.0)
            }
        }
        _axes.value = currentMap
        logEvent(LogSeverity.INFO, "WCS", "All Axes Zeroed to ${_currentCoordSystem.value} origin")
        sendRemoteCommand("ZERO_ALL", mapOf("coord" to _currentCoordSystem.value))
    }

    fun homeAxis(axis: String) {
        val currentMap = _axes.value.toMutableMap()
        val axisObj = currentMap[axis]
        if (axisObj != null) {
            currentMap[axis] = axisObj.copy(isHomed = true, machinePos = 0.0, workPos = 0.0)
            _axes.value = currentMap
        }
        logEvent(LogSeverity.INFO, "HOMING", "Axis $axis Homed successfully")
        sendRemoteCommand("HOME_AXIS", mapOf("axis" to axis))
    }

    fun homeAllAxes() {
        val currentMap = _axes.value.toMutableMap()
        currentMap.keys.forEach { key ->
            val obj = currentMap[key]
            if (obj != null) {
                currentMap[key] = obj.copy(isHomed = true, machinePos = 0.0, workPos = 0.0)
            }
        }
        _axes.value = currentMap
        logEvent(LogSeverity.INFO, "HOMING", "All machine axes homed to physical index switches")
        sendRemoteCommand("HOME_ALL", emptyMap())
    }

    // Spindle & Feed Controls
    fun toggleSpindle() {
        val cur = _spindle.value
        val newState = !cur.isEnabled
        _spindle.value = cur.copy(isEnabled = newState)
        logEvent(LogSeverity.INFO, "SPINDLE", if (newState) "Spindle ON at ${cur.commandedRpm.toInt()} RPM" else "Spindle STOP")
        sendRemoteCommand("SPINDLE_TOGGLE", mapOf("enabled" to _spindle.value.isEnabled, "rpm" to _spindle.value.commandedRpm))
    }

    fun setSpindleRpm(rpm: Double) {
        _spindle.value = _spindle.value.copy(commandedRpm = rpm)
        sendRemoteCommand("SET_SPINDLE_RPM", mapOf("rpm" to rpm))
    }

    fun setSpindleOverride(pct: Int) {
        _spindle.value = _spindle.value.copy(overridePct = pct.coerceIn(10, 200))
        sendRemoteCommand("SPINDLE_OVERRIDE", mapOf("override" to pct))
    }

    fun setFeedOverride(pct: Int) {
        _feed.value = _feed.value.copy(feedOverridePct = pct.coerceIn(0, 200))
        sendRemoteCommand("FEED_OVERRIDE", mapOf("override" to pct))
    }

    fun toggleMistCoolant() {
        val cur = _coolant.value
        _coolant.value = cur.copy(mist = !cur.mist)
        sendRemoteCommand("COOLANT_MIST", mapOf("mist" to _coolant.value.mist))
    }

    fun toggleFloodCoolant() {
        val cur = _coolant.value
        _coolant.value = cur.copy(flood = !cur.flood)
        sendRemoteCommand("COOLANT_FLOOD", mapOf("flood" to _coolant.value.flood))
    }

    // Program Cycle Controls
    fun cycleStart() {
        if (_machineState.value == MachineStateEnum.ON || _machineState.value == MachineStateEnum.IDLE || _machineState.value == MachineStateEnum.PAUSED) {
            _machineState.value = MachineStateEnum.RUNNING
            _taskMode.value = TaskMode.AUTO
            _spindle.value = _spindle.value.copy(isEnabled = true)
            logEvent(LogSeverity.INFO, "CYCLE", "Cycle Started: Executing program '${_loadedFileName.value}'")
            sendRemoteCommand("CYCLE_START", emptyMap())
        } else {
            logEvent(LogSeverity.WARNING, "CYCLE", "Cannot start cycle: Machine is ${_machineState.value.displayName}")
        }
    }

    fun feedHold() {
        if (_machineState.value == MachineStateEnum.RUNNING) {
            _machineState.value = MachineStateEnum.PAUSED
            logEvent(LogSeverity.WARNING, "CYCLE", "FEED HOLD / PAUSED by Operator")
            sendRemoteCommand("FEEDHOLD", emptyMap())
        }
    }

    fun cycleStop() {
        _machineState.value = MachineStateEnum.IDLE
        _activeGCodeLine.value = 0
        logEvent(LogSeverity.INFO, "CYCLE", "Cycle Aborted / Stopped")
        sendRemoteCommand("ABORT", emptyMap())
    }

    // Metrology & Probing Simulation
    fun triggerProbeRoutine(routineType: String) {
        scope.launch {
            logEvent(LogSeverity.INFO, "PROBE", "Starting Metrology routine: $routineType")
            _probe.value = _probe.value.copy(activeRoutine = routineType, isTripped = false)
            delay(600)
            _probe.value = _probe.value.copy(
                isTripped = true,
                lastContactX = _axes.value["X"]?.workPos ?: 0.0,
                lastContactY = _axes.value["Y"]?.workPos ?: 0.0,
                lastContactZ = _axes.value["Z"]?.workPos ?: 0.0
            )
            logEvent(LogSeverity.INFO, "PROBE", "Touch Contact Confirmed at (${String.format(java.util.Locale.US, "%.3f", _probe.value.lastContactX)}, ${String.format(java.util.Locale.US, "%.3f", _probe.value.lastContactY)}, ${String.format(java.util.Locale.US, "%.3f", _probe.value.lastContactZ)})")
            delay(400)
            _probe.value = _probe.value.copy(isTripped = false, activeRoutine = null)
        }
        sendRemoteCommand("PROBE_CYCLE", mapOf("routine" to routineType))
    }

    // Tool Table & ATC Operations
    fun mountTool(toolId: Int) {
        val currentList = _toolTable.value
        val target = currentList.find { it.id == toolId } ?: return
        val updated = currentList.map { it.copy(isActive = (it.id == toolId)) }
        _toolTable.value = updated
        _activeTool.value = target.copy(isActive = true)
        _tool.value = ToolInfo(
            toolNumber = target.id,
            description = target.description,
            lengthOffset = target.lengthOffset,
            diameterOffset = target.diameter,
            atcSlot = target.pocket
        )
        logEvent(LogSeverity.INFO, "ATC", "Tool changed to T${target.id} (${target.description}), Offset G43 H${target.id} applied")
        sendRemoteCommand("TOOL_CHANGE", mapOf("tool" to toolId, "pocket" to target.pocket))
    }

    fun updateToolItem(updatedTool: CncToolItem) {
        val currentList = _toolTable.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedTool.id }
        if (index >= 0) {
            currentList[index] = updatedTool
        } else {
            currentList.add(updatedTool)
        }
        _toolTable.value = currentList
        if (updatedTool.isActive) {
            _activeTool.value = updatedTool
            _tool.value = ToolInfo(
                toolNumber = updatedTool.id,
                description = updatedTool.description,
                lengthOffset = updatedTool.lengthOffset,
                diameterOffset = updatedTool.diameter,
                atcSlot = updatedTool.pocket
            )
        }
        logEvent(LogSeverity.INFO, "TOOL_TABLE", "Tool T${updatedTool.id} parameters updated")
    }

    fun deleteTool(toolId: Int) {
        val currentList = _toolTable.value.filter { it.id != toolId }
        _toolTable.value = currentList
        logEvent(LogSeverity.INFO, "TOOL_TABLE", "Tool T$toolId removed from carousel")
    }

    fun touchOffToolZ(toolId: Int, currentSpindleZ: Double) {
        val target = _toolTable.value.find { it.id == toolId } ?: return
        val updated = target.copy(lengthOffset = round(abs(currentSpindleZ) * 1000.0) / 1000.0)
        updateToolItem(updated)
        logEvent(LogSeverity.INFO, "TOOL_OFFSET", "T${toolId} Tool Length Offset (TLO) set to ${updated.lengthOffset} mm via Touch-Off")
    }

    // Switch Architecture Profile dynamically
    fun switchArchitecture(arch: HardwareArchitecture) {
        val axesList = when (arch) {
            HardwareArchitecture.ETHERCAT_DELTA -> listOf("X", "Y", "Z", "A")
            HardwareArchitecture.MESA_FPGA -> listOf("X", "Y", "Z")
            HardwareArchitecture.PARPORT_LEGACY -> listOf("X", "Y", "Z")
            HardwareArchitecture.STEP_DIR_CLOSED_LOOP -> listOf("X", "Y", "Z")
        }

        _capabilities.value = _capabilities.value.copy(
            architecture = arch,
            compatibilityLevel = arch.level,
            axes = axesList,
            hasServoTorque = (arch == HardwareArchitecture.ETHERCAT_DELTA),
            hasDriveTemp = (arch == HardwareArchitecture.ETHERCAT_DELTA),
            hasEtherCat = (arch == HardwareArchitecture.ETHERCAT_DELTA),
            hasProbe = (arch != HardwareArchitecture.PARPORT_LEGACY || true)
        )

        // Adjust axes map if needed
        val newMap = mutableMapOf<String, AxisCoord>()
        axesList.forEach { ax ->
            newMap[ax] = _axes.value[ax] ?: AxisCoord(name = ax)
        }
        _axes.value = newMap
    }

    // Network / Live Server Connection
    fun connectToHost(hostIp: String, port: Int = 8000) {
        scope.launch {
            _capabilities.value = _capabilities.value.copy(hostIp = hostIp, port = port)
            try {
                okHttpClient = OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .build()

                val request = Request.Builder()
                    .url("ws://$hostIp:$port/ws/telemetry")
                    .build()

                webSocket = okHttpClient?.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        isConnectedToRealServer = true
                        _isSimulatedMode.value = false
                        _capabilities.value = _capabilities.value.copy(isConnected = true, pingMs = 5)
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        parseIncomingTelemetry(text)
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        isConnectedToRealServer = false
                        _isSimulatedMode.value = true
                        _capabilities.value = _capabilities.value.copy(isConnected = false)
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        isConnectedToRealServer = false
                        _isSimulatedMode.value = true
                    }
                })
            } catch (e: Exception) {
                _isSimulatedMode.value = true
            }
        }
    }

    private fun parseIncomingTelemetry(jsonText: String) {
        try {
            val json = JSONObject(jsonText)
            if (json.has("state")) {
                val stateStr = json.getString("state")
                _machineState.value = when (stateStr) {
                    "ON" -> MachineStateEnum.ON
                    "ESTOP" -> MachineStateEnum.ESTOP
                    "RUNNING" -> MachineStateEnum.RUNNING
                    "PAUSED" -> MachineStateEnum.PAUSED
                    else -> MachineStateEnum.OFF
                }
            }
        } catch (_: Exception) {}
    }

    private fun sendRemoteCommand(cmdName: String, args: Map<String, Any>) {
        if (isConnectedToRealServer && webSocket != null) {
            val json = JSONObject()
            json.put("command", cmdName)
            val argsObj = JSONObject()
            args.forEach { (k, v) -> argsObj.put(k, v) }
            json.put("args", argsObj)
            webSocket?.send(json.toString())
        }
    }

    private fun loadSampleGCode() {
        val sampleCode = listOf(
            GCodeSegment(1, "G21 (Metric Units)", isRapid = false, isCut = false),
            GCodeSegment(2, "G90 G54 (Absolute Coord System)", isRapid = false, isCut = false),
            GCodeSegment(3, "G0 Z10.000 (Safe Height)", isRapid = true, isCut = false, startX = 0f, startY = 0f, startZ = 0f, endX = 0f, endY = 0f, endZ = 10f),
            GCodeSegment(4, "G0 X10.000 Y10.000", isRapid = true, isCut = false, startX = 0f, startY = 0f, startZ = 10f, endX = 10f, endY = 10f, endZ = 10f),
            GCodeSegment(5, "G1 Z-2.500 F500", isRapid = false, isCut = true, startX = 10f, startY = 10f, startZ = 10f, endX = 10f, endY = 10f, endZ = -2.5f),
            GCodeSegment(6, "G1 X60.000 Y10.000 F1500", isRapid = false, isCut = true, startX = 10f, startY = 10f, startZ = -2.5f, endX = 60f, endY = 10f, endZ = -2.5f),
            GCodeSegment(7, "G1 X60.000 Y60.000", isRapid = false, isCut = true, startX = 60f, startY = 10f, startZ = -2.5f, endX = 60f, endY = 60f, endZ = -2.5f),
            GCodeSegment(8, "G1 X10.000 Y60.000", isRapid = false, isCut = true, startX = 60f, startY = 60f, startZ = -2.5f, endX = 10f, endY = 60f, endZ = -2.5f),
            GCodeSegment(9, "G1 X10.000 Y10.000", isRapid = false, isCut = true, startX = 10f, startY = 60f, startZ = -2.5f, endX = 10f, endY = 10f, endZ = -2.5f),
            GCodeSegment(10, "G0 Z15.000 M5 (Retract & Spindle Off)", isRapid = true, isCut = false, startX = 10f, startY = 10f, startZ = -2.5f, endX = 10f, endY = 10f, endZ = 15f),
            GCodeSegment(11, "G0 X0.000 Y0.000 (Return Home)", isRapid = true, isCut = false, startX = 10f, startY = 10f, startZ = 15f, endX = 0f, endY = 0f, endZ = 15f),
            GCodeSegment(12, "M30 (End of Program)", isRapid = false, isCut = false)
        )
        _loadedGCode.value = sampleCode
    }

    // --- Metrological Axis Calibration Engine ---
    private val _activeCalibrationSession = MutableStateFlow<AxisCalibrationSession?>(null)
    val activeCalibrationSession: StateFlow<AxisCalibrationSession?> = _activeCalibrationSession.asStateFlow()

    fun startAxisCalibration(
        axis: String = "X",
        totalTravelMm: Double = 600.0,
        stepIntervalPercent: Double = 10.0,
        instrumentName: String = "Dial Indicator (0.001mm Resolution)",
        instrumentUncertaintyMm: Double = 0.003
    ) {
        val totalSteps = ((100.0 / stepIntervalPercent).toInt() + 1).coerceAtLeast(2)
        val stepSizeMm = totalTravelMm / (totalSteps - 1)

        val points = (0 until totalSteps).map { i ->
            val percent = i * stepIntervalPercent
            val nominalPos = i * stepSizeMm
            AxisCalibrationPoint(
                stepIndex = i,
                percentOfTravel = percent,
                nominalPositionMm = nominalPos,
                measuredPositionMm = null,
                errorMm = null,
                sectorUncertaintyMm = instrumentUncertaintyMm
            )
        }

        val session = AxisCalibrationSession(
            axis = axis,
            totalTravelMm = totalTravelMm,
            stepIntervalPercent = stepIntervalPercent,
            totalSteps = totalSteps,
            instrumentName = instrumentName,
            instrumentUncertaintyMm = instrumentUncertaintyMm,
            points = points,
            isCompleted = false
        )
        _activeCalibrationSession.value = session
        logEvent(LogSeverity.INFO, "METROLOGY", "Started $axis-Axis Calibration session with $totalSteps points ($stepIntervalPercent% step)")
    }

    fun recordCalibrationMeasurement(stepIndex: Int, measuredValueMm: Double) {
        val current = _activeCalibrationSession.value ?: return
        val updatedPoints = current.points.map { pt ->
            if (pt.stepIndex == stepIndex) {
                val error = measuredValueMm - pt.nominalPositionMm
                pt.copy(
                    measuredPositionMm = measuredValueMm,
                    errorMm = error,
                    sectorUncertaintyMm = current.instrumentUncertaintyMm
                )
            } else pt
        }

        val measuredPoints = updatedPoints.filter { it.measuredPositionMm != null }
        val isAllCompleted = measuredPoints.size == updatedPoints.size

        val maxErr = if (measuredPoints.isNotEmpty()) measuredPoints.maxOf { Math.abs(it.errorMm ?: 0.0) } else 0.0
        val meanErr = if (measuredPoints.isNotEmpty()) measuredPoints.map { it.errorMm ?: 0.0 }.average() else 0.0

        // Expanded uncertainty k=2 calculation: Uc = sqrt(u_inst^2 + u_repeat^2) * 2
        val repeatUncertainty = if (measuredPoints.size > 1) {
            val variance = measuredPoints.map { Math.pow((it.errorMm ?: 0.0) - meanErr, 2.0) }.sum() / (measuredPoints.size - 1)
            Math.sqrt(variance)
        } else current.instrumentUncertaintyMm

        val combinedUncertainty = Math.sqrt(
            Math.pow(current.instrumentUncertaintyMm, 2.0) + Math.pow(repeatUncertainty, 2.0)
        )
        val expandedUncertainty = combinedUncertainty * 2.0

        _activeCalibrationSession.value = current.copy(
            points = updatedPoints,
            isCompleted = isAllCompleted,
            maxErrorMm = maxErr,
            meanErrorMm = meanErr,
            expandedUncertaintyMm = expandedUncertainty
        )

        logEvent(LogSeverity.INFO, "METROLOGY", "Recorded ${current.axis} point [$stepIndex] Nominal=${updatedPoints[stepIndex].nominalPositionMm}mm, Measured=${measuredValueMm}mm, Err=${String.format(java.util.Locale.US, "%.4f", updatedPoints[stepIndex].errorMm)}mm")
    }

    fun moveAxisToNominal(stepIndex: Int) {
        val current = _activeCalibrationSession.value ?: return
        val point = current.points.getOrNull(stepIndex) ?: return
        val axisName = current.axis
        val targetPos = point.nominalPositionMm

        val currentAxes = _axes.value.toMutableMap()
        val currentAxisData = currentAxes[axisName] ?: AxisCoord(name = axisName)
        currentAxes[axisName] = currentAxisData.copy(
            workPos = targetPos,
            machinePos = targetPos,
            isHomed = true
        )
        _axes.value = currentAxes
        logEvent(LogSeverity.INFO, "METROLOGY", "Moved Axis $axisName to Nominal Pos $targetPos mm for Calibration Step $stepIndex")
    }

    fun generateLinuxCncCompTable(session: AxisCalibrationSession): String {
        val sb = StringBuilder()
        sb.append("# LinuxCNC Screw Pitch Compensation Table (comp.tbl)\n")
        sb.append("# Axis: ${session.axis} | Total Travel: ${session.totalTravelMm} mm\n")
        sb.append("# Instrument: ${session.instrumentName} (±${session.instrumentUncertaintyMm} mm)\n")
        sb.append("# Max Deviation: ${String.format("%.4f", session.maxErrorMm)} mm | Expanded Uncertainty U (k=2): ±${String.format("%.4f", session.expandedUncertaintyMm)} mm\n")
        sb.append("# Format: Nominal_Position_Pos_Forward  Compensation_Forward  Nominal_Position_Neg_Reverse  Compensation_Reverse\n\n")

        session.points.forEach { pt ->
            val nominal = pt.nominalPositionMm
            val comp = if (pt.errorMm != null) -pt.errorMm else 0.0 // Negative of error is compensation
            sb.append(String.format(java.util.Locale.US, "%10.4f  %10.4f  %10.4f  %10.4f\n", nominal, comp, nominal, comp))
        }
        return sb.toString()
    }
}
