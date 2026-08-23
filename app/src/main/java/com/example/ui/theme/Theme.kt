package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IndustrialDarkColorScheme = darkColorScheme(
    primary = CncCyberCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = CncActiveGreen,
    onSecondary = Color(0xFF003919),
    secondaryContainer = Color(0xFF005327),
    onSecondaryContainer = Color(0xFF73FA9D),
    tertiary = CncWarningAmber,
    onTertiary = Color(0xFF412D00),
    tertiaryContainer = Color(0xFF5E4200),
    onTertiaryContainer = Color(0xFFFFDEA2),
    error = CncEstopRed,
    onError = Color(0xFF690013),
    errorContainer = Color(0xFF930020),
    onErrorContainer = Color(0xFFFFDAD9),
    background = CncBackground,
    onBackground = CncTextPrimary,
    surface = CncSurface,
    onSurface = CncTextPrimary,
    surfaceVariant = CncSurfaceVariant,
    onSurfaceVariant = CncTextSecondary,
    outline = CncCardBorder
)

@Composable
fun LinuxCncTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IndustrialDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backwards-compat alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LinuxCncTheme(content = content)
}
