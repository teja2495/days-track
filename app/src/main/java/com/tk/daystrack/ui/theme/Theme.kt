package com.tk.daystrack.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    secondary = Emerald400,
    tertiary = Emerald500,
    background = Gray900,
    surface = Gray800,
    surfaceVariant = Gray800,
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = White,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald700,
    secondary = Emerald700,
    tertiary = Emerald600,
    background = Color(0xFFF0F7F3),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE3F0E9),
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF374151),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46)
)

@Composable
fun DayTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Set status bar color to match background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Update WindowInsets instead of directly setting statusBarColor
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // Use window insets controller to handle system bars
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun dayTrackBackgroundBrush(): Brush {
    // Use solid color instead of gradient to match the design
    val background = MaterialTheme.colorScheme.background
    return Brush.verticalGradient(
        colors = listOf(background, background)
    )
}