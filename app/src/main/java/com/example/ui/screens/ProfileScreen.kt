/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/screens/ProfileScreen.kt
 *
 * PURPOSE & AIM:
 * Citizen emergency profile and accessibility customization screen.
 * Configures vital mobility constraints (wheelchair access, slope avoidance, medical priority,
 * audio/haptic guidance cues), stores family contact safewords, and provides administrative access
 * to the municipal emergency authority console.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [ProfileScreen].
 * - Consumed Models: [UserProfile], [AccessibilityProfile].
 * - Action Callbacks: [onToggleAccessibility], [onOpenAuthorityConsole].
 * - Invoked From: [MainActivity] when [ActiveScreen.Profile] or [MobileTab.PROFILE] is selected.
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onToggleAccessibility: (String) -> Unit,
    onOpenAuthorityConsole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val acc = userProfile.accessibilityProfile

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Profile Header Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(ResQAmberWarning),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.name.take(1),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(userProfile.name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1C))
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Verified, null, tint = ResQBluePrimary, modifier = Modifier.size(16.dp))
                        }
                        Text(userProfile.phone, fontSize = 12.sp, color = Color(0xFF524436))
                        Text(userProfile.address, fontSize = 11.sp, color = Color(0xFF524436))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ResQBlueContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, null, tint = ResQBluePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verified Citizen Profile • Disaster Priority Tag: Moderate",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF001B3B)
                        )
                    }
                }
            }
        }

        // Household Vulnerability Matrix
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FamilyRestroom, null, tint = ResQBluePrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Household Composition & Medical Needs", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Elderly Dependent", fontSize = 10.sp, color = Color(0xFF524436))
                            Text("1 Senior (74y)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("Gentle Incline", fontSize = 9.sp, color = ResQBluePrimary)
                        }
                    }

                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Pediatric", fontSize = 10.sp, color = Color(0xFF524436))
                            Text("1 Toddler (3y)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("Stroller Friendly", fontSize = 9.sp, color = ResQBluePrimary)
                        }
                    }

                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4F3F3), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Blood Group", fontSize = 10.sp, color = Color(0xFF524436))
                            Text("B+ Positive", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("Verified", fontSize = 9.sp, color = ResQAmberWarning)
                        }
                    }
                }
            }
        }

        // Accessibility & Evacuation Filters
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ACCESSIBILITY & ROUTING PREFERENCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF524436)
                )

                // Filter 1: Wheelchair
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(ResQBlueContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Accessible, null, tint = ResQBluePrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Wheelchair & Stroller Accessible", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("Step-free corridors and curb-cuts only", fontSize = 11.sp, color = Color(0xFF524436))
                        }
                    }
                    Switch(
                        checked = acc.wheelchairAccessible,
                        onCheckedChange = { onToggleAccessibility("wheelchair") },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ResQBluePrimary)
                    )
                }

                // Filter 2: Slopes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(ResQAmberContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Landscape, null, tint = Color(0xFF2A1700), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Avoid Steep Gradients (>8%)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("Favors gentle contour switchbacks", fontSize = 11.sp, color = Color(0xFF524436))
                        }
                    }
                    Switch(
                        checked = acc.avoidSteepSlopes,
                        onCheckedChange = { onToggleAccessibility("slopes") },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ResQBluePrimary)
                    )
                }

                // Filter 3: Medical Priority
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFE4E6)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.LocalHospital, null, tint = Color(0xFFBE123C), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Medical Priority Routing", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("Route to shelters with oxygen and refrigeration", fontSize = 11.sp, color = Color(0xFF524436))
                        }
                    }
                    Switch(
                        checked = acc.medicalPriority,
                        onCheckedChange = { onToggleAccessibility("medical") },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ResQBluePrimary)
                    )
                }

                // Filter 4: Audio Haptics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEDE9FE)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Vibration, null, tint = Color(0xFF6D28D9), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Haptic Turn Pulses", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
                            Text("Vibratory feedback in rain noise", fontSize = 11.sp, color = Color(0xFF524436))
                        }
                    }
                    Switch(
                        checked = acc.audioHapticGuidance,
                        onCheckedChange = { onToggleAccessibility("audioHaptics") },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ResQBluePrimary)
                    )
                }
            }
        }

        // Authority Responder Console Portal
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAuthorityConsole() }
                .testTag("authority_desk_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Disaster Authority Command", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Incident Triage & Road Closures", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }

                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
            }
        }

        // Version metadata
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ResQRoute v2.4 (SIH26191)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF524436))
            Text("Intelligent Flood Evacuation & Shelter Intelligence", fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
    }
}
