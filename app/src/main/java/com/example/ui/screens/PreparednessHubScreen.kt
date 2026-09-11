/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/PreparednessHubScreen.kt
 *
 * PURPOSE & AIM:
 * Comprehensive family preparedness and crisis readiness hub.
 * Manages the essential 72-hour Go-Bag checklist with category grouping and packing status,
 * household emergency plan drills, offline SMS emergency templates, and operational mode switching
 * (Peacetime vs. Drill vs. Emergency).
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [PreparednessHubScreen].
 * - Consumed Models: [Shelter], [GoBagItem], [OperatingMode].
 * - Navigation & State Callbacks: [onToggleMode], [onToggleGoBagItem], [onNavigateToRoute],
 *   [onNavigateToShelter], [onNavigateToSMS], [onNavigateToSOS].
 * - Invoked From: [MainActivity] when [ActiveScreen.PreparednessHub] or [MobileTab.PREPAREDNESS] is selected.
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoBagItem
import com.example.data.model.OperatingMode
import com.example.data.model.Shelter
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed

@Composable
fun PreparednessHubScreen(
    shelter: Shelter,
    goBagItems: List<GoBagItem>,
    operatingMode: OperatingMode,
    onToggleMode: (OperatingMode) -> Unit,
    onToggleGoBagItem: (String) -> Unit,
    onNavigateToRoute: () -> Unit,
    onNavigateToShelter: () -> Unit,
    onNavigateToSMS: () -> Unit,
    onNavigateToSOS: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showInventoryDialog by remember { mutableStateOf(false) }

    val packedCount = goBagItems.count { it.packed }
    val totalCount = goBagItems.size.coerceAtLeast(1)
    val percentage = (packedCount * 100) / totalCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // District Header & Live Civic Telemetry
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PEACETIME MONITORING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF524436)
                    )
                    Text(
                        text = "Good morning, Drashti",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1C1C)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFFF4F3F3)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ResQBluePrimary)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Live Sync",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF524436)
                        )
                    }
                }
            }

            // Civic Telemetry Chip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF4F3F3),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ResQBluePrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bandra West • District 17 • Normal Status (River Gauge 1.2m / Safe)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF524436)
                    )
                }
            }

            // Peacetime vs Emergency Drill Switcher
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFEEED),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (operatingMode == OperatingMode.PEACETIME) Color.White else Color.Transparent,
                        shadowElevation = if (operatingMode == OperatingMode.PEACETIME) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggleMode(OperatingMode.PEACETIME) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ResQAmberWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Peacetime Hub",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1C1C)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (operatingMode == OperatingMode.DRILL || operatingMode == OperatingMode.EMERGENCY) ResQDangerRed else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggleMode(OperatingMode.DRILL) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (operatingMode != OperatingMode.PEACETIME) Color.White else Color(0xFF524436),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Emergency Drill",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (operatingMode != OperatingMode.PEACETIME) Color.White else Color(0xFF524436)
                            )
                        }
                    }
                }
            }
        }

        // Monsoon Readiness Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = ResQBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "HYDROLOGY & DRAINAGE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ResQBluePrimary
                            )
                        }
                        Text(
                            text = "Monsoon Readiness",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1C),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = ResQAmberAccent.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cyclone,
                                contentDescription = null,
                                tint = Color(0xFF704600),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Seasonal Watch",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF704600)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF4F3F3),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "72h Precipitation",
                                fontSize = 11.sp,
                                color = Color(0xFF524436)
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "22",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1A1C1C)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "mm",
                                    fontSize = 12.sp,
                                    color = Color(0xFF524436)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = ResQBluePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Low Risk",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ResQBluePrimary
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF4F3F3),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Culvert Flow Capacity",
                                fontSize = 11.sp,
                                color = Color(0xFF524436)
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "98",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1A1C1C)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF524436)
                                )
                            }
                            Text(
                                text = "Node #104 • 2m ago",
                                fontSize = 11.sp,
                                color = Color(0xFF524436)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ResQAmberContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = ResQAmberWarning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pre-monsoon culvert desilting active in Sector 4",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF653E00)
                        )
                    }
                }
            }
        }

        // Go-Bag Readiness Interactive Ring & Checklist
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Go-Bag Readiness",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1C)
                        )
                        Text(
                            text = "$packedCount of $totalCount household essentials packed",
                            fontSize = 12.sp,
                            color = Color(0xFF524436)
                        )
                    }

                    Text(
                        text = "Manage Items",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ResQBluePrimary,
                        modifier = Modifier.clickable { showInventoryDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Progress Canvas
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14f
                            // Background track
                            drawCircle(
                                color = Color(0xFFE9E8E8),
                                radius = size.minDimension / 2 - strokeWidth,
                                style = Stroke(width = strokeWidth)
                            )
                            // Progress arc
                            drawArc(
                                color = ResQBluePrimary,
                                startAngle = -90f,
                                sweepAngle = (percentage / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$percentage%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C1C)
                            )
                            Text(
                                text = "Ready",
                                fontSize = 9.sp,
                                color = Color(0xFF524436)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Quick Item Pills
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        goBagItems.take(3).forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = if (item.packed) Color(0xFFF4F3F3) else ResQAmberContainer.copy(alpha = 0.5f),
                                modifier = Modifier.clickable { onToggleGoBagItem(item.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (item.packed) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (item.packed) ResQBluePrimary else ResQAmberWarning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.name.substringBefore(" ("),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (item.packed) Color(0xFF1A1C1C) else Color(0xFF704600),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showInventoryDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEEED)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        tint = Color(0xFF1A1C1C),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Update Go-Bag Inventory",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1C)
                    )
                }
            }
        }

        // Core Safeguards 2x2 Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "CORE SAFEGUARDS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color(0xFF524436)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tile 1: Offline Map
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToSMS() }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ResQBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Map, null, tint = Color(0xFF001B3B), modifier = Modifier.size(18.dp))
                            }
                            Icon(Icons.Default.CheckCircle, null, tint = ResQBluePrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Offline Map Pack", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text("District 17 (42 MB)", fontSize = 11.sp, color = Color(0xFF524436))
                    }
                }

                // Tile 2: Shelter
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToShelter() }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ResQAmberContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NightShelter, null, tint = Color(0xFF2A1700), modifier = Modifier.size(18.dp))
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = ResQBlueContainer) {
                                Text("Open", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3B), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(shelter.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C), maxLines = 1)
                        Text("${shelter.distanceKm} km • Ridge Spine", fontSize = 11.sp, color = Color(0xFF524436))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tile 3: Emergency SOS
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToSOS() }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ResQDangerContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Sos, null, tint = ResQDangerRed, modifier = Modifier.size(18.dp))
                            }
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ResQBluePrimary))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Emergency SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text("3 Confirmed • NDRF", fontSize = 11.sp, color = Color(0xFF524436))
                    }
                }

                // Tile 4: Family Safe Word
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ResQBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Group, null, tint = Color(0xFF001B3B), modifier = Modifier.size(18.dp))
                            }
                            Icon(Icons.Default.Sync, null, tint = ResQBluePrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Family Safe Word", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                        Text("Synced • BLUE RIVER", fontSize = 11.sp, color = Color(0xFF524436))
                    }
                }
            }
        }

        // Daily Corridor Simulation Card & CTA
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ResQBluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Explore, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Corridor Simulation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1C)
                        )
                        Text(
                            text = "Autonomous route testing validated 4 minutes ago. Your primary high-ground evacuation path is verified dry.",
                            fontSize = 12.sp,
                            color = Color(0xFF524436),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onNavigateToRoute,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Inspect Cached Evacuation Route", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // Go-Bag Inventory Dialog
    if (showInventoryDialog) {
        AlertDialog(
            onDismissRequest = { showInventoryDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("72-Hour Emergency Go-Bag", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Tap items to mark as packed", fontSize = 11.sp, color = Color(0xFF524436))
                    }
                    IconButton(onClick = { showInventoryDialog = false }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(goBagItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (item.packed) Color(0xFFF4F3F3) else Color(0xFFFAF9F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleGoBagItem(item.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.packed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (item.packed) ResQBluePrimary else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (item.packed) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (item.packed) Color(0xFF524436) else Color(0xFF1A1C1C)
                                    )
                                    Text(
                                        text = item.note,
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                if (item.essential) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = ResQAmberContainer.copy(alpha = 0.5f)) {
                                        Text("ESSENTIAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF704600), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInventoryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save & Close ($percentage% Ready)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
