/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/components/LiveGuidanceCanvas.kt
 *
 * PURPOSE & AIM:
 * Custom animated graphics HUD showing live GPS pulse tracking, dynamic corridor direction arrows,
 * and high-contrast night/storm guidance cues for active evacuation.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [LiveGuidanceCanvas].
 * - Consumed By: [LiveGuidanceScreen] as an animated navigation background visualizer.
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerRed

/**
 * Animated guidance HUD displaying pulsing user GPS location and forward evacuation trajectory.
 */
@Composable
fun LiveGuidanceCanvas(
    modifier: Modifier = Modifier
) {
    // Pulse animation representing active GPS positioning
    val infiniteTransition = rememberInfiniteTransition(label = "gpsPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 44f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gpsRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gpsAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Grid lines representing terrain reference
            for (i in 1..4) {
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(0f, h * (i * 0.2f)),
                    end = Offset(w, h * (i * 0.2f)),
                    strokeWidth = 2f
                )
            }

            // 2. Safe Evacuation Corridor Road Path
            val roadPath = Path().apply {
                moveTo(w * 0.5f, h)
                cubicTo(w * 0.5f, h * 0.7f, w * 0.42f, h * 0.45f, w * 0.65f, h * 0.15f)
            }

            // Outer road buffer
            drawPath(
                path = roadPath,
                color = ResQBlueContainer.copy(alpha = 0.6f),
                style = Stroke(width = 36f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Inner directional spine
            drawPath(
                path = roadPath,
                color = ResQBluePrimary,
                style = Stroke(
                    width = 10f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 14f), 0f)
                )
            )

            // 3. User GPS Node with Real-time Animation Pulse
            val userPos = Offset(w * 0.5f, h * 0.8f)
            drawCircle(
                color = ResQBluePrimary.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = userPos
            )
            drawCircle(
                color = ResQBluePrimary,
                radius = 12f,
                center = userPos
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = userPos
            )

            // 4. Next Maneuver Turning Waypoint (+32m Ridge Entry)
            val turnWaypoint = Offset(w * 0.45f, h * 0.45f)
            drawCircle(
                color = ResQAmberAccent,
                radius = 9f,
                center = turnWaypoint
            )

            // 5. Destination Beacon (St. Jude Sanctuary)
            val destPos = Offset(w * 0.65f, h * 0.15f)
            drawCircle(
                color = Color(0xFF059669),
                radius = 16f,
                center = destPos
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = destPos
            )
        }
    }
}
