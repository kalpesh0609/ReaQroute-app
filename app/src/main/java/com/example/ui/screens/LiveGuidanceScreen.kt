/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/LiveGuidanceScreen.kt
 *
 * PURPOSE & AIM:
 * Full-screen active turn-by-turn evacuation navigation console.
 * Designed for extreme high-stress disaster conditions with high-contrast maneuver prompts,
 * real-time OSRM step progression, remaining distance/ETA meters, automated hardware TTS voice
 * callouts, tactile haptic vibration pulses, and instant QR check-in upon arriving at the shelter gate.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [LiveGuidanceScreen].
 * - Consumed Models: [Shelter], [MapUiState], [OsrmStepData].
 * - Sensory Services: [GuidanceFeedbackHelper] (TTS + Haptics + Compass Sensor).
 * - Embedded Components: [ResQMapPlaceholder] (vector map tracking), [LiveGuidanceCanvas] (animated HUD).
 * - Navigation Callbacks: [onBack], [onEndRoute], [onShelterCheckIn].
 * - Invoked From: [MainActivity] when [ActiveScreen.LiveGuidance] is triggered.
 */

package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.TurnSlightLeft
import androidx.compose.material.icons.filled.TurnSlightRight
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.MapUiState
import com.example.data.model.OsrmStepData
import com.example.data.model.Shelter
import com.example.service.GuidanceFeedbackHelper
import com.example.ui.components.LiveGuidanceCanvas
import com.example.ui.components.ResQMapPlaceholder
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerRed
import com.example.ui.theme.ResQSafeGreen
import kotlinx.coroutines.delay

@Composable
fun LiveGuidanceScreen(
    shelter: Shelter,
    mapUiState: MapUiState = MapUiState(),
    onBack: () -> Unit,
    onEndRoute: () -> Unit,
    onShelterCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Hardware Sensory Helper (TTS + Haptics + Compass Sensor)
    val feedbackHelper = remember { GuidanceFeedbackHelper(context) }
    DisposableEffect(Unit) {
        feedbackHelper.startCompass()
        onDispose {
            feedbackHelper.destroy()
        }
    }

    // Dynamic Steps from OSRM Active Route or verified Ridge Safe Route
    val activeSteps: List<OsrmStepData> = remember(mapUiState.activeRoute) {
        mapUiState.activeRoute?.steps?.takeIf { it.isNotEmpty() } ?: listOf(
            OsrmStepData("Hill Road", "Depart northeast on Hill Road away from low basin", 220.0, 140.0, "depart"),
            OsrmStepData("St. Anne High Street", "Turn slight left onto St. Anne High Street", 350.0, 180.0, "turn"),
            OsrmStepData("Ridge Ascent Road", "Ascend along Ridge Road following high ground signs", 800.0, 320.0, "turn"),
            OsrmStepData("Ridge Spine", "Continue along dry paved spine (+32m elevation)", 900.0, 360.0, "continue"),
            OsrmStepData("Sanctuary Gate", "Arrive at St. Jude Safe Haven on right", 330.0, 120.0, "arrive")
        )
    }

    // Navigation State
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSimulatingWalk by remember { mutableStateOf(false) }
    var detourAccepted by remember { mutableStateOf(false) }
    var showQrModal by remember { mutableStateOf(false) }
    var checkedIn by remember { mutableStateOf(false) }
    var showGisVectorMap by remember { mutableStateOf(true) }
    var compassHeading by remember { mutableFloatStateOf(24f) }
    var simulatedSpeedKmH by remember { mutableIntStateOf(18) }

    // Register compass listener
    LaunchedEffect(feedbackHelper) {
        feedbackHelper.onHeadingChanged = { heading ->
            compassHeading = heading
        }
    }

    // Permission Launcher for GPS Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            feedbackHelper.vibrateTurnUpcoming()
        }
    }

    // Check location permission on start
    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Current Step and Progress Calculations
    val safeStepIndex = currentStepIndex.coerceIn(0, activeSteps.size - 1)
    val currentStep = activeSteps[safeStepIndex]
    val nextStep = if (safeStepIndex + 1 < activeSteps.size) activeSteps[safeStepIndex + 1] else null
    val progressFraction = safeStepIndex.toFloat() / (activeSteps.size - 1).coerceAtLeast(1).toFloat()

    // Remaining Distance and ETA
    val remainingDistanceM = remember(safeStepIndex, activeSteps) {
        activeSteps.drop(safeStepIndex).sumOf { it.distanceMeters }
    }
    val remainingSeconds = remember(safeStepIndex, activeSteps) {
        activeSteps.drop(safeStepIndex).sumOf { it.durationSeconds }
    }
    val remainingMinutes = (remainingSeconds / 60.0).toInt().coerceAtLeast(1)

    // Trigger TTS and Haptics when step changes
    LaunchedEffect(safeStepIndex) {
        if (!isMuted) {
            feedbackHelper.speak(currentStep.instruction)
        }
        if (safeStepIndex == activeSteps.size - 1) {
            feedbackHelper.vibrateArrivalCelebration()
            showQrModal = true
        } else {
            feedbackHelper.vibrateImmediateManeuver()
        }
    }

    // Simulation Loop: Auto-advances through the route if user enables simulation
    LaunchedEffect(isSimulatingWalk, currentStepIndex) {
        if (isSimulatingWalk && currentStepIndex < activeSteps.size - 1) {
            delay(3500)
            currentStepIndex++
            simulatedSpeedKmH = (14..24).random()
        } else if (isSimulatingWalk && currentStepIndex >= activeSteps.size - 1) {
            isSimulatingWalk = false
        }
    }

    val currentWaypoint = remember(safeStepIndex, mapUiState.activeRoute) {
        val pts = mapUiState.activeRoute?.geometryPoints
        if (!pts.isNullOrEmpty()) {
            val targetIdx = (safeStepIndex.toFloat() / (activeSteps.size - 1).coerceAtLeast(1) * (pts.size - 1)).toInt()
            pts.getOrNull(targetIdx) ?: pts.first()
        } else {
            mapUiState.userLocation
        }
    }
    val effectiveMapUiState = remember(mapUiState, isSimulatingWalk, currentWaypoint) {
        if (isSimulatingWalk) mapUiState.copy(userLocation = currentWaypoint) else mapUiState
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Map Canvas Layer (Toggleable between Real OpenStreetMap and Tactical Guidance Canvas)
        if (showGisVectorMap) {
            ResQMapPlaceholder(
                uiState = effectiveMapUiState,
                isExpanded = true,
                onToggleExpand = { showGisVectorMap = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LiveGuidanceCanvas(
                progressFraction = progressFraction,
                headingDegrees = compassHeading,
                hazardNearby = !detourAccepted,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Navigation Header
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            color = Color.White.copy(alpha = 0.94f),
            shadowElevation = 4.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ResQBluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "RESQROUTE LIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ResQBluePrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSimulatingWalk) ResQAmberAccent else ResQSafeGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (isSimulatingWalk) "SIMULATING WALK" else "LIVE GPS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSimulatingWalk) Color(0xFF2A1700) else ResQSafeGreen,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (mapUiState.isOnline) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = if (mapUiState.isOnline) "ONLINE" else "OFFLINE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (mapUiState.isOnline) Color(0xFF15803D) else Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Step ${safeStepIndex + 1} of ${activeSteps.size}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1C)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // GIS Map Layer View Switcher
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (showGisVectorMap) ResQBluePrimary else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showGisVectorMap = !showGisVectorMap }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Toggle GIS Map",
                                    tint = if (showGisVectorMap) Color.White else Color(0xFF334155),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showGisVectorMap) "GIS Active" else "OSRM Map",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showGisVectorMap) Color.White else Color(0xFF334155)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Audio Voice Prompt Toggle
                        IconButton(onClick = {
                            isMuted = !isMuted
                            if (!isMuted) feedbackHelper.speak("Voice guidance enabled. ${currentStep.instruction}")
                        }) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute guidance" else "Mute guidance",
                                tint = if (isMuted) Color.Gray else ResQBluePrimary
                            )
                        }
                    }
                }

                // Corridor Progress Indicator Bar
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = ResQBluePrimary,
                    trackColor = Color(0xFFE2E8F0)
                )
            }
        }

        // Floating Instructions & Tactical Maneuver Cards
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp, start = 14.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // High-Contrast Turn-by-Turn Maneuver Card in Warm Amber
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ResQAmberAccent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("maneuver_instruction_card")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getManeuverIcon(currentStep),
                            contentDescription = currentStep.instruction,
                            tint = Color(0xFF2A1700),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "IN ${(currentStep.distanceMeters).toInt()} M",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2A1700)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (safeStepIndex >= activeSteps.size - 1) "Safe Haven Arrival" else "Elevated Safe Path",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2A1700),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = currentStep.instruction,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2A1700)
                        )

                        nextStep?.let { next ->
                            Text(
                                text = "Then: ${next.instruction}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF653E00)
                            )
                        }
                    }
                }
            }

            // Dynamic Reroute Alert Prompt
            AnimatedVisibility(
                visible = !detourAccepted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ResQAmberAccent.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = ResQAmberWarning, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Flooding on Canal Rd", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(shape = RoundedCornerShape(100.dp), color = ResQAmberContainer.copy(alpha = 0.6f)) {
                                Text("+3 min detour", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQAmberWarning, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Button(
                            onClick = {
                                detourAccepted = true
                                feedbackHelper.vibrateHazardWarning()
                                if (!isMuted) feedbackHelper.speak("Detour confirmed. Staying on high ground Ridge Way.")
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Detour", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Floating Safe Spine & Water Status Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ResQSafeGreen))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentStep.name} • Safe +32m",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (detourAccepted) ResQSafeGreen else ResQDangerRed,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (detourAccepted) Icons.Default.CheckCircle else Icons.Default.Flood,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (detourAccepted) "DETOUR ACTIVE" else "FLOODING AHEAD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = if (detourAccepted) "Canal Rd Bypassed" else "Canal Rd Submerged",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Mid-screen Floating Telemetry & Simulator Controls
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Speedometer and Compass HUD
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "$simulatedSpeedKmH",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1C)
                        )
                        Text("KM/H", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF524436))
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE9E8E8)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CompassCalibration, null, tint = ResQBluePrimary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("BEARING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                        }
                        Text("${compassHeading.toInt()}° NNE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }
                }
            }

            // Tactical FAB Controls: Step Navigator & Simulator Toggle
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Auto Walk Simulator Play/Pause Button
                FloatingActionButton(
                    onClick = {
                        isSimulatingWalk = !isSimulatingWalk
                        if (isSimulatingWalk && currentStepIndex >= activeSteps.size - 1) {
                            currentStepIndex = 0
                        }
                    },
                    containerColor = if (isSimulatingWalk) ResQAmberAccent else Color.White,
                    contentColor = ResQBluePrimary,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("simulate_walk_fab")
                ) {
                    Icon(
                        imageVector = if (isSimulatingWalk) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Simulate Walk",
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Step Forward Manual Button
                FloatingActionButton(
                    onClick = {
                        if (currentStepIndex < activeSteps.size - 1) {
                            currentStepIndex++
                        } else {
                            showQrModal = true
                        }
                    },
                    containerColor = Color.White,
                    contentColor = ResQBluePrimary,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("next_step_fab")
                ) {
                    Icon(
                        imageVector = if (currentStepIndex >= activeSteps.size - 1) Icons.Default.QrCode else Icons.Default.FastForward,
                        contentDescription = "Next Maneuver",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Hazard Warning FAB
                FloatingActionButton(
                    onClick = {
                        feedbackHelper.vibrateHazardWarning()
                    },
                    containerColor = ResQDangerRed,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Warning, "Report Obstacle", modifier = Modifier.size(20.dp))
                }
            }
        }

        // Bottom Sticky Navigation Panel
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Trip ETA & Remaining Distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$remainingMinutes",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = ResQBluePrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("MIN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val distanceKm = String.format("%.1f", remainingDistanceM / 1000.0)
                        Text("$distanceKm km left", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text(" • ", fontSize = 13.sp, color = Color(0xFF524436))
                        Text("ETA +$remainingMinutes m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }

                    Surface(shape = RoundedCornerShape(100.dp), color = ResQBlueContainer) {
                        Text(
                            text = if (safeStepIndex >= activeSteps.size - 1) "At Sanctuary" else "High Ground",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001B3B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Safe Haven Destination Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF4F3F3),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ResQAmberContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NightShelter, null, tint = Color(0xFF2A1700), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("SAFE HAVEN DESTINATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ResQAmberWarning)
                                Text("${shelter.name} • Gate 2", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            }
                        }
                        Text("${shelter.availableSpaces} beds ready", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ResQAmberWarning)
                    }
                }

                // Actions Grid: End Route and Shelter Check-In
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onEndRoute,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9E8E8)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = ResQDangerRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("End Route", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }

                    Button(
                        onClick = {
                            showQrModal = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (checkedIn) ResQSafeGreen else ResQAmberWarning
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("shelter_checkin_button")
                    ) {
                        Icon(
                            imageVector = if (checkedIn) Icons.Default.CheckCircle else Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (checkedIn) "Checked In ✓" else "Shelter Check-In",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Shelter QR Admission Verification Dialog
        if (showQrModal) {
            AlertDialog(
                onDismissRequest = { showQrModal = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NightShelter, null, tint = ResQBluePrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Safe Haven Gate Check-In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${shelter.name} • Gate 2 Arrival",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ResQBluePrimary
                        )
                        Text(
                            text = "Present this admission token to the shelter registrar",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Stylized QR Code Box
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "Admission QR Code",
                                    tint = Color.White,
                                    modifier = Modifier.size(90.dp)
                                )
                                Text(
                                    text = "SEC17-STJ-4481",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ResQAmberAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ResQBlueContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Assigned Section: Sector 17 High-Ground Ward", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Capacity: ${shelter.availableSpaces} of ${shelter.totalCapacity} beds available", fontSize = 10.sp, color = Color(0xFF334155))
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            checkedIn = true
                            showQrModal = false
                            onShelterCheckIn()
                            feedbackHelper.vibrateArrivalCelebration()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ResQSafeGreen),
                        modifier = Modifier.testTag("confirm_admission_button")
                    ) {
                        Text("Confirm Admission ✓", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showQrModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Dismiss", color = Color.Black)
                    }
                }
            )
        }
    }
}

/**
 * Maps OSRM step maneuver strings to high-visibility icons.
 */
private fun getManeuverIcon(step: OsrmStepData): ImageVector {
    val type = step.maneuverType.lowercase()
    val instr = step.instruction.lowercase()
    return when {
        type == "arrive" || instr.contains("arrive") -> Icons.Default.NightShelter
        type == "depart" || instr.contains("depart") -> Icons.AutoMirrored.Filled.ArrowForward
        instr.contains("slight left") -> Icons.Default.TurnSlightLeft
        instr.contains("slight right") -> Icons.Default.TurnSlightRight
        instr.contains("left") -> Icons.Default.TurnLeft
        instr.contains("right") -> Icons.Default.TurnRight
        instr.contains("continue") || instr.contains("straight") -> Icons.Default.Straight
        else -> Icons.Default.TurnLeft
    }
}
