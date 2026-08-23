package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AxisCoord
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun MiniDroBar(
    axes: Map<String, AxisCoord>,
    currentCoordSystem: String,
    onZeroAxis: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = CncCardBg,
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CncCardBorder)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Coordinate System Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CncCyberCyan.copy(alpha = 0.15f))
                    .border(1.dp, CncCyberCyan, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = currentCoordSystem,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = CncCyberCyan,
                    fontFamily = FontFamily.Monospace
                )
            }

            // X, Y, Z, A Mini Coordinates
            listOf("X" to AxisXColor, "Y" to AxisYColor, "Z" to AxisZColor).forEach { (axisName, color) ->
                val axisData = axes[axisName]
                val pos = axisData?.workPos ?: 0.0

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onZeroAxis(axisName) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$axisName:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format(Locale.US, "%+07.3f", pos),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CncDroDigits
                    )
                }
            }
        }
    }
}
