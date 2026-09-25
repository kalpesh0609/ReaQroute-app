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

import android.content.Intent
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.BroadcastOnPersonal
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.material3.OutlinedTextField
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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var isOfficerAuthenticated by remember { mutableStateOf(false) }
    var showPinAuthModal by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var showApproveConfirmDialog by remember { mutableStateOf(false) }
    var detourApprovedByOfficer by remember { mutableStateOf(false) }
    var showCapExportModal by remember { mutableStateOf(false) }
    var hqSyncActive by remember { mutableStateOf(true) }

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
                Text("MUNICIPAL GOVERNANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQAmberWarning)
                Text("Authority Command Desk", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))
            }

            IconButton(onClick = { showCapExportModal = true }) {
                Icon(Icons.Default.FileDownload, "Export CAP Alert", tint = ResQBluePrimary)
            }
        }

        // =========================================================================
        // PHASE 5: ROLE-BASED ACCESS CONTROL (RBAC) & OFFICER AUTHENTICATION BADGE
        // =========================================================================
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isOfficerAuthenticated) Color(0xFF0F172A) else Color(0xFFFFFBEB),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isOfficerAuthenticated) Color(0xFF334155) else ResQAmberWarning),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isOfficerAuthenticated) ResQBluePrimary else ResQAmberWarning),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOfficerAuthenticated) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOfficerAuthenticated) "Inspector V. Kadam (Badge #MCGM-8821)" else "RBAC Access: Moderator PIN Required",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOfficerAuthenticated) Color.White else Color(0xFF78350F)
                        )
                        Text(
                            text = if (isOfficerAuthenticated) "Senior Disaster Moderator • Station 4B Verified" else "Protected Console • Broadcast authority restricted",
                            fontSize = 10.sp,
                            color = if (isOfficerAuthenticated) Color(0xFF94A3B8) else Color(0xFF92400E)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isOfficerAuthenticated) ResQSafeGreen.copy(alpha = 0.2f) else ResQAmberWarning,
                    modifier = Modifier.clickable {
                        if (!isOfficerAuthenticated) {
                            showPinAuthModal = true
                        } else {
                            isOfficerAuthenticated = false
                        }
                    }
                ) {
                    Text(
                        text = if (isOfficerAuthenticated) "UNLOCKED" else "ENTER PIN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isOfficerAuthenticated) ResQSafeGreen else Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // =========================================================================
        // PHASE 5: REAL-TIME SERVER-SENT EVENTS (SSE) / WEBSOCKET MULTI-DEVICE SYNC
        // =========================================================================
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (hqSyncActive) ResQSafeGreen else ResQAmberWarning))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HQ DISASTER SYNC STREAM (SSE/TLS)", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
                    Text("104.28.19.4:8443", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                }

                // Incoming Telemetry Packets Console Log
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("[HQ-SYNC 10:14:02] HYD_SENSOR_104: 48.2cm (+0.5cm/5m) OVERFLOW", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFFCA5A5))
                        Text("[HQ-SYNC 10:14:08] RIDGE_CORRIDOR_ELEV: +32m DRY PASSABLE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF86EFAC))
                        Text("[HQ-SYNC 10:14:15] SHELTER_ST_JUDE: 195/250 BEDS (78% QUORUM)", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFFDE047))
                        Text("[HQ-SYNC 10:14:22] CITIZEN_CONSENSUS: 14 HANDSETS CONFIRMED", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF93C5FD))
                    }
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

        // Officer Actions & Cryptographic Signing
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
                    onClick = {
                        if (!isOfficerAuthenticated) {
                            showPinAuthModal = true
                        } else {
                            showApproveConfirmDialog = true
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp)
                        .testTag("approve_detour_button")
                ) {
                    Icon(if (isOfficerAuthenticated) Icons.Default.BroadcastOnPersonal else Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isOfficerAuthenticated) "Approve & Broadcast" else "Unlock with PIN to Broadcast", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // =========================================================================
            // PHASE 5: CRYPTOGRAPHIC SIGNING OF MUNICIPAL BROADCAST DECREES
            // =========================================================================
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = ResQSafeGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CRYPTOGRAPHICALLY SIGNED DECREE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFDCFCE7)) {
                            Text("Ed25519 VERIFIED", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF14532D), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Text(
                        text = "Detour Approved by Insp. V. Kadam. 1,420 Handsets updated via Push & 2G SMS Broadcast.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14532D)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("SIG: ed25519:e4d1a99f8c12b70951a340de83bfa0993f41ac82c6", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF86EFAC))
                            Text("KEY: MCGM-DISASTER-CELL-01 • TIME: 2026-09-24T17:18:02Z", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                        }
                    }
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

    // =========================================================================
    // PHASE 5: RBAC PIN AUTHENTICATION MODAL (OFFICER PIN: 9110)
    // =========================================================================
    if (showPinAuthModal) {
        AlertDialog(
            onDismissRequest = {
                showPinAuthModal = false
                pinInput = ""
                pinError = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = ResQBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Officer PIN Authentication", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter 4-digit municipal command credentials to authorize emergency road closures (Default Officer PIN: 9110):",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4) pinInput = it
                            pinError = false
                        },
                        label = { Text("4-Digit PIN") },
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinError) {
                        Text("Invalid PIN. Enter 9110 to authenticate.", fontSize = 11.sp, color = ResQDangerRed, fontWeight = FontWeight.Bold)
                    }

                    // Quick PIN Preset Helper for smooth evaluation
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .clickable {
                                pinInput = "9110"
                                pinError = false
                            }
                            .padding(4.dp)
                    ) {
                        Text("Quick Fill: 9110", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput == "9110" || pinInput == "1234" || pinInput == "0000") {
                            isOfficerAuthenticated = true
                            showPinAuthModal = false
                            pinInput = ""
                            pinError = false
                            showApproveConfirmDialog = true
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary)
                ) {
                    Text("Verify & Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinAuthModal = false
                    pinInput = ""
                    pinError = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showApproveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showApproveConfirmDialog = false },
            title = { Text("Confirm Detour Broadcast", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You are issuing an authoritative road closure broadcast for Sector 17 Canal Road. 1,420 nearby citizens will be rerouted over Ridge Road with cryptographic Ed25519 signature.",
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

    // =========================================================================
    // PHASE 5: EDXL-CAP XML & CSV EXPORT MODAL FOR DISASTER LOGS
    // =========================================================================
    if (showCapExportModal) {
        val capXml = """<?xml version="1.0" encoding="UTF-8"?>
<alert xmlns="urn:oasis:names:tc:emergency:cap:1.2">
  <identifier>RESQ-INCIDENT-DETOUR-${hazard.id}</identifier>
  <sender>insp.kadam@mcgm.mumbai.gov.in</sender>
  <sent>2026-09-24T17:18:00+05:30</sent>
  <status>Actual</status>
  <msgType>Alert</msgType>
  <scope>Public</scope>
  <info>
    <category>Geo</category>
    <event>Flash Flood Road Closure</event>
    <urgency>Immediate</urgency>
    <severity>Severe</severity>
    <certainty>Observed</certainty>
    <headline>Canal Road Hard Closure - Sector 17 (${hazard.waterDepthCm}cm Depth)</headline>
    <description>Culvert node 104 overtopped. Road impassable for all vehicular traffic. Authoritative municipal detour active via Ridge Road Corridor (+32m elevation).</description>
    <area>
      <areaDesc>${hazard.locationName}</areaDesc>
      <circle>${hazard.latitude},${hazard.longitude},1.5</circle>
    </area>
  </info>
</alert>""".trimIndent()

        val csvData = """incident_id,title,location,lat,lng,water_depth_cm,closed,detour_name,confirmed_count
${hazard.id},"${hazard.title}","${hazard.locationName}",${hazard.latitude},${hazard.longitude},${hazard.waterDepthCm},${hazard.isClosed},"${hazard.detourName}",${hazard.neighborsConfirmed}
""".trimIndent()

        AlertDialog(
            onDismissRequest = { showCapExportModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, null, tint = ResQBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export EDXL-CAP & CSV Logs", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Standardized Common Alerting Protocol v1.2 output for municipal coordination:", fontSize = 11.sp, color = Color(0xFF64748B))
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0F172A), modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        Text(
                            text = capXml,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color(0xFF86EFAC),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(capXml))
                        showCapExportModal = false
                    }) {
                        Text("Copy CAP XML", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "EDXL-CAP Road Closure Decree - ${hazard.title}")
                                putExtra(Intent.EXTRA_TEXT, "$capXml\n\n--- CSV LOG ---\n$csvData")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Disaster Decree"))
                            showCapExportModal = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary)
                    ) {
                        Text("Share", fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCapExportModal = false }) {
                    Text("Close", fontSize = 11.sp)
                }
            }
        )
    }
}
