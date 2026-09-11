/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/theme/Theme.kt
 *
 * PURPOSE & AIM:
 * Central Material Design 3 theme orchestrator.
 * Binds Light and Dark color schemes, high-contrast typography, and surface elevations
 * to ensure legibility during rain storms, bright outdoor sunlight, and power-outage darkness.
 *
 * LINKINGS & CONNECTIONS:
 * - Theme Composable: [ResQRouteTheme].
 * - Color Schemes: [LightColorScheme], [DarkColorScheme].
 * - Typography: [Typography] from [Type.kt].
 * - Consumed By: [MainActivity.kt] wrapping the entire application tree.
 */

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Light theme color scheme mapping disaster design tokens to Material 3 semantic roles.
 */
private val LightColorScheme = lightColorScheme(
    primary = ResQBluePrimary,
    onPrimary = Color.White,
    primaryContainer = ResQBlueContainer,
    onPrimaryContainer = ResQOnBlueContainer,
    secondary = ResQAmberWarning,
    onSecondary = Color.White,
    secondaryContainer = ResQAmberContainer,
    onSecondaryContainer = ResQOnAmberContainer,
    tertiary = ResQSafeGreen,
    onTertiary = Color.White,
    tertiaryContainer = ResQSafeContainer,
    error = ResQDangerRed,
    onError = Color.White,
    errorContainer = ResQDangerContainer,
    onErrorContainer = ResQOnDangerContainer,
    background = ResQCanvasBackground,
    onBackground = ResQTextPrimary,
    surface = ResQSurfaceCard,
    onSurface = ResQTextPrimary,
    surfaceVariant = ResQSurfaceMuted,
    onSurfaceVariant = ResQTextSecondary,
    outline = ResQBorderSubtle
)

/**
 * Dark theme color scheme providing high-contrast tactical night visibility during power grid failures.
 */
private val DarkColorScheme = darkColorScheme(
    primary = ResQBlueContainer,
    onPrimary = ResQOnBlueContainer,
    primaryContainer = ResQBlueDark,
    onPrimaryContainer = ResQBlueContainer,
    secondary = ResQAmberAccent,
    onSecondary = ResQOnAmberContainer,
    error = Color(0xFFFFB4AB),
    background = Color(0xFF111315),
    surface = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E5),
    onSurface = Color(0xFFE2E2E5)
)

/**
 * Root theme composable wrapping all ResQRoute screens and dialogs.
 */
@Composable
fun ResQRouteTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
