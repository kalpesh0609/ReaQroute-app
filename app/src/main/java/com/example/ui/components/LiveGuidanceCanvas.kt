/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/components/LiveGuidanceCanvas.kt
 *
 * PURPOSE & AIM:
 * Custom animated graphics HUD showing live GPS pulse tracking, dynamic corridor direction arrows,
 * real-time user trajectory progression along the safe elevation spine, and high-contrast
 * night/storm guidance cues for active evacuation.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [LiveGuidanceCanvas].
 * - Consumed By: [LiveGuidanceScreen] as an interactive animated navigation visualizer.
 * - Reactive Inputs: [progressFraction] (0.0 to 1.0 along the route), [headingDegrees], [hazardNearby].
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
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberContainer
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerRed
import com.example.ui.theme.ResQSafeGreen

/**
 * Animated guidance HUD displaying pulsing user GPS location advancing along the forward evacuation corridor.
 *
 * @param progressFraction Float between 0.0f (start) and 1.0f (arrived at safe haven).
 * @param headingDegrees Current compass orientation.
 * @param hazardNearby Flag indicating proximity to active water hazard.
 */
@Composable
fun LiveGuidanceCanvas(
    progressFraction: Float = 0f,
    headingDegrees: Float = 24f,
    hazardNearby: Boolean = false,
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

            // 2. Safe Evacuation Corridor Road Path (Elevated Ridge Spine)
            val roadPath = Path().apply {
                moveTo(w * 0.5f, h * 0.88f)
                cubicTo(w * 0.48f, h * 0.68f, w * 0.40f, h * 0.42f, w * 0.65f, h * 0.16f)
            }

            // Outer road buffer
            drawPath(
                path = roadPath,
                color = if (hazardNearby) ResQAmberContainer.copy(alpha = 0.7f) else ResQBlueContainer.copy(alpha = 0.7f),
                style = Stroke(width = 44f, cap = StrokeCap.Round, join = StrokeJoin.Round)
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

            // Hazard warning zone alert on map if nearby
            if (hazardNearby) {
                drawCircle(
                    color = ResQDangerRed.copy(alpha = 0.25f),
                    radius = 80f,
                    center = Offset(w * 0.25f, h * 0.55f)
                )
                drawCircle(
                    color = ResQDangerRed,
                    radius = 12f,
                    center = Offset(w * 0.25f, h * 0.55f)
                )
            }

            // 3. Waypoint Checkpoints
            val waypoints = listOf(
                Offset(w * 0.48f, h * 0.68f),
                Offset(w * 0.42f, h * 0.45f),
                Offset(w * 0.54f, h * 0.30f)
            )
            waypoints.forEach { wp ->
                drawCircle(color = ResQAmberAccent, radius = 7f, center = wp)
            }

            // 4. Destination Safe Haven Beacon (St. Jude Sanctuary)
            val destPos = Offset(w * 0.65f, h * 0.16f)
            drawCircle(
                color = ResQSafeGreen,
                radius = 18f,
                center = destPos
            )
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = destPos
            )

            // 5. User GPS Position interpolated along the trajectory
            val clampedFraction = progressFraction.coerceIn(0f, 1f)
            // Interpolate user position along the curve
            val startY = h * 0.88f
            val endY = h * 0.16f
            val curY = startY + (endY - startY) * clampedFraction

            // approximate curved X based on progress
            val curX = when {
                clampedFraction < 0.35f -> w * (0.50f - (clampedFraction / 0.35f) * 0.08f)
                clampedFraction < 0.70f -> {
                    val localF = (clampedFraction - 0.35f) / 0.35f
                    w * (0.42f + localF * 0.12f)
                }
                else -> {
                    val localF = (clampedFraction - 0.70f) / 0.30f
                    w * (0.54f + localF * 0.11f)
                }
            }
            val userPos = Offset(curX, curY)

            // Pulsing beacon
            drawCircle(
                color = (if (clampedFraction >= 0.95f) ResQSafeGreen else ResQBluePrimary).copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = userPos
            )

            // Bearing orientation pointer
            rotate(degrees = headingDegrees, pivot = userPos) {
                val arrowPath = Path().apply {
                    moveTo(userPos.x, userPos.y - 18f)
                    lineTo(userPos.x + 10f, userPos.y + 12f)
                    lineTo(userPos.x, userPos.y + 6f)
                    lineTo(userPos.x - 10f, userPos.y + 12f)
                    close()
                }
                drawPath(
                    path = arrowPath,
                    color = if (clampedFraction >= 0.95f) ResQSafeGreen else ResQBluePrimary
                )
            }

            drawCircle(
                color = Color.White,
                radius = 5f,
                center = userPos
            )
        }
    }
}
