/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/components/ElevationProfileCanvas.kt
 *
 * PURPOSE & AIM:
 * Custom graphics canvas rendering the geological cross-section and elevation profile.
 * Visually communicates the terrain elevation difference between the low flooded basin
 * (+2m Canal Road with 48cm standing water) and the elevated safe dry ridge (+32m Ridge Road).
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [ElevationProfileCanvas].
 * - Consumed By: [RouteComparisonScreen] for hydrologic terrain visualization.
 */

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed

/**
 * Custom Compose canvas displaying the elevation profile of candidate evacuation paths.
 */
@Composable
fun ElevationProfileCanvas(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEEF3F7))
            .border(1.dp, Color(0xFFE9E8E8), RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Elevated Dry Ridge Terrain
            val ridgePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.48f)
                cubicTo(w * 0.75f, h * 0.55f, w * 0.5f, h * 0.38f, 0f, h * 0.58f)
                close()
            }
            drawPath(ridgePath, color = Color(0xFFE2F3E8))

            // 2. Flooded Basin & Canal Lowland
            val floodBasinPath = Path().apply {
                moveTo(0f, h * 0.65f)
                cubicTo(w * 0.25f, h * 0.62f, w * 0.4f, h * 0.75f, w * 0.55f, h * 0.72f)
                lineTo(w * 0.55f, h)
                lineTo(0f, h)
                close()
            }
            drawPath(floodBasinPath, color = ResQDangerContainer.copy(alpha = 0.65f))

            // Water Surface Shimmer Line
            drawLine(
                color = ResQDangerRed,
                start = Offset(0f, h * 0.65f),
                end = Offset(w * 0.55f, h * 0.72f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Safe Ridge Trajectory Polyline
            val safeRoutePath = Path().apply {
                moveTo(w * 0.08f, h * 0.54f)
                cubicTo(w * 0.35f, h * 0.35f, w * 0.65f, h * 0.45f, w * 0.92f, h * 0.42f)
            }
            drawPath(
                path = safeRoutePath,
                color = ResQBluePrimary,
                style = Stroke(
                    width = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f),
                    cap = StrokeCap.Round
                )
            )

            // Origin Marker (User Position)
            drawCircle(
                color = ResQBluePrimary,
                radius = 10f,
                center = Offset(w * 0.08f, h * 0.54f)
            )

            // Destination Marker (Safe Haven Shelter)
            drawCircle(
                color = Color(0xFF059669),
                radius = 14f,
                center = Offset(w * 0.92f, h * 0.42f)
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(w * 0.92f, h * 0.42f)
            )

            // Hazard Submersion Point Indicator
            drawCircle(
                color = ResQDangerRed,
                radius = 12f,
                center = Offset(w * 0.28f, h * 0.67f)
            )
        }

        // Overlay Callout Badges
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1FADF))
            ) {
                Text(
                    text = "High Ground (+32m Ridge Spine)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF027A48),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ResQDangerRed,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "48cm Deep Flood Basin",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Culvert #104 Overtopping",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7A271A)
            )
        }
    }
}
