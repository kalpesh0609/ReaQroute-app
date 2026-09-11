/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/AuthorityConsoleScreen.kt
 *
 * PURPOSE & AIM:
 * Municipal and emergency coordinator management console for critical infrastructure governance.
 * Allows disaster response officers to review sensor anomalies, issue formal hard road closures,
 * and authorize neighborhood-wide detour broadcasts across all citizen handsets.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [AuthorityConsoleScreen].
 * - Consumed Models: [HazardReport].
 * - Action Callbacks: [onBack], [onApproveDetour].
 * - Invoked From: [MainActivity] when [ActiveScreen.AuthorityConsole] is triggered.
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.BroadcastOnPersonal
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HazardReport
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed
import com.example.ui.theme.ResQSafeGreen

@Composable
fun AuthorityConsoleScreen(
    hazard: HazardReport,
    onBack: () -> Unit,
    onApproveDetour: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showApproveConfirmDialog by remember { mutableStateOf(false) }
    var detourApprovedByOfficer by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Authority Header
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("INCIDENT MODERATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQAmberWarning)
                Text("Authority Command Desk", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))
            }

            Surface(shape = RoundedCornerShape(100.dp), color = Color(0xFF1E293B)) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Station 4B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Officer Badge
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Inspector V. Kadam • Senior Disaster Moderator", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    Text("Municipal Disaster Management Authority (Bandra Division)", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
        }

        // Incident Triage Card
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
                        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(ResQDangerContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Flood, null, tint = ResQDangerRed, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INCIDENT #104", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ResQDangerRed)
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (detourApprovedByOfficer) ResQBlueContainer else ResQAmberContainer
                    ) {
                        Text(
                            text = if (detourApprovedByOfficer) "BROADCAST ACTIVE" else "PENDING DISPATCH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (detourApprovedByOfficer) ResQBluePrimary else Color(0xFF704600),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(hazard.title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))
                Text("Reported along ${hazard.locationName} • Depth: ${hazard.waterDepthCm}cm", fontSize = 12.sp, color = Color(0xFF524436))

                // Metrics Matrix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8FAFC), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Citizen Reports", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("${hazard.neighborsConfirmed}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("Geo-verified", fontSize = 9.sp, color = ResQBluePrimary)
                        }
                    }

                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8FAFC), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Water Depth", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("${hazard.waterDepthCm} cm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ResQDangerRed)
                            Text("Impassable", fontSize = 9.sp, color = ResQDangerRed)
                        }
                    }

                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8FAFC), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Affected Units", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("1,420", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("Sector 17 Handsets", fontSize = 9.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }

        // Decision Impact Forecaster
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AltRoute, null, tint = ResQBluePrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Proposed High-Ground Detour", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                }

                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF1F5F9), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Reroute corridor: ${hazard.detourName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("+3.2 km distance • +6 min average travel time", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Surface(shape = RoundedCornerShape(100.dp), color = ResQBlueContainer) {
                            Text("High Ground", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                Text(
                    text = "Approving this detour will automatically update the routing graph for all connected mobile handsets within a 3.5 km radius, pushing canal closures and safely funneling citizen traffic to St. Jude Safe Haven.",
                    fontSize = 11.sp,
                    color = Color(0xFF524436),
                    lineHeight = 16.sp
                )
            }
        }

        // Officer Actions
        if (!detourApprovedByOfficer) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onBack() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reject", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                }

                Button(
                    onClick = { showApproveConfirmDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp)
                        .testTag("approve_detour_button")
                ) {
                    Icon(Icons.Default.BroadcastOnPersonal, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Approve Detour & Broadcast", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ResQBlueContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = ResQBluePrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Detour Approved by Insp. V. Kadam. 1,420 Handsets updated via Push & SMS broadcast.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001B3B)
                    )
                }
            }
        }

        // Recent Audit Trail
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recent Moderation Actions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                }

                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8FAFC), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Incident #101: Hill Road Culvert cleared", fontSize = 11.sp, color = Color(0xFF1A1C1C))
                        Text("18m ago", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }

                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8FAFC), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Incident #98: Sector 4 Underpass hard closed", fontSize = 11.sp, color = Color(0xFF1A1C1C))
                        Text("42m ago", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }

    if (showApproveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showApproveConfirmDialog = false },
            title = { Text("Confirm Detour Broadcast", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You are issuing an authoritative road closure broadcast for Sector 17 Canal Road. 1,420 nearby citizens will be rerouted over Ridge Road.",
                    fontSize = 13.sp,
                    color = Color(0xFF524436)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApproveConfirmDialog = false
                        detourApprovedByOfficer = true
                        onApproveDetour()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm & Broadcast")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
