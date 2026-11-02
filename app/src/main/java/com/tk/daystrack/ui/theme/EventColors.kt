package com.tk.daystrack.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
    return colorHex.toComposeColorOrNull() ?: Emerald400
}

// Accent used for text/icons on event cards; darkened in light mode to keep contrast
@Composable
fun eventTextAccentColor(colorHex: String?): Color {
    val accent = colorHex.toComposeColorOrNull() ?: return ThemeTextColor
    return if (isDarkScheme) accent else lerp(accent, Color.Black, 0.45f)
}

@Composable
fun eventCardBackgroundColor(colorHex: String?): Color {
    val base = MaterialTheme.colorScheme.surface
    val accent = colorHex.toComposeColorOrNull() ?: return base
    return lerp(base, accent, if (isDarkScheme) 0.22f else 0.16f)
}

@Composable
fun eventAddButtonBackgroundColor(colorHex: String?): Color {
    val base = if (isDarkScheme) ButtonColor else MaterialTheme.colorScheme.surfaceVariant
    val accent = colorHex.toComposeColorOrNull() ?: return base
    return lerp(base, accent, if (isDarkScheme) 0.7f else 0.45f)
}

private fun String?.toComposeColorOrNull(): Color? {
    if (this.isNullOrBlank()) return null
    return runCatching { Color(AndroidColor.parseColor(this.trim())) }.getOrNull()
}
