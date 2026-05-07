package com.tk.daystrack.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

data class EventColorPreset(
    val hex: String
)

val EventColorPresets = listOf(
    EventColorPreset("#60A5FA"), // sky blue
    EventColorPreset("#06B6D4"), // cyan
    EventColorPreset("#34D399"), // emerald
    EventColorPreset("#84CC16"), // lime
    EventColorPreset("#F59E0B"), // amber
    EventColorPreset("#F87171"), // coral
    EventColorPreset("#EC4899"), // rose
    EventColorPreset("#A78BFA"), // violet
    EventColorPreset("#6366F1"), // indigo
    EventColorPreset("#14B8A6"), // teal
    EventColorPreset("#EAB308"), // yellow
    EventColorPreset("#EF4444")  // red
)

fun eventAccentColor(colorHex: String?): Color {
    return colorHex.toComposeColorOrNull() ?: ThemeTextColor
}

fun eventCardBackgroundColor(colorHex: String?): Color {
    val accent = colorHex.toComposeColorOrNull() ?: return Gray800
    return lerp(Gray800, accent, 0.22f)
}

fun eventAddButtonBackgroundColor(colorHex: String?): Color {
    val accent = colorHex.toComposeColorOrNull() ?: return ButtonColor
    return lerp(ButtonColor, accent, 0.7f)
}

private fun String?.toComposeColorOrNull(): Color? {
    if (this.isNullOrBlank()) return null
    return runCatching { Color(AndroidColor.parseColor(this.trim())) }.getOrNull()
}
