package com.jagoalt.praisebreak.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Night = Color(0xFF0C1416)
private val Surface = Color(0xFF162022)
private val Tide = Color(0xFF8ED4CE)
private val Coral = Color(0xFFF0A08C)
private val Foam = Color(0xFFE8F3F2)
private val Mist = Color(0xFF8FA3A1)
private val Line = Color(0xFF2C4446)

private val Colors = darkColorScheme(
    primary = Tide,
    onPrimary = Color(0xFF042221),
    primaryContainer = Color(0xFF1C3A3C),
    onPrimaryContainer = Foam,
    secondary = Coral,
    onSecondary = Color(0xFF3A1610),
    background = Night,
    onBackground = Foam,
    surface = Surface,
    onSurface = Foam,
    surfaceVariant = Color(0xFF1E2C2E),
    onSurfaceVariant = Mist,
    outline = Line
)

private val Type = Typography(
    headlineMedium = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Medium, lineHeight = 36.sp),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun PraiseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Colors,
        typography = Type,
        content = content
    )
}
