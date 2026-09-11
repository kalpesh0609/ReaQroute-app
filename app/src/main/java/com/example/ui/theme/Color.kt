/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/theme/Color.kt
 *
 * PURPOSE & AIM:
 * Central color design tokens for the ResQRoute application.
 * Establishes accessible, high-contrast disaster palette conforming to Material Design 3 and DESIGN.md:
 * - High-visibility emergency blue primary and container shades.
 * - Calibrated flood warning ambers and alerts.
 * - High-ground safe green indicators for open safe havens.
 * - Uncompromising danger reds for impassable submerged road edges and hazard perimeters.
 *
 * LINKINGS & CONNECTIONS:
 * - Design Tokens: [ResQBluePrimary], [ResQSafeGreen], [ResQAmberWarning], [ResQDangerRed], etc.
 * - Consumed By: [Theme.kt], all screen composables, custom Canvas renderers, and vector overlays.
 */

package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Action & Branding Colors (ResQ Blue)
val ResQBluePrimary = Color(0xFF005EB2)
val ResQBlueDark = Color(0xFF002E5D)
val ResQBlueContainer = Color(0xFFD5E3FF)
val ResQOnBlueContainer = Color(0xFF001B3B)

// High-Ground Safe Haven & Shelter Colors (Safety Green)
val ResQSafeGreen = Color(0xFF059669)
val ResQSafeContainer = Color(0xFFD1FADF)

// Cautionary & Quorum Warning Colors (Amber Alert)
val ResQAmberWarning = Color(0xFF855400)
val ResQAmberAccent = Color(0xFFFFB248)
val ResQAmberContainer = Color(0xFFFFDDB7)
val ResQOnAmberContainer = Color(0xFF2A1700)

// Critical Flood & Impassable Hazard Colors (Danger Red)
val ResQDangerRed = Color(0xFFBA1A1A)
val ResQDangerContainer = Color(0xFFFFDAD6)
val ResQOnDangerContainer = Color(0xFF93000A)

// Surfaces & Canvas Neutral Backgrounds
val ResQCanvasBackground = Color(0xFFFAF9F9)
val ResQSurfaceCard = Color(0xFFFFFFFF)
val ResQSurfaceMuted = Color(0xFFF4F3F3)
val ResQSurfaceMutedAlt = Color(0xFFEFEEED)
val ResQBorderSubtle = Color(0xFFE9E8E8)

// Typography Text Shades
val ResQTextPrimary = Color(0xFF1A1C1C)
val ResQTextSecondary = Color(0xFF524436)
val ResQTextMuted = Color(0xFF5F5E5E)
