/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/components/MicroGisRadarCanvas.kt
 *
 * PURPOSE & AIM:
 * Custom Canvas-based micro-GIS radar visualizer displaying animated radar sweep rings,
 * Red Zone flood polygons, safe evacuation spine polylines, and terrain elevation contours.
 * Functions as an ultra-compact, high-performance GIS display.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [MicroGisRadarCanvas].
 * - Consumed By: Secondary situational views and radar comparison panels.
 */

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQBlueDark
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed
import com.example.ui.theme.ResQSafeGreen

/**
 * Micro GIS radar visualizer with animated concentric pulse circles and flood hazard polygons.
 */
@Composable
fun MicroGisRadarCanvas(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isExpanded) 340.dp else 220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A)) // Dark tactical map canvas
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.45f, h * 0.55f)

            // 1. Radar Range Rings
            val ringRadii = listOf(w * 0.18f, w * 0.36f, w * 0.54f)
            ringRadii.forEach { r ->
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }

            // 2. Animated Radar Pulse Wave
            drawCircle(
                color = ResQBluePrimary.copy(alpha = pulseAlpha),
                radius = pulseRadius * (w / 300f),
                center = center,
                style = Stroke(width = 2.5f)
            )

            // 3. Flooded Culvert Basin (Red Polygon Overlay)
            val floodPolygon = Path().apply {
                moveTo(w * 0.18f, h * 0.62f)
                lineTo(w * 0.38f, h * 0.58f)
                lineTo(w * 0.42f, h * 0.78f)
                lineTo(w * 0.22f, h * 0.82f)
                close()
            }
            drawPath(
                path = floodPolygon,
                brush = Brush.radialGradient(
                    colors = listOf(ResQDangerRed.copy(alpha = 0.55f), ResQDangerRed.copy(alpha = 0.15f)),
                    center = Offset(w * 0.30f, h * 0.70f),
                    radius = 90f
                )
            )
            drawPath(
                path = floodPolygon,
                color = ResQDangerRed,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            )

            // 4. Safe Haven Ridge Spine (Green Elevated Polyline)
            val safeSpinePath = Path().apply {
                moveTo(center.x, center.y)
                cubicTo(w * 0.52f, h * 0.40f, w * 0.65f, h * 0.32f, w * 0.76f, h * 0.22f)
            }
            drawPath(
                path = safeSpinePath,
                color = ResQSafeGreen,
                style = Stroke(
                    width = 5f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                )
            )

            // 5. Unsafe Flooded Path (Red Warning Cross-Line)
            val unsafePath = Path().apply {
                moveTo(center.x, center.y)
                lineTo(w * 0.30f, h * 0.70f)
            }
            drawPath(
                path = unsafePath,
                color = ResQDangerRed.copy(alpha = 0.8f),
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
            )

            // 6. User Position Beacon
            drawCircle(
                color = ResQBluePrimary,
                radius = 7f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = center
            )

            // 7. Safe Haven Destination Beacon
            val shelterPos = Offset(w * 0.76f, h * 0.22f)
            drawCircle(
                color = ResQSafeGreen,
                radius = 9f,
                center = shelterPos
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = shelterPos
            )

            // 8. Culvert Node #104 Danger Marker
            val hazardCenter = Offset(w * 0.30f, h * 0.70f)
            drawCircle(
                color = ResQDangerRed,
                radius = 8f,
                center = hazardCenter
            )
        }

        // Top Status Header Overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ResQAmberAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE MICRO-GIS RADAR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1F5F9)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Expand / Contract toggle button
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                shape = CircleShape,
                modifier = Modifier
                    .clickable { onToggleExpand() }
                    .size(32.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isExpanded) "Collapse radar" else "Expand radar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom Legend Overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ResQSafeGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Dry Ridge Spine (+32m)",
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ResQDangerRed)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Culvert Basin (Flooded)",
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}
