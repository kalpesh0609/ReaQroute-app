/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/RouteComparisonScreen.kt
 *
 * PURPOSE & AIM:
 * Corridor comparison screen delivering visual and hydrologic proof of ADR-004:
 * "Shortest Route ≠ Safest Route".
 * Juxtaposes the direct but submerged low-basin route against the recommended elevated ridge spine.
 * Offers a dual-mode visualizer switching between an interactive OSRM GIS vector map and
 * a cross-sectional elevation profile showing water depth inundation.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [RouteComparisonScreen].
 * - Consumed Models: [RoutingResult], [Shelter], [MapUiState].
 * - Embedded Components: [ResQMapPlaceholder] (GIS map mode), [ElevationProfileCanvas] (terrain profile mode).
 * - Navigation Callbacks: [onBack], [onConfirmSafestRoute].
 * - Invoked From: [MainActivity] when [ActiveScreen.RouteComparison] is selected.
 */

package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MapUiState
import com.example.data.model.Shelter
import com.example.domain.RoutingResult
import com.example.ui.components.ElevationProfileCanvas
import com.example.ui.components.ResQMapPlaceholder
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RouteComparisonScreen(
    routingResult: RoutingResult,
    shelter: Shelter,
    mapUiState: MapUiState = MapUiState(),
    onBack: () -> Unit,
    onConfirmSafestRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRouteId by remember { mutableStateOf("route-b-safe") }
    var isCalibrating by remember { mutableStateOf(false) }
    var comparisonViewMode by remember { mutableStateOf(0) } // 0: GIS Map & OSRM, 1: Elevation Profile
    var isMapExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val safeRoute = routingResult.alternativeRoutes.find { it.isSafe } ?: routingResult.selectedRoute
    val unsafeRoute = routingResult.alternativeRoutes.find { it.isFlooded } ?: routingResult.alternativeRoutes.last()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Navigation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F3F3))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1A1C1C)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "ROUTE COMPARISON",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = ResQBluePrimary
                )
                Text(
                    text = "Why this route?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1C)
                )
            }
        }

        // Reassuring Clarity Callout
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ResQBlueContainer.copy(alpha = 0.35f),
            border = androidx.compose.foundation.BorderStroke(1.dp, ResQBlueContainer.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ResQBluePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "We picked the safest dry path to St. Jude Shelter. The shorter shortcut is dangerously flooded.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1C1C),
                    lineHeight = 18.sp
                )
            }
        }

        // Visual Side-by-Side Dual View: OSRM Vector Micro-GIS Map & Elevation Profile
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, null, tint = ResQBluePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Visual Route Comparison", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }
                    
                    // Dual Toggle Pills
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (comparisonViewMode == 0) ResQBluePrimary else Color.Transparent,
                            modifier = Modifier.clickable { comparisonViewMode = 0 }
                        ) {
                            Text(
                                text = "GIS Map",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (comparisonViewMode == 0) Color.White else Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (comparisonViewMode == 1) ResQBluePrimary else Color.Transparent,
                            modifier = Modifier.clickable { comparisonViewMode = 1 }
                        ) {
                            Text(
                                text = "Elevation",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (comparisonViewMode == 1) Color.White else Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (comparisonViewMode == 0) {
                    ResQMapPlaceholder(
                        uiState = mapUiState,
                        isExpanded = isMapExpanded,
                        onToggleExpand = { isMapExpanded = !isMapExpanded }
                    )
                } else {
                    ElevationProfileCanvas()
                }
            }
        }

        // Card 1: Safe Route (Selected)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(
                if (selectedRouteId == "route-b-safe") 2.dp else 1.dp,
                if (selectedRouteId == "route-b-safe") ResQBluePrimary else Color(0xFFE9E8E8)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedRouteId = "route-b-safe" }
                .testTag("safe_route_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ResQBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(safeRoute.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(100.dp), color = ResQBlueContainer) {
                                    Text("RECOMMENDED", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF001B3B), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text(safeRoute.corridorName, fontSize = 12.sp, color = Color(0xFF524436))
                        }
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = ResQBluePrimary.copy(alpha = 0.1f)) {
                        Text("${safeRoute.travelTimeMins} min", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Metrics Pill Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Travel Time", fontSize = 10.sp, color = Color(0xFF524436))
                            Text("${safeRoute.travelTimeMins} min", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                        }
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Distance", fontSize = 10.sp, color = Color(0xFF524436))
                            Text("${safeRoute.distanceKm} km", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        }
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ground Status", fontSize = 10.sp, color = Color(0xFF524436))
                            Text("High Ground", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, null, tint = ResQBluePrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(safeRoute.description, fontSize = 12.sp, color = Color(0xFF524436), lineHeight = 16.sp)
                }
            }
        }

        // Card 2: Unsafe Shortcut (Avoid)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F9)),
            border = androidx.compose.foundation.BorderStroke(
                if (selectedRouteId == "route-a-unsafe") 2.dp else 1.dp,
                if (selectedRouteId == "route-a-unsafe") ResQDangerRed else ResQDangerContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedRouteId = "route-a-unsafe" }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ResQDangerContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, null, tint = ResQDangerRed, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(unsafeRoute.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(100.dp), color = ResQDangerContainer) {
                                    Text("HAZARD", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = ResQDangerRed, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text(unsafeRoute.corridorName, fontSize = 12.sp, color = Color(0xFF524436))
                        }
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = ResQDangerContainer) {
                        Text(
                            text = "${unsafeRoute.travelTimeMins} min",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ResQDangerRed,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Cancel, null, tint = ResQDangerRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Road is completely underwater (48cm deep). Cars and walking are impassable due to deep canal water and drain collapse.",
                        fontSize = 12.sp,
                        color = Color(0xFF524436),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Destination Confirmation Badge
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ResQBluePrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalHospital, null, tint = ResQBluePrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Destination: ${shelter.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text("${shelter.availableSpaces} open beds • Generator backup", fontSize = 11.sp, color = Color(0xFF524436))
                    }
                }
                Text("Safe Zone", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
            }
        }

        // Primary Follow Route CTA
        Button(
            onClick = {
                isCalibrating = true
                scope.launch {
                    delay(800)
                    isCalibrating = false
                    onConfirmSafestRoute()
                }
            },
            enabled = !isCalibrating,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("follow_safest_route_button")
        ) {
            if (isCalibrating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calibrating High-Ground GPS...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Follow Safest Route (${safeRoute.travelTimeMins} min)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
        }
        Text(
            text = "Real-time alerts will reroute you if water rises unexpectedly.",
            fontSize = 11.sp,
            color = Color(0xFF524436),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
