/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/LiveGuidanceScreen.kt
 *
 * PURPOSE & AIM:
 * Full-screen active turn-by-turn evacuation navigation console.
 * Designed for extreme high-stress disaster conditions with high-contrast maneuver prompts,
 * remaining distance/ETA meters, step-by-step corridor landmarks, dual-mode GIS map & animated radar,
 * safety checkpoints, and instant QR check-in upon arriving at the shelter gate.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [LiveGuidanceScreen].
 * - Consumed Models: [Shelter], [MapUiState].
 * - Embedded Components: [ResQMapPlaceholder] (vector map tracking), [LiveGuidanceCanvas] (animated HUD).
 * - Navigation Callbacks: [onBack], [onEndRoute], [onShelterCheckIn].
 * - Invoked From: [MainActivity] when [ActiveScreen.LiveGuidance] is triggered.
 */

package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MapUiState
import com.example.data.model.Shelter
import com.example.ui.components.LiveGuidanceCanvas
import com.example.ui.components.ResQMapPlaceholder
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerRed
import com.example.ui.theme.ResQSafeGreen

@Composable
fun LiveGuidanceScreen(
    shelter: Shelter,
    mapUiState: MapUiState = MapUiState(),
    onBack: () -> Unit,
    onEndRoute: () -> Unit,
    onShelterCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var detourAccepted by remember { mutableStateOf(false) }
    var checkedIn by remember { mutableStateOf(false) }
    var showGisVectorMap by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Map Canvas Layer (Toggleable between Live Guidance Canvas and OSRM GIS Map Placeholder)
        if (showGisVectorMap) {
            ResQMapPlaceholder(
                uiState = mapUiState,
                isExpanded = true,
                onToggleExpand = { showGisVectorMap = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LiveGuidanceCanvas()
        }

        // Top Navigation Header
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 4.dp
        ) {
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
                        Text(
                            text = "RESQROUTE LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ResQBluePrimary
                        )
                        Text(
                            text = "Live Guidance",
                            fontSize = 16.sp,
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

                    IconButton(onClick = { isMuted = !isMuted }) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Audio toggle",
                            tint = Color(0xFF524436)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ResQAmberWarning),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("D", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Floating Instructions & Prompts
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp, start = 14.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Turn-by-Turn Instruction Banner in Warm Amber
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ResQAmberAccent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TurnLeft,
                            contentDescription = null,
                            tint = Color(0xFF2A1700),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "IN 200 M",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2A1700)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Elevated Safe Path",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2A1700),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Turn Left onto Ridge Crest Way",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2A1700)
                        )
                        Text(
                            text = "High-ground route clear of rising water",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF653E00)
                        )
                    }
                }
            }

            // Dynamic Reroute Alert Prompt
            if (!detourAccepted) {
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
                            Text("Flooding Ahead on Canal Rd", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(shape = RoundedCornerShape(100.dp), color = ResQAmberContainer.copy(alpha = 0.6f)) {
                                Text("+3 min", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQAmberWarning, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Button(
                            onClick = { detourAccepted = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Detour", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Floating Flooding Warning Chip
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
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ResQBluePrimary))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ridge Way • Safe Path", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ResQDangerRed,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Flood, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("FLOODING AHEAD", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("Canal Rd Submerged", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Mid-screen Floating Speedometer & Action FABs
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
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
                        Text("38", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text("KM/H", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF524436))
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE9E8E8)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("SAFE GROUND", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                        Text("NNE 24°", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(
                    onClick = { },
                    containerColor = Color.White,
                    contentColor = ResQBluePrimary,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Default.MyLocation, "My Location", modifier = Modifier.size(20.dp))
                }

                FloatingActionButton(
                    onClick = { },
                    containerColor = ResQDangerRed,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Default.Warning, "Report Hazard", modifier = Modifier.size(20.dp))
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("9", fontSize = 32.sp, fontWeight = FontWeight.Black, color = ResQBluePrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("MIN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("2.6 km", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text(" • ", fontSize = 13.sp, color = Color(0xFF524436))
                        Text("ETA 09:50", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }

                    Surface(shape = RoundedCornerShape(100.dp), color = ResQBlueContainer) {
                        Text("High Ground", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3B), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
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
                                    .size(32.dp)
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

                // Actions Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onEndRoute,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9E8E8)),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = ResQDangerRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("End Route", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }

                    Button(
                        onClick = {
                            checkedIn = true
                            onShelterCheckIn()
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
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
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
    }
}
