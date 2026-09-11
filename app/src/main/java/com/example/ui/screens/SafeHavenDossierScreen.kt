/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/SafeHavenDossierScreen.kt
 *
 * PURPOSE & AIM:
 * Comprehensive facility dossier and resource census for emergency safe havens.
 * Displays real-time carrying capacity gauges, remaining bed counts, quorum alerts,
 * verified amenities (generators, potable water, medical triage), live situational ground updates,
 * and auxiliary volunteer sanctuary spots within the neighborhood.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [SafeHavenDossierScreen].
 * - Consumed Models: [Shelter], [MapUiState].
 * - Embedded Components: [ResQMapPlaceholder] (shelter perimeter and approach map).
 * - Navigation Callbacks: [onBack], [onGetDirections].
 * - Invoked From: [MainActivity] when [ActiveScreen.ShelterDetail] or [MobileTab.SAFE_ZONES] is selected.
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
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.components.ResQMapPlaceholder
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQSafeGreen

@Composable
fun SafeHavenDossierScreen(
    shelter: Shelter,
    mapUiState: MapUiState = MapUiState(),
    onBack: () -> Unit,
    onGetDirections: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isBookmarked by remember { mutableStateOf(false) }
    var hasConfirmedConditions by remember { mutableStateOf(false) }
    var isMapExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
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

            Text("Safe Haven Dossier", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))

            Row {
                IconButton(onClick = { isBookmarked = !isBookmarked }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) ResQBluePrimary else Color(0xFF524436)
                    )
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Share, "Share", tint = Color(0xFF524436))
                }
            }
        }

        // 1. Shelter Summary Card
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
                    Surface(shape = RoundedCornerShape(100.dp), color = ResQBlueContainer) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ResQBluePrimary))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Updated just now", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF001B3B))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, null, tint = ResQBluePrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live Network", fontSize = 11.sp, color = Color(0xFF5F5E5E))
                    }
                }

                Text(shelter.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))
                Text(shelter.sector, fontSize = 12.sp, color = Color(0xFF524436))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp), color = ResQBluePrimary.copy(alpha = 0.1f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ResQBluePrimary))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open & Accepting Evacuees", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                        }
                    }

                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsWalk, null, tint = ResQBluePrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${shelter.walkTimeMins} min walk (Ridge)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF524436))
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, null, tint = ResQBluePrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verified Safe", fontSize = 11.sp, color = Color(0xFF5F5E5E))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terrain, null, tint = ResQAmberWarning, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("High Ground (Dry)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        }
                    }
                }
            }
        }

        // 1b. High-Ground Geographic Approach & OSRM Access Map
        Card(
            shape = RoundedCornerShape(20.dp),
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
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ResQBluePrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Map, null, tint = ResQBluePrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Safe Haven Access Map", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("OSRM Ridge Corridor approach", fontSize = 10.sp, color = Color(0xFF5F5E5E))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "Elev. +32m High Ground",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ResQMapPlaceholder(
                    uiState = mapUiState,
                    isExpanded = isMapExpanded,
                    onToggleExpand = { isMapExpanded = !isMapExpanded }
                )
            }
        }

        // 2. Capacity & Occupancy Visualization
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
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ResQAmberAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PieChart, null, tint = ResQAmberWarning, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Space Available", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("Live shelter head-count", fontSize = 11.sp, color = Color(0xFF5F5E5E))
                        }
                    }

                    Surface(shape = RoundedCornerShape(100.dp), color = ResQAmberContainer) {
                        Text("${shelter.occupancyPct}% Full", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A1700), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${shelter.occupiedCount}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))
                            Text(" / ${shelter.totalCapacity} People", fontSize = 14.sp, color = Color(0xFF5F5E5E), modifier = Modifier.padding(bottom = 3.dp))
                        }
                        Text("${shelter.availableSpaces} Spaces Available", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ResQAmberWarning)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Arrivals", fontSize = 11.sp, color = Color(0xFF5F5E5E))
                        Text("+${shelter.arrivalRatePerMin} people/min", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }
                }

                LinearProgressIndicator(
                    progress = { shelter.occupancyPct / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(100.dp)),
                    color = ResQBluePrimary,
                    trackColor = Color(0xFFE9E8E8)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Arriving", fontSize = 10.sp, color = Color(0xFF5F5E5E))
                            Text("+${shelter.arrivalRatePerMin} /min", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        }
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Time Left", fontSize = 10.sp, color = Color(0xFF5F5E5E))
                            Text("Full in ${shelter.expectedFullMins}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ResQAmberWarning)
                        }
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Accessible", fontSize = 10.sp, color = Color(0xFF5F5E5E))
                            Text("${shelter.wheelchairBedsLeft} Ramps", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                        }
                    }
                }

                // Approaching Quorum Warning
                Surface(shape = RoundedCornerShape(12.dp), color = ResQAmberContainer.copy(alpha = 0.4f)) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, null, tint = ResQAmberWarning, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Approaching Quorum: If this shelter fills up, you will be guided to Ridge Road Annex automatically.",
                            fontSize = 11.sp,
                            color = Color(0xFF653E00),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 3. Facilities Grid (12 Items)
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
                    Column {
                        Text("Available Facilities", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text("All services are free and open", fontSize = 11.sp, color = Color(0xFF5F5E5E))
                    }
                    Icon(Icons.Default.TaskAlt, null, tint = ResQBluePrimary)
                }

                val facilities = shelter.facilities
                for (i in facilities.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(facilities[i].name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C), maxLines = 1)
                                Text(facilities[i].statusText, fontSize = 10.sp, color = ResQBluePrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                        if (i + 1 < facilities.size) {
                            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(facilities[i + 1].name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C), maxLines = 1)
                                    Text(facilities[i + 1].statusText, fontSize = 10.sp, color = ResQBluePrimary, fontWeight = FontWeight.Medium)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 4. Community Trust & Volunteer Safe Spaces
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
                    Column {
                        Text("Visitor Trust Rating", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text("From families staying here", fontSize = 11.sp, color = Color(0xFF5F5E5E))
                    }
                    Surface(shape = RoundedCornerShape(100.dp), color = ResQBlueContainer) {
                        Text("${shelter.visitorPositivePct}% Positive", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3B), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }

                Button(
                    onClick = { hasConfirmedConditions = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasConfirmedConditions) ResQBlueContainer else Color(0xFFEFEEED)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        tint = if (hasConfirmedConditions) ResQBluePrimary else Color(0xFF1A1C1C),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasConfirmedConditions) "Feedback Sent • Thank You!" else "Confirm Shelter Conditions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasConfirmedConditions) ResQBluePrimary else Color(0xFF1A1C1C)
                    )
                }
            }
        }

        // Primary Get Directions Action Button
        Button(
            onClick = onGetDirections,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("get_directions_button")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Navigation, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Directions (${shelter.walkTimeMins} min walk)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
            }
        }
    }
}
