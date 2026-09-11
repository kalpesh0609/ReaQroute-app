/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/EvacuationRadarScreen.kt
 *
 * PURPOSE & AIM:
 * The flagship dashboard and situation room for citizen evacuation.
 * Answers the life-or-death question: "AM I SAFE?".
 * Provides high-visibility threat status, interactive OSRM vector map with hazard overlays,
 * rapid route launch actions, safe haven shelter capacity telemetry, and emergency SOS shortcuts.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [EvacuationRadarScreen].
 * - Consumed Models: [Shelter], [MapUiState].
 * - Child Components: [ResQMapPlaceholder] (interactive OSRM GIS map with layer overlays).
 * - Navigation Callbacks: [onStartEvacuation], [onRequestRescue], [onViewRouteDetails], [onViewShelterDetails].
 * - Invoked From: [MainActivity] when [ActiveScreen.Radar] is selected.
 */

package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmergencyShare
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed

@Composable
fun EvacuationRadarScreen(
    shelter: Shelter,
    mapUiState: MapUiState = MapUiState(),
    onStartEvacuation: () -> Unit,
    onRequestRescue: () -> Unit,
    onViewRouteDetails: () -> Unit,
    onViewShelterDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMapExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pingAnimation")
    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pingAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. CORE QUESTION: 'AM I SAFE?' - High-visibility life threat banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ResQDangerRed),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .testTag("life_threat_banner")
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = ResQDangerRed,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFA4A2).copy(alpha = pingAlpha))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "LIFE THREAT ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "FLASH FLOOD EMERGENCY",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 20.sp
                        )

                        Text(
                            text = "Sector 17 Low Basin • Immediate High-Ground Evacuation Mandated",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.95f),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Municipal Disaster Authority (NDRF) • Issued 1 min ago",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // 2. EXPANDED MAP HERO: Micro-GIS Topological Vector Map Canvas
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ResQBluePrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = ResQBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Recommended Escape Ridge",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1C)
                            )
                            Text(
                                text = "Dry spine corridor (+32m elevation)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF524436)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = ResQBlueContainer.copy(alpha = 0.6f),
                        modifier = Modifier.clickable { onViewRouteDetails() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = ResQBluePrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear & Dry",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ResQBluePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Map Placeholder & Real Map SDK Connector (Powered by OSRM)
                ResQMapPlaceholder(
                    uiState = mapUiState,
                    isExpanded = isMapExpanded,
                    onToggleExpand = { isMapExpanded = !isMapExpanded }
                )

                // OSRM Routing & Hazard/Report Overlay Telemetry Strip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = ResQBluePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OSRM Routing • 2.6 km • 14 mins walk",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "2 Hazards Avoided",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. CORE QUESTION: 'WHERE SHOULD I GO?' - Destination Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF4F3F3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewShelterDetails() }
                        .testTag("shelter_destination_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DESIGNATED SAFE HAVEN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = ResQAmberWarning
                                )
                                Text(
                                    text = shelter.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1A1C1C),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "High Ground Ridge Corridor • (+32m MSL)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF524436),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${shelter.walkTimeMins} min",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ResQBluePrimary
                                )
                                Text(
                                    text = "${shelter.distanceKm} km walk",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF524436)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Amenities & Capacity Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bed,
                                        contentDescription = null,
                                        tint = ResQBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "${shelter.availableSpaces} Beds",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A1C1C)
                                        )
                                        Text(
                                            text = "Available",
                                            fontSize = 9.sp,
                                            color = Color(0xFF524436)
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Accessible,
                                        contentDescription = null,
                                        tint = ResQBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Step-Free",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A1C1C)
                                        )
                                        Text(
                                            text = "ADA Ramp",
                                            fontSize = 9.sp,
                                            color = Color(0xFF524436)
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = ResQAmberWarning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Power & Food",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A1C1C)
                                        )
                                        Text(
                                            text = "Verified",
                                            fontSize = 9.sp,
                                            color = Color(0xFF524436)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. ACTION BUTTONS: Primary Evacuate CTA + Lifeline Rescue Button
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onStartEvacuation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_evacuation_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResQBluePrimary)
            ) {
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
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Start Evacuation to High Ground",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            OutlinedButton(
                onClick = onRequestRescue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("request_rescue_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ResQDangerContainer,
                    contentColor = ResQDangerRed
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(ResQDangerRed.copy(alpha = 0.25f)))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmergencyShare,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trapped or need assistance? Request Rescue",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
