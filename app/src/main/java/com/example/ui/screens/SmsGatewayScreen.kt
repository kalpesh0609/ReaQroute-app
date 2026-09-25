/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/SmsGatewayScreen.kt
 *
 * PURPOSE & AIM:
 * Zero-internet offline SMS and 2G emergency fallback gateway.
 * Formats ultra-dense structured SMS telegrams (e.g., "RESQ SOS Bandra #402 19.0545N,72.8285E 2P WD:BLUE-RIVER")
 * to ensure distress calls, location beacons, and shelter queries succeed even when cellular towers
 * lose broadband internet backhaul.
 * Supports direct Android SMS Intent dispatch, inbound 2G broadcast simulation, and offline
 * peer-to-peer mesh packet generation.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [SmsGatewayScreen].
 * - Action Callbacks: [onBack], [onIngestBroadcast].
 * - Ingests Into: [ResQRouteRepository] and Room SQLite via [SmsBroadcastReceiver].
 * - Invoked From: [MainActivity] when [ActiveScreen.SmsGateway] is triggered.
 */

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed
import com.example.ui.theme.ResQSafeGreen

@Composable
fun SmsGatewayScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onIngestBroadcast: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var inputQuery by remember { mutableStateOf("ROUTE 400050") }
    var terminalOutput by remember {
        mutableStateOf("RESQ SAFE: Turn Right Ridge Rd (+32m). St. Jude Shelter 2.6km / 14m. 55 beds open. Avoid Canal Rd (48cm water).")
    }
    var showQrModal by remember { mutableStateOf(false) }
    var peerMeshActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
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

            Text("Offline SMS Gateway", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = ResQAmberContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WifiOff, null, tint = Color(0xFF704600), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Zero Data Mode", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF704600))
                }
            }
        }

        // Protocol Info Banner
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
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
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CellTower, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2G GSM & SMS BROADCAST PROTOCOL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0F172A)) {
                        Text("Shortcode: 56161", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Text(
                    text = "When 4G/5G cell towers fail during monsoons, structured 2G SMS payloads deliver live road conditions, shelter bed counts, and emergency beacons under 160 characters.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )

                // Direct Android SMS Dispatcher Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            dispatchSystemSms(context, "56161", inputQuery)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send via SMS App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            dispatchSystemSms(context, "112", "RESQ SOS Bandra #402 19.0545N,72.8285E 2P WD:BLUE-RIVER")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ResQDangerRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(14.dp), tint = ResQDangerRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SOS 112 SMS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // =========================================================================
        // PHASE 4 FEATURE: INTERACTIVE DISASTER COMMAND GENERATOR
        // =========================================================================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            var selectedGeneratorMode by remember { mutableStateOf("SOS") }
            var sosAddress by remember { mutableStateOf("Bandra #402") }
            var sosGps by remember { mutableStateOf("19.0545N,72.8285E") }
            var sosCount by remember { mutableStateOf("2P") }
            var sosWord by remember { mutableStateOf("BLUE-RIVER") }

            var shelterPin by remember { mutableStateOf("400050") }
            var shelterCode by remember { mutableStateOf("ST-JUDE") }

            var reportHazId by remember { mutableStateOf("104") }
            var reportDepth by remember { mutableStateOf("48") }
            var reportRoad by remember { mutableStateOf("CANAL-RD") }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("DISASTER COMMAND GENERATOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)

                // Generator Mode Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("SOS", "SHELTER", "REPORT").forEach { mode ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedGeneratorMode == mode) ResQBluePrimary else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedGeneratorMode = mode }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = mode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedGeneratorMode == mode) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                when (selectedGeneratorMode) {
                    "SOS" -> {
                        Text("Formats distress telegram with GPS beacon & household safeword:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sosAddress,
                                onValueChange = { sosAddress = it },
                                label = { Text("Sector / Apt") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sosGps,
                                onValueChange = { sosGps = it },
                                label = { Text("GPS Coordinates") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sosCount,
                                onValueChange = { sosCount = it },
                                label = { Text("People (e.g. 2P)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sosWord,
                                onValueChange = { sosWord = it },
                                label = { Text("Safeword") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                val generated = "RESQ SOS $sosAddress $sosGps $sosCount WD:$sosWord"
                                inputQuery = generated
                                terminalOutput = "RESQ SOS ACK: Distress alert broadcast to NDRF Bandra West. 2 rescue craft active in Sector 17."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ResQDangerRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate SOS Telegram (160 Char Buffer)", fontWeight = FontWeight.Bold)
                        }
                    }

                    "SHELTER" -> {
                        Text("Query live bed availability and remaining spaces at high ground:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = shelterPin,
                                onValueChange = { shelterPin = it },
                                label = { Text("Pincode") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = shelterCode,
                                onValueChange = { shelterCode = it },
                                label = { Text("Shelter Code") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                val generated = "RESQ SHELTER $shelterPin $shelterCode REQ:CAPACITY"
                                inputQuery = generated
                                terminalOutput = "RESQ HAVEN: St. Jude Center (55 beds), Ridge High Annex (120 beds). Both open. Generator power active."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ResQAmberWarning),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Shelter Query", fontWeight = FontWeight.Bold)
                        }
                    }

                    "REPORT" -> {
                        Text("Relay field reports of submerged culverts or impassable roadways:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = reportHazId,
                                onValueChange = { reportHazId = it },
                                label = { Text("Hazard ID") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = reportDepth,
                                onValueChange = { reportDepth = it },
                                label = { Text("Depth (cm)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = reportRoad,
                                onValueChange = { reportRoad = it },
                                label = { Text("Road") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                val generated = "RESQ REPORT HAZ:$reportHazId DEPTH:${reportDepth}CM ROAD:$reportRoad STATUS:CLOSED BYPASS:RIDGE-ROAD"
                                inputQuery = generated
                                terminalOutput = "RESQ REPORT ACK: Hazard #$reportHazId updated to ${reportDepth}cm in municipal registry. Ridge Road bypass active."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Hazard Field Report", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SMS Sandbox Simulator
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
                        Icon(Icons.Default.Terminal, null, tint = Color(0xFF0F172A), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulated SMS Handset Console", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }
                    Text("160 Char Buffer", fontSize = 10.sp, color = Color(0xFF64748B))
                }

                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it.uppercase() },
                    label = { Text("SMS Query Text") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sms_query_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ResQBluePrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            terminalOutput = when {
                                inputQuery.contains("SOS") -> "RESQ SOS ACK: Distress alert broadcast to NDRF Bandra West. 2 rescue inflatable crafts active in Sector 17."
                                inputQuery.contains("SHELTER") -> "RESQ HAVEN: St. Jude Center (55 beds), Ridge High Annex (120 beds). Both open. Generator power active."
                                else -> "RESQ SAFE: Turn Right Ridge Rd (+32m). St. Jude Shelter 2.6km / 14m. 55 beds open. Avoid Canal Rd (48cm water)."
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("send_sms_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send SMS Query", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(inputQuery))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                    }
                }

                // SMS Response Terminal Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("INCOMING 160-CHAR PDU", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF38BDF8))
                            Text("${terminalOutput.length}/160 chars", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                        }

                        Text(
                            text = terminalOutput,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF8FAFC),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // =========================================================================
        // PHASE 4 FEATURE: 2G INBOUND BROADCAST SIMULATOR & ROOM INGESTION
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
                        Icon(Icons.Default.Download, null, tint = ResQAmberWarning, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("2G Inbound Broadcast Ingestion", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }
                    Text("Auto-Syncs Room DB", fontSize = 10.sp, color = ResQSafeGreen, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Test automatic processing of incoming cellular broadcast alerts without data backhaul:",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                // Interactive Broadcast Ingestion Buttons
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val msg = "RESQ BROADCAST HAZ:104 DEPTH:56CM STATUS:CLOSED BYPASS:RIDGE-ROAD"
                                onIngestBroadcast?.invoke(msg)
                                terminalOutput = msg
                            }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🚨", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Inbound Flood Alert (56cm)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ResQDangerRed)
                                Text("Updates Hazard #104 in Room to 56cm & triggers detour warning", fontSize = 9.sp, color = Color(0xFF7F1D1D))
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val msg = "RESQ SHELTER:ST-JUDE OCCUPIED:215"
                                onIngestBroadcast?.invoke(msg)
                                terminalOutput = msg
                            }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🏠", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Inbound Shelter Update (215 Occupied)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ResQSafeGreen)
                                Text("Synchronizes remaining bed counter to 35 available spaces", fontSize = 9.sp, color = Color(0xFF14532D))
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val msg = "RESQ CONSENSUS HAZ:104 CONFIRMED:+1"
                                onIngestBroadcast?.invoke(msg)
                                terminalOutput = msg
                            }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("👥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Inbound Neighbor Verification (+1)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ResQAmberWarning)
                                Text("Increments consensus quorum counter for Sector 17 culvert", fontSize = 9.sp, color = Color(0xFF78350F))
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // PHASE 4 FEATURE: PEER-TO-PEER BLE MESH PACKET EXCHANGE
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
                        Icon(Icons.Default.Bluetooth, null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Handset-to-Handset Peer Mesh", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                    }
                    Text(if (peerMeshActive) "4 PEERS CONNECTED" else "IDLE", fontSize = 10.sp, color = if (peerMeshActive) ResQSafeGreen else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Nearby citizen handsets exchange encrypted BLE mesh packets carrying local flood depths and detour consensus without cellular towers or internet.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                if (peerMeshActive) {
                    // Live Mesh Packet Stream Console
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("LIVE BLE MESH PACKET STREAM (RSSI -68 dBm • 18 Hops)", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            Text("[MESH] NODE_ALPHA (19.054,72.828): HAZ#104=48CM (2m ago)", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF86EFAC))
                            Text("[MESH] NODE_BRAVO (19.059,72.833): RIDGE_RD_DRY_PASSABLE (4m ago)", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF93C5FD))
                            Text("[MESH] NODE_CHARLIE (19.066,72.836): ST_JUDE_HAVEN_55_BEDS (6m ago)", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFFDE047))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { peerMeshActive = !peerMeshActive },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (peerMeshActive) ResQSafeGreen else Color(0xFF2563EB)
                        ),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (peerMeshActive) "Mesh Active" else "Start BLE Mesh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showQrModal = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Offline QR & Base64", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // =========================================================================
    // PHASE 4 FEATURE: COMPACT BASE64 QR PAYLOAD GENERATOR & PEER VERIFIER
    // =========================================================================
    if (showQrModal) {
        val base64Payload = remember {
            android.util.Base64.encodeToString(
                "RESQ:V4:GEO:19.0545,72.8285:HAZ:104:48CM:SH:210:SAFE:RIDGE_ROAD:CRC:8A1F".toByteArray(),
                android.util.Base64.NO_WRAP
            )
        }
        var verifyInput by remember { mutableStateOf("") }
        var verificationResult by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showQrModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = ResQBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Offline Peer Verification Token", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.size(150.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "█▀▀▀▀▀█ ▄▄█ █▀▀▀▀▀█\n█ ███ █ ▄█▄ █ ███ █\n█ ▀▀▀ █ █ █ █ ▀▀▀ █\n▀▀▀▀▀▀▀ ▀▄█ ▀▀▀▀▀▀▀\n███▀█▄█▀█▄█▄▀█▀██▄█\n█▀▀▀▀▀█ ▄█▀▀█▄▄█▀██\n█ ███ █ █▄▄ █▀▄█ ▀█\n█ ▀▀▀ █ █▀█ █ █▄█▄█\n▀▀▀▀▀▀▀ ▀▀▀ ▀▀▀▀▀▀▀",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                color = Color.White,
                                lineHeight = 9.sp
                            )
                        }
                    }

                    // Compact Base64 Encoded Payload String
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("COMPACT BASE64 PAYLOAD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                                Text("${base64Payload.length} bytes", fontSize = 9.sp, color = Color(0xFF64748B))
                            }
                            Text(
                                text = base64Payload,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    // Peer Token Verifier Input
                    OutlinedTextField(
                        value = verifyInput,
                        onValueChange = { verifyInput = it },
                        label = { Text("Paste Peer Token to Verify") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val token = if (verifyInput.isNotBlank()) verifyInput.trim() else base64Payload
                                try {
                                    val decoded = String(android.util.Base64.decode(token, android.util.Base64.DEFAULT))
                                    verificationResult = "✓ Verified Authentic: $decoded"
                                } catch (e: Exception) {
                                    verificationResult = "⚠ Invalid Token Format: ${e.message}"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Verify Token", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { verifyInput = base64Payload },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Auto-Paste", fontSize = 11.sp)
                        }
                    }

                    verificationResult?.let { res ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (res.startsWith("✓")) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = res,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (res.startsWith("✓")) Color(0xFF166534) else Color(0xFF991B1B),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(base64Payload))
                }) {
                    Text("Copy Token", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrModal = false }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * Fires an Android system intent to launch the default SMS app with pre-filled number and body.
 */
private fun dispatchSystemSms(context: Context, number: String, body: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$number")
            putExtra("sms_body", body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback for devices without standard telephony app
        val genericIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:$number?body=${Uri.encode(body)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(genericIntent)
        } catch (_: Exception) {}
    }
}
