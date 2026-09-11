/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/components/ResQTopBar.kt
 *
 * PURPOSE & AIM:
 * Persistent top application bar providing situational brand identity, operational mode switcher
 * (Peacetime / Drill / Emergency), and instant emergency action buttons (Offline SMS Gateway and
 * Authority Admin Console).
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [ResQTopBar].
 * - Consumed Models: [OperatingMode].
 * - Action Callbacks: [onToggleOperatingMode], [onOpenSmsGateway], [onOpenAuthorityConsole].
 * - Invoked By: [Scaffold.topBar] in [MainActivity.kt].
 */

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import com.example.data.model.OperatingMode
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed

/**
 * Top app bar displaying app title, operational mode badge, and quick access actions.
 */
@Composable
fun ResQTopBar(
    operatingMode: OperatingMode,
    onToggleOperatingMode: () -> Unit,
    onOpenSmsGateway: () -> Unit,
    onOpenAuthorityConsole: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ResQBluePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RQ",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ResQRoute",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFF1A1C1C),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Sector 17 • Bandra Basin",
                        fontSize = 11.sp,
                        color = Color(0xFF5F5E5E)
                    )
                }
            }

            // Mode Badge and Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Interactive Mode Selector Badge
                val (modeBg, modeFg, modeText) = when (operatingMode) {
                    OperatingMode.PEACETIME -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "PEACETIME")
                    OperatingMode.DRILL -> Triple(ResQAmberContainer, ResQAmberWarning, "DRILL MODE")
                    OperatingMode.EMERGENCY -> Triple(ResQDangerContainer, ResQDangerRed, "EMERGENCY")
                }

                Surface(
                    color = modeBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clickable { onToggleOperatingMode() }
                        .testTag("mode_toggle_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(modeFg)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = modeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = modeFg,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Zero-Internet SMS Gateway Shortcut
                IconButton(
                    onClick = onOpenSmsGateway,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sms_gateway_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "Offline SMS Gateway",
                        tint = ResQBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Authority / Incident Command Console Shortcut
                IconButton(
                    onClick = onOpenAuthorityConsole,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("authority_console_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Authority Console",
                        tint = Color(0xFF5F5E5E),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
