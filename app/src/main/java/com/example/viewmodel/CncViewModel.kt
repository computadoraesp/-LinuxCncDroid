package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.CncAppDatabase
import com.example.data.local.MachineProfileEntity
import com.example.data.local.MdiMacroEntity
import com.example.model.*
import com.example.service.CncFeedbackManager
import com.example.service.CncSecurityScanner
import com.example.service.LinuxCncEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CncViewModel(application: Application) : AndroidViewModel(application) {

    val engine = LinuxCncEngine()
    val feedbackManager = CncFeedbackManager(application)
    val securityScanner = CncSecurityScanner()

    private val db = Room.databaseBuilder(
        application,
        CncAppDatabase::class.java,
        "linuxcnc_hmi.db"
    ).build()

    // State flows from engine
    val machineState: StateFlow<MachineStateEnum> = engine.machineState
    val taskMode: StateFlow<TaskMode> = engine.taskMode
    val currentCoordSystem: StateFlow<String> = engine.currentCoordSystem
    val axes: StateFlow<Map<String, AxisCoord>> = engine.axes
    val spindle: StateFlow<SpindleInfo> = engine.spindle
    val feed: StateFlow<FeedInfo> = engine.feed
    val coolant: StateFlow<CoolantInfo> = engine.coolant
    val probe: StateFlow<ProbeInfo> = engine.probe
    val tool: StateFlow<ToolInfo> = engine.tool
    val etherCatMaster: StateFlow<EtherCatMasterInfo> = engine.etherCatMaster
    val etherCatSlaves: StateFlow<List<EtherCatSlaveInfo>> = engine.etherCatSlaves
    val capabilities: StateFlow<CapabilitiesManifest> = engine.capabilities
    val activeGCodeLine: StateFlow<Int> = engine.activeGCodeLine
    val loadedGCode: StateFlow<List<GCodeSegment>> = engine.loadedGCode
    val loadedFileName: StateFlow<String> = engine.loadedFileName
    val isSimulatedMode: StateFlow<Boolean> = engine.isSimulatedMode
    val eventLogs: StateFlow<List<CncEventLog>> = engine.eventLogs
    val networkLatencyMs: StateFlow<Int> = engine.networkLatencyMs
    val toolTable: StateFlow<List<CncToolItem>> = engine.toolTable
    val activeTool: StateFlow<CncToolItem> = engine.activeTool
    val cycleElapsedSeconds: StateFlow<Long> = engine.cycleElapsedSeconds
    val cycleEstimatedTotalSeconds: StateFlow<Long> = engine.cycleEstimatedTotalSeconds

    // Jog style: BUTTON_PAD vs VIRTUAL_MPG
    private val _jogStyle = MutableStateFlow(JogControlStyle.BUTTON_PAD)
    val jogStyle: StateFlow<JogControlStyle> = _jogStyle.asStateFlow()

    // MPG selected axis & multiplier
    private val _mpgAxis = MutableStateFlow("X")
    val mpgAxis: StateFlow<String> = _mpgAxis.asStateFlow()

    private val _mpgMultiplier = MutableStateFlow(MpgMultiplier.X100)
    val mpgMultiplier: StateFlow<MpgMultiplier> = _mpgMultiplier.asStateFlow()

    // User Role
    private val _userRole = MutableStateFlow(UserRole.OPERATOR)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    // Active Unit System: METRIC (G21 / mm) vs IMPERIAL (G20 / inch)
    private val _unitSystem = MutableStateFlow(UnitSystem.METRIC)
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    // Selected Jog increment step
    private val _jogStep = MutableStateFlow(1.0) // 1.0mm or 0.1in default
    val jogStep: StateFlow<Double> = _jogStep.asStateFlow()

    // Jog mode: Continuous vs Step
    private val _isContinuousJog = MutableStateFlow(true)
    val isContinuousJog: StateFlow<Boolean> = _isContinuousJog.asStateFlow()

    // Jog Speed slider
    private val _jogSpeedMmMin = MutableStateFlow(1500.0)
    val jogSpeedMmMin: StateFlow<Double> = _jogSpeedMmMin.asStateFlow()

    // MDI Input
    private val _mdiCommandText = MutableStateFlow("")
    val mdiCommandText: StateFlow<String> = _mdiCommandText.asStateFlow()

    private val _mdiHistory = MutableStateFlow<List<String>>(
        listOf("G0 X0 Y0 Z10", "G1 Z-5 F300", "M3 S12000", "G54", "G28", "T1 M6")
    )
    val mdiHistory: StateFlow<List<String>> = _mdiHistory.asStateFlow()

    // Persistent Profiles and Macros from DB
    val machineProfiles: StateFlow<List<MachineProfileEntity>> = db.profileDao().getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val macros: StateFlow<List<MdiMacroEntity>> = db.macroDao().getAllMacros()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            // Seed sample profiles if empty
            val initialProfiles = listOf(
                MachineProfileEntity(name = "Workshop VMC-850 (EtherCAT + Delta)", hostIp = "192.168.1.100", architecture = "ETHERCAT_DELTA", isDefault = true),
                MachineProfileEntity(name = "Prototype Router (Mesa 7i96S FPGA)", hostIp = "10.42.0.1", architecture = "MESA_FPGA"),
                MachineProfileEntity(name = "Mini Mill (Parallel Port Legacy)", hostIp = "192.168.1.150", architecture = "PARPORT_LEGACY")
            )
            initialProfiles.forEach { db.profileDao().insertProfile(it) }

            val initialMacros = listOf(
                MdiMacroEntity("m1", "Zero All (G54)", "G10 L20 P1 X0 Y0 Z0", "Set current location as G54 Work Zero", "SETUP"),
                MdiMacroEntity("m2", "Park Position", "G0 G53 Z0\nG0 G53 X0 Y300", "Retract Z and move table forward", "MOTION"),
                MdiMacroEntity("m3", "Spindle Warmup", "M3 S3000\nG4 P5\nM3 S8000\nG4 P5\nM3 S15000", "3-Stage spindle bearing warmup cycle", "SPINDLE"),
                MdiMacroEntity("m4", "Laser Crosshair", "M64 P0", "Toggle optical alignment laser crosshair", "TOOLING"),
                MdiMacroEntity("m5", "Tool Length Touch", "G38.2 Z-50 F100\nG91 G0 Z2\nG90", "Execute toolsetter probe touch routine", "PROBING")
            )
            db.macroDao().insertMacros(initialMacros)
        }
    }

    // Role switching
    fun setUserRole(role: UserRole) {
        _userRole.value = role
        feedbackManager.triggerActionClick()
        engine.logEvent(LogSeverity.INFO, "AUTH", "Active security level switched to ${role.displayName}")
    }

    // Unit System switching (G21 MM <-> G20 INCH)
    fun toggleUnitSystem() {
        val newUnit = if (_unitSystem.value == UnitSystem.METRIC) UnitSystem.IMPERIAL else UnitSystem.METRIC
        _unitSystem.value = newUnit
        // Reset default jog step to standard increment for selected unit
        _jogStep.value = if (newUnit == UnitSystem.IMPERIAL) 0.010 else 1.000
        feedbackManager.triggerSuccessHaptic()
        engine.logEvent(
            LogSeverity.INFO,
            "MODAL",
            "Active Unit System switched to ${newUnit.code} (${newUnit.shortLabel} / ${newUnit.lengthUnit})"
        )
    }

    fun setUnitSystem(unit: UnitSystem) {
        _unitSystem.value = unit
        _jogStep.value = if (unit == UnitSystem.IMPERIAL) 0.010 else 1.000
        feedbackManager.triggerActionClick()
        engine.logEvent(
            LogSeverity.INFO,
            "MODAL",
            "Active Unit System set to ${unit.code} (${unit.shortLabel} / ${unit.lengthUnit})"
        )
    }

    // Wrapped actions with Haptics and Audio
    fun toggleEstop() {
        feedbackManager.triggerEstopHaptic()
        if (engine.machineState.value != MachineStateEnum.ESTOP) {
            feedbackManager.playEstopSound()
        }
        engine.toggleEstop()
    }

    fun powerOn() {
        feedbackManager.triggerActionClick()
        engine.powerOn()
    }

    fun powerOff() {
        feedbackManager.triggerActionClick()
        engine.powerOff()
    }

    fun stepJog(axis: String, direction: Int) {
        feedbackManager.triggerJogTick()
        val stepInMm = if (_unitSystem.value == UnitSystem.IMPERIAL) {
            _jogStep.value * 25.4
        } else {
            _jogStep.value
        }
        engine.stepJog(axis, direction, stepInMm)
    }

    fun startJog(axis: String, direction: Int) {
        feedbackManager.triggerActionClick()
        engine.startJog(axis, direction, _jogSpeedMmMin.value)
    }

    fun stopJog() {
        engine.stopJog()
    }

    fun zeroAxis(axis: String) {
        feedbackManager.triggerSuccessHaptic()
        engine.zeroAxis(axis)
    }

    fun zeroAllAxes() {
        feedbackManager.triggerSuccessHaptic()
        engine.zeroAllAxes()
    }

    fun homeAxis(axis: String) {
        feedbackManager.triggerActionClick()
        engine.homeAxis(axis)
    }

    fun homeAllAxes() {
        feedbackManager.triggerActionClick()
        engine.homeAllAxes()
    }

    fun cycleStart() {
        feedbackManager.triggerActionClick()
        engine.cycleStart()
    }

    fun feedHold() {
        feedbackManager.triggerWarningHaptic()
        feedbackManager.playWarningBeep()
        engine.feedHold()
    }

    fun cycleStop() {
        feedbackManager.triggerActionClick()
        engine.cycleStop()
    }

    fun triggerProbe(routine: String) {
        feedbackManager.triggerActionClick()
        engine.triggerProbeRoutine(routine)
    }

    fun applySpeedsFeeds(rpm: Double, feedMmMin: Double) {
        feedbackManager.triggerSuccessHaptic()
        engine.setSpindleRpm(rpm)
        engine.logEvent(LogSeverity.INFO, "METROLOGY", "Applied Speeds & Feeds: ${rpm.toInt()} RPM, Feed: ${feedMmMin.toInt()} mm/min")
    }

    fun loadAndScanGCode(fileName: String, content: String) {
        val result = securityScanner.scanGCode(fileName, content)
        if (result.isExecutable) {
            feedbackManager.triggerSuccessHaptic()
            engine.loadGCodeContent(fileName, content)
            val fp = if (result.sha256Fingerprint.length >= 8) result.sha256Fingerprint.substring(0, 8) else result.sha256Fingerprint
            engine.logEvent(LogSeverity.INFO, "SECURITY", "G-Code verification PASSED [${result.threatLevel.displayName}]. SHA-256: $fp")
        } else {
            feedbackManager.triggerEstopHaptic()
            feedbackManager.playErrorAlarm()
            engine.logEvent(LogSeverity.CRITICAL, "SECURITY", "THREAT BLOCKED: Program '$fileName' contains unauthorized exploit payloads!")
        }
    }

    fun simulateDiagnosticAlarm() {
        feedbackManager.triggerWarningHaptic()
        feedbackManager.playErrorAlarm()
        engine.logEvent(LogSeverity.ERROR, "ETHERCAT", "ALARM AL.006 (Over-Torque Protection Tripped on Axis Z)")
    }

    fun clearLogs() {
        feedbackManager.triggerActionClick()
        engine.clearEventLogs()
    }

    fun setJogStyle(style: JogControlStyle) {
        feedbackManager.triggerActionClick()
        _jogStyle.value = style
    }

    fun setMpgAxis(axis: String) {
        feedbackManager.triggerActionClick()
        _mpgAxis.value = axis
    }

    fun setMpgMultiplier(mult: MpgMultiplier) {
        feedbackManager.triggerActionClick()
        _mpgMultiplier.value = mult
    }

    fun sendMpgStep(axis: String, direction: Int, multiplier: MpgMultiplier) {
        feedbackManager.triggerJogTick()
        val stepMm = multiplier.getStep(_unitSystem.value)
        engine.stepJog(axis, direction, stepMm)
    }

    fun mountTool(toolId: Int) {
        feedbackManager.triggerSuccessHaptic()
        engine.mountTool(toolId)
    }

    fun updateTool(tool: CncToolItem) {
        feedbackManager.triggerSuccessHaptic()
        engine.updateToolItem(tool)
    }

    fun deleteTool(toolId: Int) {
        feedbackManager.triggerActionClick()
        engine.deleteTool(toolId)
    }

    fun touchOffToolZ(toolId: Int) {
        feedbackManager.triggerSuccessHaptic()
        val currentZ = engine.axes.value["Z"]?.workPos ?: 0.0
        engine.touchOffToolZ(toolId, currentZ)
    }

    fun setJogStep(step: Double) {
        feedbackManager.triggerJogTick()
        _jogStep.value = step
    }

    fun setJogMode(continuous: Boolean) {
        feedbackManager.triggerActionClick()
        _isContinuousJog.value = continuous
    }

    fun setJogSpeed(speed: Double) {
        _jogSpeedMmMin.value = speed
    }

    fun setMdiText(text: String) {
        _mdiCommandText.value = text
    }

    fun executeMdiCommand(cmd: String = _mdiCommandText.value) {
        if (cmd.isNotBlank()) {
            feedbackManager.triggerActionClick()
            val updated = _mdiHistory.value.toMutableList()
            updated.add(0, cmd)
            _mdiHistory.value = updated.take(25)
            _mdiCommandText.value = ""
            engine.logEvent(LogSeverity.INFO, "MDI", "Executed: $cmd")
        }
    }

    fun saveProfile(name: String, ip: String, port: Int, arch: String) {
        viewModelScope.launch {
            db.profileDao().insertProfile(
                MachineProfileEntity(
                    name = name,
                    hostIp = ip,
                    port = port,
                    architecture = arch
                )
            )
            feedbackManager.triggerSuccessHaptic()
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            db.profileDao().deleteProfile(id)
            feedbackManager.triggerActionClick()
        }
    }

    // --- Metrological Calibration Bridge ---
    val activeCalibrationSession: StateFlow<AxisCalibrationSession?> = engine.activeCalibrationSession

    fun startAxisCalibration(
        axis: String = "X",
        totalTravelMm: Double = 600.0,
        stepIntervalPercent: Double = 10.0,
        instrumentName: String = "Dial Indicator (0.001mm Resolution)",
        instrumentUncertaintyMm: Double = 0.003
    ) {
        feedbackManager.triggerActionClick()
        engine.startAxisCalibration(axis, totalTravelMm, stepIntervalPercent, instrumentName, instrumentUncertaintyMm)
    }

    fun recordCalibrationMeasurement(stepIndex: Int, measuredValueMm: Double) {
        feedbackManager.triggerSuccessHaptic()
        engine.recordCalibrationMeasurement(stepIndex, measuredValueMm)
    }

    fun moveAxisToNominal(stepIndex: Int) {
        feedbackManager.triggerJogTick()
        engine.moveAxisToNominal(stepIndex)
    }

    fun generateLinuxCncCompTable(session: AxisCalibrationSession): String {
        return engine.generateLinuxCncCompTable(session)
    }
}
