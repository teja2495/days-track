package com.tk.daystrack.ui.theme

import androidx.compose.ui.graphics.Color

// New colors based on the HTML design
val Gray900 = Color(0xFF111827) // bg-gray-900
val Gray800 = Color(0xFF1F2937) // bg-gray-800
val Gray700 = Color(0xFF374151) // bg-gray-700
val Emerald500 = Color(0xFF10B981)
val Emerald600 = Color(0xFF059669)
val Emerald400 = Color(0xFF34D399)
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

// Primary theme colors
val PrimaryColor = Emerald500
val PrimaryLightColor = Emerald400
val PrimaryDarkColor = Emerald600

// Interactive elements
val FocusedBorderColor = Emerald400
val FocusedLabelColor = Emerald400
val CursorColor = Emerald400
val ButtonContainerColor = Emerald500
val ButtonDisabledColor = Emerald500.copy(alpha = 0.5f)

// Calendar and date picker colors
val CalendarSelectedColor = Emerald500
val CalendarTodayColor = Emerald400
val CalendarTodayBorderColor = Emerald400
val CalendarYearContentColor = Emerald400

// Text colors for theme elements
val ThemeTextColor = Emerald400

// Updated colors for the app
val BackgroundColor = Gray900
val CardColor = Gray800
val ButtonColor = Gray700
val EventDateColor = PrimaryLightColor
val FabColor = PrimaryColor
val FabHoverColor = PrimaryDarkColor

// Additional colors for specific use cases
val DeleteButtonColor = Color(0xFFF87171) // Red-400 for delete button
val DeleteButtonTextColor = Color.Black
val TextPrimary = White
val TextSecondary = White.copy(alpha = 0.7f)
val TextTertiary = White.copy(alpha = 0.3f)
val BorderColor = White.copy(alpha = 0.3f)