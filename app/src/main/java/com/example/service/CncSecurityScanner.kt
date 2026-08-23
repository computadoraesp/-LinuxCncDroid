package com.example.service

import com.example.model.*
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.min

class CncSecurityScanner {

    fun scanGCode(fileName: String, content: String): SecurityScanResult {
        val startTime = System.currentTimeMillis()
        val threats = mutableListOf<SecurityThreat>()
        val lines = content.lines()
        val sizeBytes = content.toByteArray(Charsets.UTF_8).size.toLong()

        // 1. Calculate SHA-256 Cryptographic Hash
        val sha256 = calculateSha256(content)

        // 2. Binary & Disguised Executable Check
        if (content.startsWith("\u007FELF") || content.startsWith("MZ") || content.startsWith("PK\u0003\u0004")) {
            threats.add(
                SecurityThreat(
                    code = "SEC-CRIT-001",
                    title = "Disguised Binary Executable Detected",
                    description = "File contains ELF/PE/ZIP binary magic bytes disguised as G-Code. Execution blocked.",
                    lineNumber = 1,
                    lineContent = content.take(32),
                    severity = LogSeverity.CRITICAL
                )
            )
        }

        // 3. Shell Script Header Check
        if (content.startsWith("#!/") || content.contains("#!/bin/bash") || content.contains("#!/bin/sh")) {
            threats.add(
                SecurityThreat(
                    code = "SEC-CRIT-002",
                    title = "Embedded Shell Script Header",
                    description = "Detected Unix shebang (#!) in G-Code file. Potential arbitrary shell execution.",
                    lineNumber = 1,
                    lineContent = lines.firstOrNull() ?: "",
                    severity = LogSeverity.CRITICAL
                )
            )
        }

        var minX = 0f
        var maxX = 0f
        var minY = 0f
        var maxY = 0f
        var minZ = 0f
        var maxZ = 0f
        var hasMotion = false

        // 4. Line-by-Line AST & Security Heuristics Analysis
        lines.forEachIndexed { idx, rawLine ->
            val lineNum = idx + 1
            val line = rawLine.trim()

            // Buffer Overflow check (> 512 chars per block)
            if (line.length > 512) {
                threats.add(
                    SecurityThreat(
                        code = "SEC-WARN-003",
                        title = "Excessive G-Code Line Length (Buffer Overflow Risk)",
                        description = "Line exceeds 512 characters (${line.length} chars). May cause buffer overflow in legacy LinuxCNC parsers.",
                        lineNumber = lineNum,
                        lineContent = line.take(60) + "...",
                        severity = LogSeverity.WARNING
                    )
                )
            }

            // Path Traversal in O-Word Call Subroutines
            val oCallPattern = Regex("""O<([^>]+)>\s*CALL""", RegexOption.IGNORE_CASE)
            val oMatch = oCallPattern.find(line)
            if (oMatch != null) {
                val path = oMatch.groupValues[1]
                if (path.contains("..") || path.startsWith("/") || path.contains("etc/") || path.contains("root/")) {
                    threats.add(
                        SecurityThreat(
                            code = "SEC-CRIT-004",
                            title = "Path Traversal in O-Code Macro Subroutine",
                            description = "Subroutine call attempts relative path traversal: '$path'. Unauthorized file access risk.",
                            lineNumber = lineNum,
                            lineContent = line,
                            severity = LogSeverity.CRITICAL
                        )
                    )
                }
            }

            // Unvetted Custom User M-Codes (M100 - M199) that invoke OS bash scripts in LinuxCNC
            val mUserPattern = Regex("""\bM1[0-9]{2}\b""", RegexOption.IGNORE_CASE)
            val mMatch = mUserPattern.find(line)
            if (mMatch != null) {
                threats.add(
                    SecurityThreat(
                        code = "SEC-WARN-005",
                        title = "Unvetted External Script Execution (${mMatch.value})",
                        description = "M100-M199 executes custom bash scripts on the host Linux CNC controller. Verify script permissions.",
                        lineNumber = lineNum,
                        lineContent = line,
                        severity = LogSeverity.WARNING
                    )
                )
            }

            // Hidden Base64 / Hex Payload in comments
            if (line.contains("(") && line.contains(")")) {
                val commentContent = line.substringAfter("(").substringBefore(")")
                if (commentContent.length > 80 && !commentContent.contains(" ") && commentContent.matches(Regex("^[a-zA-Z0-9+/=]+$"))) {
                    threats.add(
                        SecurityThreat(
                            code = "SEC-WARN-006",
                            title = "Suspicious Encoded Payload in G-Code Comment",
                            description = "Detected high-entropy Base64/Hex blob embedded inside comment. Potential trojan steganography.",
                            lineNumber = lineNum,
                            lineContent = line.take(50) + "...",
                            severity = LogSeverity.WARNING
                        )
                    )
                }
            }

            // Track Coordinate Envelope
            val xVal = extractCoord(line, 'X')
            val yVal = extractCoord(line, 'Y')
            val zVal = extractCoord(line, 'Z')

            if (xVal != null || yVal != null || zVal != null) {
                hasMotion = true
                xVal?.let { minX = min(minX, it); maxX = max(maxX, it) }
                yVal?.let { minY = min(minY, it); maxY = max(maxY, it) }
                zVal?.let { minZ = min(minZ, it); maxZ = max(maxZ, it) }
            }

            // Extreme Z Plunge Hazard Check (< -250mm without safety prompt)
            if (zVal != null && zVal < -250f) {
                threats.add(
                    SecurityThreat(
                        code = "SEC-WARN-007",
                        title = "Extreme Negative Z Axis Plunge Hazard",
                        description = "Command requests Z $zVal mm. Verify table clearance to avoid mechanical machine collision.",
                        lineNumber = lineNum,
                        lineContent = line,
                        severity = LogSeverity.WARNING
                    )
                )
            }
        }

        val hasCritical = threats.any { it.severity == LogSeverity.CRITICAL }
        val hasWarning = threats.any { it.severity == LogSeverity.WARNING }

        val level = when {
            hasCritical -> ThreatLevel.MALWARE_BLOCKED
            hasWarning -> ThreatLevel.SUSPICIOUS
            else -> ThreatLevel.CLEAN
        }

        val duration = System.currentTimeMillis() - startTime

        return SecurityScanResult(
            fileName = fileName,
            fileSizeBytes = sizeBytes,
            totalLines = lines.size,
            threatLevel = level,
            threats = threats,
            isExecutable = !hasCritical,
            scanDurationMs = duration,
            sha256Fingerprint = sha256,
            boundingBoxX = Pair(minX, maxX),
            boundingBoxY = Pair(minY, maxY),
            boundingBoxZ = Pair(minZ, maxZ)
        )
    }

    private fun extractCoord(line: String, axis: Char): Float? {
        val pattern = Regex("""\b$axis\s*([-+]?[0-9]*\.?[0-9]+)""", RegexOption.IGNORE_CASE)
        val match = pattern.find(line) ?: return null
        return match.groupValues[1].toFloatOrNull()
    }

    private fun calculateSha256(input: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            "N/A"
        }
    }
}
