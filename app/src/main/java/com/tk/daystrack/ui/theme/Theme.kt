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
    primary = White,
    secondary = White,
    tertiary = Teal500,
    background = Gray900,
    surface = Gray900,
    surfaceVariant = Gray800,
    onSurfaceVariant = White,
    onBackground = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White
)

private val LightColorScheme = darkColorScheme(  // Force dark theme to match design
    primary = White,
    secondary = White,
    tertiary = Teal500,
    background = Gray900,
    surface = Gray900,
    surfaceVariant = Gray800,
    onSurfaceVariant = White,
    onBackground = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White
)

@Composable
fun DayTrackTheme(
    darkTheme: Boolean = true, // Force dark theme to match design
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to ensure consistent design
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
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
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
    return Brush.verticalGradient(
        colors = listOf(Gray900, Gray900)
    )
}