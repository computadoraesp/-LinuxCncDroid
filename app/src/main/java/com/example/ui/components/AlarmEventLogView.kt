package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CncEventLog
import com.example.model.LogSeverity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlarmEventLogView(
    logs: List<CncEventLog>,
    onClearLogs: () -> Unit,
    onSimulateAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }

    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "ERRORS" -> logs.filter { it.severity == LogSeverity.ERROR || it.severity == LogSeverity.CRITICAL }
            "SECURITY" -> logs.filter { it.severity == LogSeverity.SECURITY }
            "WARNINGS" -> logs.filter { it.severity == LogSeverity.WARNING }
            else -> logs
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CncCardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Logs", tint = CncWarningAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("INDUSTRIAL EVENT LOG & ALARM AUDIT", fontWeight = FontWeight.Black, fontSize = 12.sp, color = CncTextPrimary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onSimulateAlarm,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CncEstopRed.copy(alpha = 0.2f),
                            contentColor = CncEstopRed
                        ),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("TEST ALARM", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = onClearLogs,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CncSurfaceVariant,
                            contentColor = CncTextSecondary
                        ),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("CLEAR", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filter Chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("ALL", "ERRORS", "SECURITY", "WARNINGS").forEach { filterName ->
                    val isSelected = selectedFilter == filterName
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) CncCyberCyan else CncSurfaceVariant)
                            .clickable { selectedFilter = filterName }
                    ) {
                        Text(
                            filterName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF00363D) else CncTextSecondary
                        )
                    }
                }
            }

            // Event Logs Listing
            Surface(
                color = CncSurface,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                if (filteredLogs.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("No event logs recorded for filter '$selectedFilter'", fontSize = 11.sp, color = CncTextMuted)
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filteredLogs) { log ->
                            val (badgeColor, textColor) = when (log.severity) {
                                LogSeverity.CRITICAL -> Pair(CncEstopRed, CncEstopRed)
                                LogSeverity.ERROR -> Pair(CncEstopRed, CncEstopRed)
                                LogSeverity.SECURITY -> Pair(CncWarningAmber, CncWarningAmber)
                                LogSeverity.WARNING -> Pair(CncWarningAmber, CncTextPrimary)
                                LogSeverity.INFO -> Pair(CncCyberCyan, CncTextPrimary)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CncBackground.copy(alpha = 0.5f))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CncTextMuted
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(badgeColor.copy(alpha = 0.2f))
                                        .border(1.dp, badgeColor, RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = log.severity.displayName,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = badgeColor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "[${log.tag}] ${log.message}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
