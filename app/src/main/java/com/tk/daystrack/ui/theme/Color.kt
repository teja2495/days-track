package com.tk.daystrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// New colors based on the HTML design
val Gray900 = Color(0xFF111827) // bg-gray-900
val Gray800 = Color(0xFF1F2937) // bg-gray-800
val Gray700 = Color(0xFF374151) // bg-gray-700
val Emerald500 = Color(0xFF10B981)
val Emerald600 = Color(0xFF059669)
val Emerald400 = Color(0xFF34D399)
val Emerald700 = Color(0xFF047857)
val White = Color(0xFFFFFFFF) // text-white

// Original colors kept for reference
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ===== SEMANTIC THEME COLORS =====
// These semantic names reference the emerald colors, making theme changes easier
// To change the entire theme color, just update the emerald color values above

// Theme-aware: bright emerald on dark surfaces, deeper emerald on light ones for contrast
val isDarkScheme: Boolean
    @Composable get() = MaterialTheme.colorScheme.background.luminance() < 0.5f

// Primary theme colors
val PrimaryColor: Color @Composable get() = if (isDarkScheme) Emerald500 else Emerald700
val PrimaryLightColor: Color @Composable get() = if (isDarkScheme) Emerald400 else Emerald700
val PrimaryDarkColor: Color @Composable get() = if (isDarkScheme) Emerald600 else Emerald700

// Interactive elements
val FocusedBorderColor: Color @Composable get() = PrimaryLightColor
val FocusedLabelColor: Color @Composable get() = PrimaryLightColor
val CursorColor: Color @Composable get() = PrimaryLightColor
val ButtonContainerColor: Color @Composable get() = PrimaryColor
val ButtonContentColor: Color @Composable get() = if (isDarkScheme) Color.Black else Color.White
val ButtonDisabledColor: Color @Composable get() = PrimaryColor.copy(alpha = 0.5f)

// Calendar and date picker colors
val CalendarSelectedColor: Color @Composable get() = PrimaryColor
val CalendarTodayColor: Color @Composable get() = PrimaryLightColor
val CalendarTodayBorderColor: Color @Composable get() = PrimaryLightColor
val CalendarYearContentColor: Color @Composable get() = PrimaryLightColor

// Text colors for theme elements
val ThemeTextColor: Color @Composable get() = PrimaryLightColor
val DateTextColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
val WarningTextColor: Color @Composable get() = if (isDarkScheme) Color(0xFFFBBF24) else Color(0xFFB45309)

// Updated colors for the app
val BackgroundColor = Gray900
val CardColor = Gray800
val ButtonColor = Gray700

// Additional colors for specific use cases
val DeleteButtonColor: Color @Composable get() = if (isDarkScheme) Color(0xFFF87171) else Color(0xFFDC2626)
val DeleteButtonTextColor: Color @Composable get() = if (isDarkScheme) Color.Black else Color.White
val TextPrimary = White
val TextSecondary = White.copy(alpha = 0.7f)
val TextTertiary = White.copy(alpha = 0.3f)
val BorderColor = White.copy(alpha = 0.3f)