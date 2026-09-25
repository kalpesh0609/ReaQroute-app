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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.example.data.model.MapUiState
import com.example.data.model.Shelter
import com.example.domain.ShelterEngine
import com.example.ui.components.ResQMapPlaceholder
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed
import com.example.ui.theme.ResQSafeGreen

@Composable
fun SafeHavenDossierScreen(
    shelter: Shelter,
    mapUiState: MapUiState = MapUiState(),
    onBack: () -> Unit,
    onGetDirections: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isBookmarked by remember { mutableStateOf(false) }
    var hasConfirmedConditions by remember { mutableStateOf(false) }
    var isMapExpanded by remember { mutableStateOf(false) }
    var showExportModal by remember { mutableStateOf(false) }
    var selectedSanctuaryNotice by remember { mutableStateOf<String?>(null) }

    val isQuorumAlert = ShelterEngine.isApproachingQuorum(shelter.occupancyPct)

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
                IconButton(onClick = { showExportModal = true }) {
                    Icon(Icons.Default.FileDownload, "Export Census Log", tint = ResQBluePrimary)
                }
            }
        }

        // =========================================================================
        // PHASE 5: QUORUM ALERT BANNER (>80% CAPACITY)
        // =========================================================================
        if (isQuorumAlert) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ResQDangerContainer,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ResQDangerRed.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ResQDangerRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CRITICAL QUORUM ALERT (${shelter.occupancyPct}% FULL)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = ResQDangerRed
                            )
                        }
                        Text(
                            text = "Safe Haven is approaching capacity ceiling (only ${shelter.availableSpaces} beds remain). Distributed Quorum protocol active: non-critical evacuees are redirected to auxiliary volunteer sanctuaries below.",
                            fontSize = 11.sp,
                            color = Color(0xFF93000A),
                            lineHeight = 15.sp
                        )
                    }
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

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isQuorumAlert) ResQDangerContainer else ResQAmberContainer
                    ) {
                        Text(
                            text = "${shelter.occupancyPct}% Full",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isQuorumAlert) ResQDangerRed else Color(0xFF2A1700),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
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
                        Text("${shelter.availableSpaces} Spaces Available", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isQuorumAlert) ResQDangerRed else ResQAmberWarning)
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
                    color = if (isQuorumAlert) ResQDangerRed else ResQBluePrimary,
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
            }
        }

        // =========================================================================
        // PHASE 5: CRITICAL FACILITY AUDIT CHECKLIST (SPEC COMPLIANT)
        // =========================================================================
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
                        Text("Critical Facility Audit Checklist", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text("Mandatory disaster facility specifications", fontSize = 11.sp, color = Color(0xFF5F5E5E))
                    }
                    Surface(shape = RoundedCornerShape(100.dp), color = Color(0xFFDCFCE7)) {
                        Text("100% Certified", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }

                // 5 Core Audit Checklist Items
                val auditItems = listOf(
                    Triple("4,000L Potable Water Tanker", "100% Full • Inspected 35m ago", Icons.Default.WaterDrop),
                    Triple("100% Operational Diesel Generator", "72-Hour Fuel Reserve • Active Triage Line", Icons.Default.ElectricBolt),
                    Triple("Oxygen & First Aid Post", "Staffed with 2 Emergency Medical Doctors", Icons.Default.LocalHospital),
                    Triple("Wheelchair Step-Free Ramp Bedding", "${shelter.wheelchairBedsLeft} Reserved Step-Free Beds Available", Icons.Default.TaskAlt),
                    Triple("VHF Ham Radio Emergency Relay", "Frequency 145.500 MHz • Bandra Node Active", Icons.Default.Radio)
                )

                auditItems.forEach { (title, subtitle, icon) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ResQBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, null, tint = ResQBluePrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                                Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                            Icon(Icons.Default.CheckCircle, null, tint = ResQSafeGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // =========================================================================
        // PHASE 5: NEIGHBORHOOD VOLUNTEER SANCTUARY NETWORK (OVERFLOW MANAGEMENT)
        // =========================================================================
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
                                .background(ResQAmberContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddHome, null, tint = Color(0xFF704600), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Auxiliary Volunteer Sanctuaries", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("Verified Private Host Homes", fontSize = 10.sp, color = Color(0xFF5F5E5E))
                        }
                    }

                    Surface(shape = RoundedCornerShape(100.dp), color = ResQBlueContainer) {
                        Text("3 Homes Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                Text(
                    text = "When municipal shelters exceed 80% capacity, vetted auxiliary volunteer residences open doors to redirect ambulatory citizens.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 15.sp
                )

                // List of Volunteer Host Homes
                shelter.volunteerHomes.forEach { home ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(home.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                                    Text(home.distance, fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                                Surface(shape = RoundedCornerShape(100.dp), color = Color(0xFFDCFCE7)) {
                                    Text("${home.spots} spots open", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                home.features.forEach { feat ->
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEDE9FE)) {
                                        Text(feat, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5B21B6), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    selectedSanctuaryNotice = "Redirected to ${home.name} (${home.spots} spaces). SMS dispatch code sent."
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text("Redirect & Request Bed at Host", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // PHASE 5: REAL-TIME GROUND UPDATES BULLETIN LOG
        // =========================================================================
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
                    Column {
                        Text("Situational Ground Bulletins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text("Shelter coordinator field log", fontSize = 10.sp, color = Color(0xFF5F5E5E))
                    }
                    Icon(Icons.Default.Verified, null, tint = ResQBluePrimary, modifier = Modifier.size(16.dp))
                }

                if (shelter.groundUpdates.isEmpty()) {
                    Text("No bulletins posted yet.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                } else {
                    shelter.groundUpdates.forEach { update ->
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8FAFC), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(update.text, fontSize = 11.sp, color = Color(0xFF1A1C1C), modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(update.timeAgo, fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Community Trust Rating
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

    // Notice Dialog for Volunteer Sanctuary redirection
    selectedSanctuaryNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = { selectedSanctuaryNotice = null },
            title = { Text("Sanctuary Bed Confirmed", fontWeight = FontWeight.Bold) },
            text = { Text(notice, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = { selectedSanctuaryNotice = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Phase 5 EDXL-CAP XML & CSV Export Modal
    if (showExportModal) {
        val capXml = """<?xml version="1.0" encoding="UTF-8"?>
<alert xmlns="urn:oasis:names:tc:emergency:cap:1.2">
  <identifier>RESQ-SHELTER-CENSUS-${shelter.id}</identifier>
  <sender>mcgm-disaster-desk@mumbai.gov.in</sender>
  <sent>2026-09-24T17:15:00+05:30</sent>
  <status>Actual</status>
  <msgType>Alert</msgType>
  <scope>Public</scope>
  <info>
    <category>Safety</category>
    <event>Shelter Capacity Census</event>
    <urgency>Expected</urgency>
    <severity>Moderate</severity>
    <certainty>Observed</certainty>
    <headline>${shelter.name}: ${shelter.occupiedCount}/${shelter.totalCapacity} Occupied (${shelter.occupancyPct}%)</headline>
    <description>Beds available: ${shelter.availableSpaces}. Wheelchair ramps: ${shelter.wheelchairBedsLeft}. Inflow: +${shelter.arrivalRatePerMin}/min. Potable water tanker 100% full. Diesel generator 100% operational.</description>
    <area>
      <areaDesc>${shelter.sector}</areaDesc>
      <circle>19.0665,72.8365,1.0</circle>
    </area>
  </info>
</alert>""".trimIndent()

        val csvData = """facility_id,name,sector,occupied,total_capacity,available_spaces,occupancy_pct,arrival_rate_per_min,wheelchair_beds
${shelter.id},"${shelter.name}","${shelter.sector}",${shelter.occupiedCount},${shelter.totalCapacity},${shelter.availableSpaces},${shelter.occupancyPct},${shelter.arrivalRatePerMin},${shelter.wheelchairBedsLeft}
""".trimIndent()

        AlertDialog(
            onDismissRequest = { showExportModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, null, tint = ResQBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Census Data (EDXL-CAP / CSV)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Standard disaster coordination formats for inter-agency emergency operations:", fontSize = 11.sp, color = Color(0xFF64748B))
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0F172A), modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        Text(
                            text = capXml,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(capXml))
                        showExportModal = false
                    }) {
                        Text("Copy CAP XML", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Shelter Census - ${shelter.name}")
                                putExtra(Intent.EXTRA_TEXT, "$capXml\n\n--- CSV FORMAT ---\n$csvData")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Census Report"))
                            showExportModal = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary)
                    ) {
                        Text("Share", fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportModal = false }) {
                    Text("Close", fontSize = 11.sp)
                }
            }
        )
    }
}
