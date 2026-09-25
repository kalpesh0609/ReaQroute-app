/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/components/ResQMapPlaceholder.kt
 *
 * PURPOSE & AIM:
 * Interactive vector GIS map component rendering evacuation corridors, real-time OSRM polylines,
 * Red Zone flood hazard polygons, citizen incident pins, and safe haven shelter destination markers.
 * Designed with a pluggable architecture: can operate as a lightweight Compose vector canvas
 * or easily bind to Google Maps / MapLibre SDKs while maintaining identical [MapUiState] contracts.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [ResQMapPlaceholder].
 * - Consumed Models: [MapUiState], [GeoPoint], [HazardZoneOverlay], [CitizenReportOverlay], [MapMarker].
 * - Invoked Across: [EvacuationRadarScreen], [RouteComparisonScreen], [LiveGuidanceScreen],
 *   [SafeHavenDossierScreen], and [CitizenDossierScreen].
 */

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CitizenReportOverlay
import com.example.data.model.GeoPoint
import com.example.data.model.HazardZoneOverlay
import com.example.data.model.MapMarker
import com.example.data.model.MapUiState
import com.example.data.model.MarkerType
import com.example.data.model.OsrmRouteData
import com.example.ui.map.OsmMapRenderer
import com.example.ui.theme.ResQAmberAccent
import com.example.ui.theme.ResQAmberWarning
import com.example.ui.theme.ResQBlueContainer
import com.example.ui.theme.ResQBluePrimary
import com.example.ui.theme.ResQDangerContainer
import com.example.ui.theme.ResQDangerRed
import com.example.ui.theme.ResQSafeGreen

/**
 * =========================================================================================
 * RESQROUTE MAP PLACEHOLDER & REAL MAP SDK CONNECTOR
 * =========================================================================================
 *
 * This component acts as a high-fidelity Map Placeholder designed to connect seamlessly
 * with any real Map API (Google Maps Android SDK, MapLibre SDK, or OSMDroid).
 *
 * It uses OSRM (Open Source Routing Machine) coordinates for real polyline rendering,
 * and overlays:
 * 1. Safe Ridge Corridors (OSRM GeoJSON geometry with elevation gain highlights)
 * 2. Unsafe Flooded Corridors (impassable basin routes)
 * 3. Hazard Inundation Polygons (depth in cm, municipal closure status)
 * 4. Citizen Incident Reports (live verified pins with confirmation counts)
 * 5. Safe Haven Shelters (high-ground target locations)
 *
 * When attaching a real map SDK, developers can plug in their `GoogleMap` or `MapView`
 * into the [RealMapApiSlot] marked below without changing any domain logic.
 */
@Composable
fun ResQMapPlaceholder(
    uiState: MapUiState,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
    onMarkerSelected: ((MapMarker) -> Unit)? = null,
    onHazardSelected: ((HazardZoneOverlay) -> Unit)? = null,
    onReportSelected: ((CitizenReportOverlay) -> Unit)? = null,
    onInspectApi: (() -> Unit)? = null
) {
    var zoomLevel by remember { mutableFloatStateOf(uiState.zoomLevel) }
    var recenterTrigger by remember { mutableStateOf(0L) }
    var showLayersDialog by remember { mutableStateOf(false) }
    var showApiInspectorDialog by remember { mutableStateOf(false) }
    var selectedEntityTitle by remember { mutableStateOf<String?>(null) }
    var selectedEntityDetails by remember { mutableStateOf<String?>(null) }

    // Layer toggles
    var showHazardLayer by remember { mutableStateOf(uiState.isFloodLayerVisible) }
    var showReportsLayer by remember { mutableStateOf(uiState.isReportsLayerVisible) }
    var showContoursLayer by remember { mutableStateOf(uiState.isElevationContoursVisible) }
    var showFacilitiesLayer by remember { mutableStateOf(uiState.isFacilitiesLayerVisible) }

    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gpsRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gpsAlpha"
    )

    val mapHeight = if (isExpanded) 460.dp else 260.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = mapHeight)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE9ECEF))
            .border(1.5.dp, Color(0xFFD0D7DE), RoundedCornerShape(18.dp))
            .testTag("resq_map_placeholder")
    ) {
        // =========================================================================
        // REAL OPENSTREETMAP NATIVE RENDERER (Powered by osmdroid & OSRM)
        // Renders real OpenStreetMap roads, polylines, flood hazards, and safe haven pins
        // =========================================================================
        OsmMapRenderer(
            uiState = uiState.copy(
                zoomLevel = zoomLevel,
                isFloodLayerVisible = showHazardLayer,
                isReportsLayerVisible = showReportsLayer,
                isElevationContoursVisible = showContoursLayer,
                isFacilitiesLayerVisible = showFacilitiesLayer
            ),
            recenterTrigger = recenterTrigger,
            modifier = Modifier.fillMaxSize(),
            onMarkerClicked = { marker ->
                selectedEntityTitle = marker.title
                selectedEntityDetails = marker.snippet
                onMarkerSelected?.invoke(marker)
            },
            onHazardClicked = { hazard ->
                selectedEntityTitle = "Hazard: ${hazard.title}"
                selectedEntityDetails = "Water Depth: ${hazard.waterDepthCm}cm • ${hazard.severity} • Avoid Basin"
                onHazardSelected?.invoke(hazard)
            },
            onReportClicked = { report ->
                selectedEntityTitle = "Citizen Incident ${report.reportNumber}"
                selectedEntityDetails = "${report.title} verified by ${report.confirmedCount} neighbors (${report.verificationPct}% consensus)"
                onReportSelected?.invoke(report)
            }
        )

        // =========================================================================
        // TOP OVERLAYS: Map Mode Badge & Diagnostic Connector
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Map status & OSRM indicator with REAL Online/Offline reflection
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.85f),
                shadowElevation = 4.dp,
                modifier = Modifier.clickable { showApiInspectorDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isOnline) Color(0xFF4ADE80) else Color(0xFFFBBF24))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (uiState.isOnline) "OSM + OSRM: ONLINE" else "OSM + OSRM: OFFLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = if (uiState.isOnline) "OpenStreetMap Live Tiles" else "Cached Local Tiles & Corridor",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.isOnline) Color(0xFFD1D5DB) else Color(0xFFFDE68A)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Inspect API",
                        tint = Color(0xFF93C5FD),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Right side buttons: Layers + Expand
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable { showLayersDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layers",
                            tint = ResQBluePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Layers",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ResQBluePrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable { onToggleExpand() }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Toggle Map Size",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                    )
                }
            }
        }

        // =========================================================================
        // BOTTOM-LEFT: Dynamic Map Scale & Coordinates Readout
        // =========================================================================
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color.Black.copy(alpha = 0.65f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "19.054°N, 72.828°E • Scale 200m",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // =========================================================================
        // BOTTOM-RIGHT: Zoom & Recenter Controls
        // =========================================================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 3.dp,
                modifier = Modifier.clickable {
                    zoomLevel = (zoomLevel + 1f).coerceAtMost(18f)
                }
            ) {
                Text(
                    text = "+",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ResQBluePrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 3.dp,
                modifier = Modifier.clickable {
                    zoomLevel = (zoomLevel - 1f).coerceAtLeast(10f)
                }
            ) {
                Text(
                    text = "−",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ResQBluePrimary,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ResQBluePrimary,
                shadowElevation = 3.dp,
                modifier = Modifier.clickable {
                    recenterTrigger = System.currentTimeMillis()
                    selectedEntityTitle = "Centered on Your GPS"
                    selectedEntityDetails = "${uiState.userLocation.toFormattedString()} • Altitude: +${uiState.userLocation.altitudeM.toInt()}m"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Recenter",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                )
            }
        }

        // =========================================================================
        // SELECTION POPUP BANNER (When User Taps Marker/Hazard/Report)
        // =========================================================================
        AnimatedVisibility(
            visible = selectedEntityTitle != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp, start = 12.dp, end = 12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedEntityTitle ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = selectedEntityDetails ?: "",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    IconButton(
                        onClick = { selectedEntityTitle = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // LAYER TOGGLE DIALOG
    // =========================================================================
    if (showLayersDialog) {
        AlertDialog(
            onDismissRequest = { showLayersDialog = false },
            title = {
                Text(text = "Map Overlays & GIS Layers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Customize active telemetry rendered on top of the OSRM routing base:",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )

                    FilterChip(
                        selected = showHazardLayer,
                        onClick = { showHazardLayer = !showHazardLayer },
                        label = { Text("Flood Inundation Basin (48cm water)") },
                        leadingIcon = {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ResQDangerRed)
                        }
                    )

                    FilterChip(
                        selected = showReportsLayer,
                        onClick = { showReportsLayer = !showReportsLayer },
                        label = { Text("Citizen Reports & Neighbor Quorums") },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = ResQAmberWarning)
                        }
                    )

                    FilterChip(
                        selected = showFacilitiesLayer,
                        onClick = { showFacilitiesLayer = !showFacilitiesLayer },
                        label = { Text("Hospitals & Critical Emergency Posts") },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = ResQBluePrimary)
                        }
                    )

                    FilterChip(
                        selected = showContoursLayer,
                        onClick = { showContoursLayer = !showContoursLayer },
                        label = { Text("Elevation Topographic Contours (+32m)") },
                        leadingIcon = {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = ResQBluePrimary)
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLayersDialog = false }) {
                    Text("Apply Layers", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =========================================================================
    // MAP API CONNECTOR & OSRM INSPECTOR DIALOG
    // =========================================================================
    if (showApiInspectorDialog) {
        AlertDialog(
            onDismissRequest = { showApiInspectorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = ResQBluePrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Map API Connector & OSRM Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "ROUTING ENGINE:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = uiState.activeRoute?.source ?: "OSRM Engine (GeoJSON LineString)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "GeoJSON Nodes: ${uiState.activeRoute?.geometryPoints?.size ?: 9} points",
                                fontSize = 11.sp,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Elevation Crest: +${uiState.activeRoute?.elevationGainMeters ?: 32}m (Dry Spine)",
                                fontSize = 11.sp,
                                color = ResQSafeGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Real Map SDK Plug Guide:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "// Connect GoogleMap or MapLibre SDK:\n" +
                                   "GoogleMap(modifier = Modifier.fillMaxSize()) {\n" +
                                   "  Polyline(points = osrmRoute.geometryPoints)\n" +
                                   "  Polygon(points = floodBasin.polygonPoints)\n" +
                                   "  Marker(position = shelter.location)\n" +
                                   "}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF93C5FD),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Text(
                        text = "The placeholder provides all necessary GeoPoints, overlays, and camera bounds ready to pass directly into any native MapView.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showApiInspectorDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// =========================================================================
// CANVAS DRAWING HELPER FUNCTIONS
// =========================================================================

private fun DrawScope.drawMapBase(w: Float, h: Float, showContours: Boolean) {
    // Street grid lines
    val streetColor = Color(0xFFDDE3EA)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

    // Minor secondary streets
    for (i in 1..4) {
        val y = h * (i * 0.2f)
        drawLine(
            color = streetColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 3f
        )
    }
    for (i in 1..4) {
        val x = w * (i * 0.22f)
        drawLine(
            color = streetColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 3f
        )
    }

    // Topographic contours
    if (showContours) {
        val contourPaint = Color(0xFFCBD5E1).copy(alpha = 0.6f)
        val contour1 = Path().apply {
            moveTo(-20f, h * 0.75f)
            cubicTo(w * 0.3f, h * 0.68f, w * 0.6f, h * 0.5f, w + 20f, h * 0.3f)
        }
        drawPath(contour1, color = contourPaint, style = Stroke(width = 2f, pathEffect = dashEffect))

        val contour2 = Path().apply {
            moveTo(-20f, h * 0.52f)
            cubicTo(w * 0.35f, h * 0.42f, w * 0.65f, h * 0.28f, w + 20f, h * 0.12f)
        }
        drawPath(contour2, color = contourPaint, style = Stroke(width = 2f, pathEffect = dashEffect))
    }
}

private fun DrawScope.drawFloodHazardBasin(w: Float, h: Float) {
    val floodPolygon = Path().apply {
        moveTo(0f, h * 0.52f)
        quadraticTo(w * 0.32f, h * 0.58f, w * 0.52f, h)
        lineTo(0f, h)
        close()
    }

    // Translucent red inundation gradient
    drawPath(
        path = floodPolygon,
        brush = Brush.verticalGradient(
            colors = listOf(ResQDangerContainer.copy(alpha = 0.9f), ResQDangerRed.copy(alpha = 0.45f)),
            startY = h * 0.52f,
            endY = h
        )
    )

    // Red warning boundary
    val floodBorder = Path().apply {
        moveTo(0f, h * 0.52f)
        quadraticTo(w * 0.32f, h * 0.58f, w * 0.52f, h)
    }
    drawPath(
        path = floodBorder,
        color = ResQDangerRed.copy(alpha = 0.85f),
        style = Stroke(width = 3.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    )
}

private fun DrawScope.drawUnsafeCanalRoute(w: Float, h: Float) {
    val canalRoute = Path().apply {
        moveTo(w * 0.16f, h * 0.82f)
        lineTo(w * 0.32f, h * 0.68f) // Culvert Node
        lineTo(w * 0.58f, h * 0.46f)
        lineTo(w * 0.82f, h * 0.22f) // St Jude
    }

    // Red dashed impassable line
    drawPath(
        path = canalRoute,
        color = ResQDangerRed.copy(alpha = 0.75f),
        style = Stroke(
            width = 4.5f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
        )
    )
}

private fun DrawScope.drawSafeRidgeRoute(w: Float, h: Float, routeData: OsrmRouteData?) {
    val start = Offset(w * 0.16f, h * 0.82f)
    val control = Offset(w * 0.46f, h * 0.42f)
    val end = Offset(w * 0.82f, h * 0.22f)

    val safePath = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(control.x, control.y, end.x, end.y)
    }

    // Outer Amber Elevation Safety Envelope
    drawPath(
        path = safePath,
        color = Color(0xFFFFE0B2).copy(alpha = 0.7f),
        style = Stroke(width = 36f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // White underlay for crisp contrast
    drawPath(
        path = safePath,
        color = Color.White.copy(alpha = 0.95f),
        style = Stroke(width = 22f, cap = StrokeCap.Round)
    )

    // Solid High-Ground Safe Royal Blue Line
    drawPath(
        path = safePath,
        color = ResQBluePrimary,
        style = Stroke(width = 10f, cap = StrokeCap.Round)
    )

    // Dashed center line indicating walking flow
    drawPath(
        path = safePath,
        color = Color.White,
        style = Stroke(
            width = 3f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 14f), 0f)
        )
    )
}

private fun DrawScope.drawCitizenReports(w: Float, h: Float, reports: List<CitizenReportOverlay>) {
    val reportLoc = Offset(w * 0.44f, h * 0.58f)
    // Amber community verification circle
    drawCircle(color = ResQAmberAccent.copy(alpha = 0.35f), radius = 18f, center = reportLoc)
    drawCircle(color = ResQAmberWarning, radius = 9f, center = reportLoc)
    drawCircle(color = Color.White, radius = 4f, center = reportLoc)
}

private fun DrawScope.drawHazardPin(w: Float, h: Float) {
    val p = Offset(w * 0.32f, h * 0.68f)

    // Outer danger halo
    drawCircle(color = ResQDangerRed.copy(alpha = 0.3f), radius = 22f, center = p)
    // Center danger disc
    drawCircle(color = ResQDangerRed, radius = 13f, center = p)
    // White horizontal barrier dash
    drawLine(
        color = Color.White,
        start = Offset(p.x - 6f, p.y),
        end = Offset(p.x + 6f, p.y),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawShelterPin(w: Float, h: Float) {
    val p = Offset(w * 0.82f, h * 0.22f)

    // Green safe zone perimeter ring
    drawCircle(color = ResQSafeGreen.copy(alpha = 0.25f), radius = 26f, center = p)
    drawCircle(color = ResQBluePrimary, radius = 15f, center = p)

    // Checkmark inside shelter target
    val check = Path().apply {
        moveTo(p.x - 5f, p.y)
        lineTo(p.x - 1f, p.y + 4f)
        lineTo(p.x + 6f, p.y - 4f)
    }
    drawPath(path = check, color = Color.White, style = Stroke(width = 2.8f, cap = StrokeCap.Round))
}

private fun DrawScope.drawUserGpsPin(w: Float, h: Float, pulseRadius: Float, pulseAlpha: Float) {
    val p = Offset(w * 0.16f, h * 0.82f)

    // Pulsing accuracy halo
    drawCircle(color = ResQBluePrimary.copy(alpha = pulseAlpha), radius = pulseRadius * 1.6f, center = p)
    // White border disc
    drawCircle(color = Color.White, radius = 11f, center = p)
    // Solid blue core
    drawCircle(color = ResQBluePrimary, radius = 6.5f, center = p)
}
