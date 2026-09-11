/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/CitizenDossierScreen.kt
 *
 * PURPOSE & AIM:
 * Crowdsourced citizen incident intelligence and localized hazard audit report.
 * Provides verifiable evidence of flood levels (e.g. 48cm depth at Culvert Node #104),
 * neighbor consensus verification counters, municipal closure flags, recommended bypass corridors,
 * and direct civic action buttons (confirm hazard, report water level recession).
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [CitizenDossierScreen].
 * - Consumed Models: [HazardReport], [MapUiState].
 * - Embedded Components: [ResQMapPlaceholder] (hazard perimeter overlay).
 * - Navigation & Action Callbacks: [onBack], [onSelectDetour], [onConfirmFlooded], [onReportCleared].
 * - Invoked From: [MainActivity] when [ActiveScreen.CitizenDossier] or [MobileTab.DOSSIER] is selected.
 */

package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HazardReport
import com.example.data.model.MapUiState
import com.example.ui.components.ResQMapPlaceholder
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed

@Composable
fun CitizenDossierScreen(
    hazard: HazardReport,
    mapUiState: MapUiState = MapUiState(),
    onBack: () -> Unit,
    onSelectDetour: () -> Unit,
    onConfirmFlooded: () -> Unit,
    onReportCleared: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userConfirmed by remember { mutableStateOf(false) }
    var reportClearedSent by remember { mutableStateOf(false) }
    var isMapExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Breadcrumb Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F3F3))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = Color(0xFFEFEEED)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ResQAmberWarning))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Under Authority Review", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                }
            }
        }

        // Threat Header Dossier Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ResQDangerContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Flood, null, tint = ResQDangerRed, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("HAZARD REPORT ${hazard.reportNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ResQDangerRed)
                    }

                    Surface(shape = RoundedCornerShape(100.dp), color = ResQDangerContainer) {
                        Text("Active Flood", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF93000A), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                Text(hazard.title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = ResQBluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${hazard.locationName} • Reported ${hazard.reportedAgo}", fontSize = 12.sp, color = Color(0xFF524436))
                }
            }
        }

        // Community Verification Gauge Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ResQBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HowToReg, null, tint = Color(0xFF001B3B), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Community Verification", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }

                    Surface(shape = RoundedCornerShape(100.dp), color = ResQAmberContainer) {
                        Text("Verified", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF704600), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }

                // Gauge Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF4F3F3),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 12f
                                drawCircle(color = Color(0xFFE3E2E2), style = Stroke(width = stroke))
                                drawArc(
                                    color = ResQBluePrimary,
                                    startAngle = -90f,
                                    sweepAngle = (hazard.communityConfirmationPct / 100f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                            }
                            Text("${hazard.communityConfirmationPct}%", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text("${hazard.communityConfirmationPct}% Confirmed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text(
                                "Nearby neighbors and water sensors confirm this road is impassable.",
                                fontSize = 11.sp,
                                color = Color(0xFF524436),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Verification Stack Breakdown
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFAF9F9), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(ResQBlueContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Group, null, tint = ResQBluePrimary, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${hazard.neighborsConfirmed} Neighbors Confirmed", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Default.CheckCircle, null, tint = ResQBluePrimary, modifier = Modifier.size(16.dp))
                    }
                }

                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFAF9F9), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(ResQAmberContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Sensors, null, tint = ResQAmberWarning, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Water Sensor: ${hazard.waterDepthCm}cm deep (${hazard.culvertNode})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Default.CheckCircle, null, tint = ResQAmberWarning, modifier = Modifier.size(16.dp))
                    }
                }

                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFAF9F9), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(ResQDangerContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Emergency, null, tint = ResQDangerRed, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rescue Team Alerted", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = ResQDangerContainer) {
                            Text("Sent", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQDangerRed, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Ground Truth Photo Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, null, tint = Color(0xFF524436), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Photos from the Ground", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }
                    Text("Live on scene", fontSize = 11.sp, color = Color(0xFF5F5E5E))
                }

                // Vector representation of flood scene
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF334155)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Flood, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("CANAL ROAD FLOOD BASIN", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Water Depth: ${hazard.waterDepthCm}cm • Impassable", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    }

                    // Floating GPS tag
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Text(
                            text = "GEO: 19.0760° N, 72.8777° E",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Warning callout
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ResQDangerContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = ResQDangerRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Road is Underwater", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF93000A))
                            Text("Water is ${hazard.waterDepthCm}cm deep. Cars will stall. Do not attempt in low clearance vehicles.", fontSize = 11.sp, color = Color(0xFF93000A))
                        }
                    }
                }
            }
        }

        // Safe Detour Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(ResQBlueContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AltRoute, null, tint = ResQBluePrimary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Safe Detour Available", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                }

                Text("This road is blocked. Take Ridge Road instead (+6 min detour).", fontSize = 12.sp, color = Color(0xFF1A1C1C))

                // Detour GIS Map Preview with Hazard & Safe Route Overlays
                ResQMapPlaceholder(
                    uiState = mapUiState,
                    isExpanded = isMapExpanded,
                    onToggleExpand = { isMapExpanded = !isMapExpanded }
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF4F3F3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDetour() }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TurnRight, null, tint = ResQBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(hazard.detourName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        }
                        Surface(shape = RoundedCornerShape(100.dp), color = ResQBlueContainer) {
                            Text("${hazard.detourSafeUsersCount} guided", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    userConfirmed = true
                    onConfirmFlooded()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userConfirmed) ResQBlueContainer else ResQAmberAccent,
                    contentColor = if (userConfirmed) ResQBluePrimary else Color(0xFF2A1700)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_flooded_button")
            ) {
                Icon(if (userConfirmed) Icons.Default.Check else Icons.Default.ThumbUp, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (userConfirmed) "Confirmed: Road is Still Flooded (+1)" else "Confirm Road is Still Flooded",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    reportClearedSent = true
                    onReportCleared()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEEED)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(if (reportClearedSent) Icons.Default.Done else Icons.Default.CheckCircle, null, tint = Color(0xFF524436), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (reportClearedSent) "Clearance Notice Sent to Command Center" else "Report Water Has Cleared",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1C1C)
                )
            }
        }
    }
}
