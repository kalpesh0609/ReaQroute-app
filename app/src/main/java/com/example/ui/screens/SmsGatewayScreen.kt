/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/SmsGatewayScreen.kt
 *
 * PURPOSE & AIM:
 * Zero-internet offline SMS and 2G emergency fallback gateway.
 * Formats ultra-dense structured SMS telegrams (e.g., "RESQ SOS Bandra #402 19.0545N,72.8285E 2P WD:BLUE-RIVER")
 * to ensure distress calls, location beacons, and shelter queries succeed even when cellular towers
 * lose broadband internet backhaul.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [SmsGatewayScreen].
 * - Action Callbacks: [onBack].
 * - Invoked From: [MainActivity] when [ActiveScreen.SmsGateway] is triggered.
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

@Composable
fun SmsGatewayScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputQuery by remember { mutableStateOf("ROUTE 400050") }
    var terminalOutput by remember {
        mutableStateOf("RESQ SAFE: Turn Right Ridge Rd (+32m). St. Jude Shelter 2.6km / 14m. 55 beds open. Avoid Canal Rd (48cm water).")
    }

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
                        Text("2G GSM FALLBACK PROTOCOL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0F172A)) {
                        Text("Shortcode: 56161", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Text(
                    text = "When 4G/5G cell towers fail during monsoons, send simple SMS queries to receive compressed turn-by-turn guidance under 160 characters.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )
            }
        }

        // Quick Command Presets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("QUICK QUERY COMMANDS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF524436))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            inputQuery = "ROUTE 400050"
                            terminalOutput = "RESQ SAFE: Turn Right Ridge Rd (+32m). St. Jude Shelter 2.6km / 14m. 55 beds open. Avoid Canal Rd (48cm water)."
                        }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("ROUTE [PIN]", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQBluePrimary)
                        Text("ROUTE 400050", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))
                        Text("Safest dry path", fontSize = 9.sp, color = Color(0xFF64748B))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            inputQuery = "SHELTER 400050"
                            terminalOutput = "RESQ HAVEN: St. Jude Center (55 beds), Ridge High Annex (120 beds). Both open. Generator power active."
                        }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("SHELTER [PIN]", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQAmberWarning)
                        Text("SHELTER 400050", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))
                        Text("Bed capacity", fontSize = 9.sp, color = Color(0xFF64748B))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            inputQuery = "SOS 19.07,72.87"
                            terminalOutput = "RESQ SOS ACK: Coordinates 19.07, 72.87 recorded by NDRF QRT Unit 4. Boat dispatched. Stay on roof."
                        }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("SOS [GPS]", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ResQDangerRed)
                        Text("SOS 19.07,72.87", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))
                        Text("Disaster dispatch", fontSize = 9.sp, color = Color(0xFF64748B))
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

                Button(
                    onClick = {
                        terminalOutput = when {
                            inputQuery.contains("SOS") -> "RESQ SOS ACK: Distress alert broadcast to NDRF Bandra West. 2 rescue inflatable crafts active in Sector 17."
                            inputQuery.contains("SHELTER") -> "RESQ HAVEN: St. Jude Center (55 beds), Ridge High Annex (120 beds). Both open. Generator power active."
                            else -> "RESQ SAFE: Turn Right Ridge Rd (+32m). St. Jude Shelter 2.6km / 14m. 55 beds open. Avoid Canal Rd (48cm water)."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("send_sms_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send SMS to 56161 (Zero Data)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
    }
}
