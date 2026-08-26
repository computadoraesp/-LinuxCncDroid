package com.example.model

enum class MachineStateEnum(val displayName: String) {
    ESTOP("ESTOP ACTIVE"),
    OFF("MACHINE OFF"),
    ON("MACHINE READY"),
    IDLE("IDLE"),
    RUNNING("CYCLE RUNNING"),
    PAUSED("FEED HOLD / PAUSED"),
    HOMING("HOMING AXES"),
    ERROR("SYSTEM ERROR")
}

enum class TaskMode(val displayName: String) {
    MANUAL("MANUAL"),
    MDI("MDI CONSOLE"),
    AUTO("AUTO PROGRAM")
}

enum class HardwareArchitecture(val displayName: String, val level: Int, val description: String) {
    ETHERCAT_DELTA("EtherCAT + Delta B3/A3 Servos", 3, "High-speed 100Mbps bus, real-time torque/temperature telemetry, CiA 402 drives"),
    MESA_FPGA("Mesa 7i96S / 7i76E FPGA", 2, "Hardware Step/Dir generation, high-speed differential encoders, isolated I/O"),
    PARPORT_LEGACY("Parallel Port (Legacy Step/Dir)", 1, "Software step generation, standard LinuxCNC HAL motion kernel"),
    STEP_DIR_CLOSED_LOOP("Closed-Loop Stepper Hybrid", 2, "Encoder step loss detection, fast positioning with step/dir abstraction")
}

enum class UserRole(val displayName: String) {
    OPERATOR("Operator (Control & Jog)"),
    VIEWER("Viewer (Read-Only DRO)"),
    ADMIN("Administrator (Full Tuning & MDI)")
}

data class AxisCoord(
    val name: String,
    val machinePos: Double = 0.0,
    val workPos: Double = 0.0,
    val dtgPos: Double = 0.0,
    val isHomed: Boolean = true,
    val minLimit: Double = -500.0,
    val maxLimit: Double = 500.0,
    val loadTorquePct: Double = 12.5,
    val motorTempC: Double = 34.0,
    val driveTempC: Double = 38.5,
    val encoderCounts: Long = 0L
)

data class SpindleInfo(
    val commandedRpm: Double = 12000.0,
    val actualRpm: Double = 11985.0,
    val isEnabled: Boolean = false,
    val isClockwise: Boolean = true,
    val overridePct: Int = 100,
    val loadAmps: Double = 2.4
)

data class FeedInfo(
    val commandedFeed: Double = 1500.0,
    val actualFeed: Double = 1495.0,
    val feedOverridePct: Int = 100,
    val rapidOverridePct: Int = 100
)

data class CoolantInfo(
    val mist: Boolean = false,
    val flood: Boolean = false
)

data class ProbeInfo(
    val isTripped: Boolean = false,
    val lastContactX: Double = 0.0,
    val lastContactY: Double = 0.0,
    val lastContactZ: Double = 0.0,
    val activeRoutine: String? = null
)

data class ToolInfo(
    val toolNumber: Int = 1,
    val description: String = "6mm 2-Flute Carbide Endmill",
    val lengthOffset: Double = 45.230,
    val diameterOffset: Double = 6.000,
    val atcSlot: Int = 1
)

enum class ToolType(val displayName: String, val iconName: String) {
    ENDMILL("Flat Endmill", "ic_endmill"),
    BALLNOSE("Ball Nose", "ic_ballnose"),
    FACE_MILL("Face Mill", "ic_facemill"),
    DRILL("Twist Drill", "ic_drill"),
    CHAMFER("Chamfer Mill", "ic_chamfer"),
    TAP("Thread Tap", "ic_tap"),
    TOUCH_PROBE("3D Touch Probe", "ic_probe"),
    FLY_CUTTER("Fly Cutter", "ic_flycutter")
}

data class CncToolItem(
    val id: Int,
    val pocket: Int,
    val description: String,
    val diameter: Double,
    val lengthOffset: Double,
    val wearLength: Double = 0.0,
    val wearDiameter: Double = 0.0,
    val toolType: ToolType = ToolType.ENDMILL,
    val flutes: Int = 3,
    val maxRpm: Double = 24000.0,
    val lifeMinutesCurrent: Double = 24.5,
    val lifeMinutesMax: Double = 120.0,
    val isActive: Boolean = false,
    val holderType: String = "ER20 / ISO30"
)

enum class UnitSystem(
    val code: String,
    val shortLabel: String,
    val lengthUnit: String,
    val speedUnit: String,
    val precisionDecimals: Int
) {
    METRIC("G21", "MM", "mm", "mm/min", 3),
    IMPERIAL("G20", "INCH", "in", "IPM", 4);

    fun formatPosition(posMm: Double): String {
        return if (this == IMPERIAL) {
            val inches = posMm / 25.4
            java.lang.String.format(java.util.Locale.US, "%+08.4f", inches)
        } else {
            java.lang.String.format(java.util.Locale.US, "%+08.3f", posMm)
        }
    }

    fun formatSpeed(speedMmMin: Double): String {
        return if (this == IMPERIAL) {
            val ipm = speedMmMin / 25.4
            "${java.lang.String.format(java.util.Locale.US, "%.1f", ipm)} IPM"
        } else {
            "${speedMmMin.toInt()} mm/min"
        }
    }

    fun toDisplayLength(lengthMm: Double): Double {
        return if (this == IMPERIAL) lengthMm / 25.4 else lengthMm
    }

    fun toMm(displayLength: Double): Double {
        return if (this == IMPERIAL) displayLength * 25.4 else displayLength
    }
}

enum class MpgMultiplier(val stepMm: Double, val metricLabel: String, val imperialLabel: String) {
    X1(0.001, "x1 (0.001 mm)", "x1 (0.0001 in)"),
    X10(0.010, "x10 (0.010 mm)", "x10 (0.001 in)"),
    X100(0.100, "x100 (0.100 mm)", "x100 (0.010 in)"),
    X1000(1.000, "x1000 (1.000 mm)", "x1000 (0.100 in)");

    val label: String get() = metricLabel

    fun getStep(unit: UnitSystem): Double {
        return if (unit == UnitSystem.IMPERIAL) {
            when (this) {
                X1 -> 0.00254 // 0.0001" in mm
                X10 -> 0.0254 // 0.001" in mm
                X100 -> 0.254 // 0.010" in mm
                X1000 -> 2.54 // 0.100" in mm
            }
        } else {
            stepMm
        }
    }

    fun getLabel(unit: UnitSystem): String {
        return if (unit == UnitSystem.IMPERIAL) imperialLabel else metricLabel
    }
}

enum class JogControlStyle(val displayName: String) {
    BUTTON_PAD("KEYPAD"),
    VIRTUAL_MPG("MPG WHEEL")
}

data class EtherCatSlaveInfo(
    val slaveIndex: Int,
    val name: String,
    val state: String = "OP (Operational)",
    val actualTorquePct: Double = 14.2,
    val driveTempC: Double = 39.0,
    val alarmCode: String = "AL.000 (NORMAL)",
    val isFault: Boolean = false
)

data class EtherCatMasterInfo(
    val masterState: String = "OP (Operational)",
    val slaveCount: Int = 4,
    val busCycleTimeUs: Int = 1000,
    val packetLossPct: Double = 0.00,
    val dcOffsetNs: Long = 12
)

data class CapabilitiesManifest(
    val machineName: String = "Industrial VMC-850",
    val architecture: HardwareArchitecture = HardwareArchitecture.ETHERCAT_DELTA,
    val compatibilityLevel: Int = 3,
    val axes: List<String> = listOf("X", "Y", "Z", "A"),
    val hasProbe: Boolean = true,
    val hasToolChanger: Boolean = true,
    val hasSpindleEncoder: Boolean = true,
    val hasServoTorque: Boolean = true,
    val hasDriveTemp: Boolean = true,
    val hasEtherCat: Boolean = true,
    val hostIp: String = "192.168.1.100",
    val port: Int = 8000,
    val isConnected: Boolean = true,
    val pingMs: Int = 4
)

data class GCodeSegment(
    val lineNumber: Int,
    val rawText: String,
    val isRapid: Boolean = false,
    val isCut: Boolean = true,
    val startX: Float = 0f,
    val startY: Float = 0f,
    val startZ: Float = 0f,
    val endX: Float = 0f,
    val endY: Float = 0f,
    val endZ: Float = 0f
)

data class MdiMacro(
    val id: String,
    val label: String,
    val command: String,
    val description: String,
    val category: String = "SETUP"
)

enum class LogSeverity(val displayName: String) {
    INFO("INFO"),
    WARNING("WARN"),
    ERROR("ERROR"),
    CRITICAL("CRITICAL"),
    SECURITY("SECURITY")
}

data class CncEventLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val severity: LogSeverity = LogSeverity.INFO,
    val tag: String = "SYSTEM",
    val message: String = ""
)

enum class ThreatLevel(val displayName: String) {
    CLEAN("SECURE / VERIFIED"),
    SUSPICIOUS("SECURITY WARNING"),
    MALWARE_BLOCKED("THREAT BLOCKED")
}

data class SecurityThreat(
    val code: String,
    val title: String,
    val description: String,
    val lineNumber: Int = -1,
    val lineContent: String = "",
    val severity: LogSeverity = LogSeverity.SECURITY
)

data class SecurityScanResult(
    val fileName: String = "program.ngc",
    val fileSizeBytes: Long = 0L,
    val totalLines: Int = 0,
    val threatLevel: ThreatLevel = ThreatLevel.CLEAN,
    val threats: List<SecurityThreat> = emptyList(),
    val isExecutable: Boolean = true,
    val scanDurationMs: Long = 0L,
    val sha256Fingerprint: String = "",
    val boundingBoxX: Pair<Float, Float> = Pair(0f, 0f),
    val boundingBoxY: Pair<Float, Float> = Pair(0f, 0f),
    val boundingBoxZ: Pair<Float, Float> = Pair(0f, 0f)
)

data class MaterialPreset(
    val id: String,
    val name: String,
    val category: String,
    val surfaceSpeedMMin: Double, // Vc (m/min)
    val feedPerToothMm: Double,   // Fz (mm/tooth for 6mm standard)
    val powerFactor: Double       // specific cutting force factor
)

data class SpeedFeedCalculation(
    val material: MaterialPreset,
    val toolDiameterMm: Double,
    val flutes: Int,
    val calculatedRpm: Double,
    val calculatedFeedMmMin: Double,
    val recommendedDocMm: Double,
    val recommendedWocMm: Double,
    val spindlePowerKw: Double
)

data class AxisCalibrationPoint(
    val stepIndex: Int,
    val percentOfTravel: Double, // 0.0 to 100.0%
    val nominalPositionMm: Double,
    val measuredPositionMm: Double? = null,
    val errorMm: Double? = null,
    val sectorUncertaintyMm: Double = 0.002
)

data class AxisCalibrationSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val axis: String = "X",
    val totalTravelMm: Double = 600.0,
    val stepIntervalPercent: Double = 10.0,
    val totalSteps: Int = 11, // 0%, 10%, 20%, ..., 100%
    val instrumentName: String = "Dial Indicator / Micrometer (Grade 0)",
    val instrumentUncertaintyMm: Double = 0.003,
    val points: List<AxisCalibrationPoint> = emptyList(),
    val isCompleted: Boolean = false,
    val maxErrorMm: Double = 0.0,
    val meanErrorMm: Double = 0.0,
    val expandedUncertaintyMm: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class DocSectionItem(
    val id: String,
    val title: String,
    val category: String,
    val iconName: String,
    val summary: String,
    val detailedContent: String,
    val standardSteps: List<String> = emptyList(),
    val safetyTips: List<String> = emptyList()
)
