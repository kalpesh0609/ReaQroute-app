/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/theme/Type.kt
 *
 * PURPOSE & AIM:
 * Central typography system defining text hierarchies for crisis legibility.
 * Configures font weights, point sizes, line heights, and letter spacings
 * to ensure that vital safety numbers, distance meters, and emergency instructions
 * are immediately readable at arm's length in vibrating or outdoor movement conditions.
 *
 * LINKINGS & CONNECTIONS:
 * - Styles: [Typography] (Material 3 Typography configuration).
 * - Consumed By: [Theme.kt] in [ResQRouteTheme].
 */

package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Standard Material 3 Typography scale configured for ResQRoute disaster guidance.
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
