/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/components/ResQBottomNav.kt
 *
 * PURPOSE & AIM:
 * Persistent bottom navigation bar conforming to Material Design 3 guidelines.
 * Enables one-tap transitions between key application workflows:
 * Radar situation room, Preparedness go-bag hub, Safe Haven directory, Citizen crowdsourced dossier,
 * and User accessibility profile.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [ResQBottomNav].
 * - Consumed Models: [MobileTab].
 * - Action Callbacks: [onTabSelected].
 * - Invoked By: [Scaffold.bottomBar] in [MainActivity.kt].
 */

package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MobileTab
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQOnBlueContainer

/**
 * Bottom navigation bar with 5 key evacuation workflow tabs.
 */
@Composable
fun ResQBottomNav(
    activeTab: MobileTab,
    onTabSelected: (MobileTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        MobileTab.entries.forEach { tab ->
            val isSelected = activeTab == tab
            val (icon, testTag) = when (tab) {
                MobileTab.RADAR -> Pair(Icons.Default.Radar, "bottom_tab_radar")
                MobileTab.PREPAREDNESS -> Pair(Icons.Default.Checklist, "bottom_tab_preparedness")
                MobileTab.SAFE_ZONES -> Pair(Icons.Default.NightShelter, "bottom_tab_shelter")
                MobileTab.DOSSIER -> Pair(Icons.Default.CrisisAlert, "bottom_tab_dossier")
                MobileTab.PROFILE -> Pair(Icons.Default.Person, "bottom_tab_profile")
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ResQBluePrimary,
                    selectedTextColor = ResQBluePrimary,
                    indicatorColor = ResQBlueContainer,
                    unselectedIconColor = Color(0xFF5F5E5E),
                    unselectedTextColor = Color(0xFF5F5E5E)
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
