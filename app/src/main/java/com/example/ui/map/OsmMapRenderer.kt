/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/map/OsmMapRenderer.kt
 *
 * PURPOSE & AIM:
 * Real interactive OpenStreetMap (OSM) map renderer powered by native Android osmdroid.
 * Renders real streets, roads, topographic geometries, live OSRM evacuation corridor polylines,
 * flood hazard inundation polygons, citizen incident pins, shelter destination markers,
 * hospitals/critical emergency facilities, road closure barriers, and verified Phase 5 authority detours.
 *
 * LINKINGS & CONNECTIONS:
 * - Composable: [OsmMapRenderer].
 * - Consumed By: [ResQMapPlaceholder], [LiveGuidanceScreen], [EvacuationRadarScreen].
 * - Consumes: [MapUiState], [GeoPoint], [OsrmRouteData], [HazardZoneOverlay], [MapMarker].
 * - Map Engine: [org.osmdroid.views.MapView] with [TileSourceFactory.MAPNIK].
 * - Attribution: "© OpenStreetMap contributors" rendered in high visibility.
 */

package com.example.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.preference.PreferenceManager
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.CitizenReportOverlay
import com.example.data.model.GeoPoint
import com.example.data.model.HazardZoneOverlay
import com.example.data.model.MapMarker
import com.example.data.model.MapUiState
import com.example.data.model.MarkerType
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import java.io.File

private const val TAG = "OsmMapRenderer"

/**
 * Cache for vector marker drawables to avoid expensive Bitmap allocations on every recomposition.
 */
private object MarkerIconCache {
    private var hazardIcon: BitmapDrawable? = null
    private var reportIcon: BitmapDrawable? = null
    private var shelterIcon: BitmapDrawable? = null
    private var hospitalIcon: BitmapDrawable? = null
    private var fireStationIcon: BitmapDrawable? = null
    private var roadClosureIcon: BitmapDrawable? = null
    private var authorityDetourIcon: BitmapDrawable? = null
    private var userLocationIcon: BitmapDrawable? = null

    fun getHazardIcon(context: Context): BitmapDrawable {
        return hazardIcon ?: createMarkerIcon(context, 0xFFBA1A1A.toInt(), "!").also { hazardIcon = it }
    }

    fun getReportIcon(context: Context): BitmapDrawable {
        return reportIcon ?: createMarkerIcon(context, 0xFFFF8F00.toInt(), "#").also { reportIcon = it }
    }

    fun getShelterIcon(context: Context): BitmapDrawable {
        return shelterIcon ?: createMarkerIcon(context, 0xFF059669.toInt(), "H").also { shelterIcon = it }
    }

    fun getHospitalIcon(context: Context): BitmapDrawable {
        return hospitalIcon ?: createMarkerIcon(context, 0xFF0284C7.toInt(), "+").also { hospitalIcon = it }
    }

    fun getFireStationIcon(context: Context): BitmapDrawable {
        return fireStationIcon ?: createMarkerIcon(context, 0xFFEA580C.toInt(), "F").also { fireStationIcon = it }
    }

    fun getRoadClosureIcon(context: Context): BitmapDrawable {
        return roadClosureIcon ?: createMarkerIcon(context, 0xFF991B1B.toInt(), "X").also { roadClosureIcon = it }
    }

    fun getAuthorityDetourIcon(context: Context): BitmapDrawable {
        return authorityDetourIcon ?: createMarkerIcon(context, 0xFF1E3A8A.toInt(), "A").also { authorityDetourIcon = it }
    }

    fun getUserLocationIcon(context: Context): BitmapDrawable {
        return userLocationIcon ?: createUserLocationIconInternal(context).also { userLocationIcon = it }
    }

    private fun createMarkerIcon(context: Context, bgColor: Int, label: String): BitmapDrawable {
        val sizePx = (34 * context.resources.displayMetrics.density).toInt().coerceAtLeast(32)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3 * context.resources.displayMetrics.density
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 14 * context.resources.displayMetrics.density
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val radius = sizePx / 2f - (2 * context.resources.displayMetrics.density)
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, radius, fillPaint)
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, radius, strokePaint)

        val yPos = (canvas.height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(label, sizePx / 2f, yPos, textPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    private fun createUserLocationIconInternal(context: Context): BitmapDrawable {
        val sizePx = (28 * context.resources.displayMetrics.density).toInt().coerceAtLeast(24)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF005EB2.toInt()
            style = Paint.Style.FILL
        }

        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 2f, whitePaint)
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 7f, bluePaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}

/**
 * Native OpenStreetMap interactive map renderer.
 */
@Composable
fun OsmMapRenderer(
    uiState: MapUiState,
    modifier: Modifier = Modifier,
    isFollowUserEnabled: Boolean = false,
    recenterTrigger: Long = 0L,
    onMarkerClicked: ((MapMarker) -> Unit)? = null,
    onHazardClicked: ((HazardZoneOverlay) -> Unit)? = null,
    onReportClicked: ((CitizenReportOverlay) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Initialize osmdroid configuration safely once
    remember {
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            Configuration.getInstance().load(context, prefs)
            Configuration.getInstance().userAgentValue = "ResQRoute/1.0 (Android; disaster evacuation navigation; com.aistudio.resqroute.safe)"
            val basePath = File(context.cacheDir, "osmdroid")
            val tilePath = File(basePath, "tiles")
            basePath.mkdirs()
            tilePath.mkdirs()
            Configuration.getInstance().osmdroidBasePath = basePath
            Configuration.getInstance().osmdroidTileCache = tilePath
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize osmdroid config", e)
        }
    }

    // Bind MapView lifecycle to the Compose LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef.value?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef.value?.onPause()
                Lifecycle.Event.ON_DESTROY -> {
                    mapViewRef.value?.onPause()
                    mapViewRef.value?.onDetach()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                mapViewRef.value?.onPause()
                mapViewRef.value?.onDetach()
            } catch (e: Exception) {
                Log.w(TAG, "Error disposing MapView", e)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Native OpenStreetMap MapView Container with clean Compose lifecycle
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewRef.value = this
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setDestroyMode(false)
                    isTilesScaledToDpi = true
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

                    val initialCenter = OsmGeoPoint(uiState.userLocation.latitude, uiState.userLocation.longitude)
                    controller.setZoom(uiState.zoomLevel.toDouble())
                    controller.setCenter(initialCenter)

                    // Guarantee valid center and zoom calculation once the view dimensions are laid out
                    addOnFirstLayoutListener { _, _, _, _, _ ->
                        controller.setZoom(uiState.zoomLevel.toDouble())
                        controller.setCenter(OsmGeoPoint(uiState.userLocation.latitude, uiState.userLocation.longitude))
                        postInvalidate()
                    }

                    // Wake up osmdroid tile downloader threads immediately
                    onResume()
                }
            },
            update = { mapView ->
                mapViewRef.value = mapView

                // Apply zoom level from state
                if (mapView.zoomLevelDouble != uiState.zoomLevel.toDouble() && uiState.zoomLevel >= 10f) {
                    mapView.controller.setZoom(uiState.zoomLevel.toDouble())
                }

                // Handle explicit recenter request
                if (recenterTrigger > 0L) {
                    val userGeo = OsmGeoPoint(uiState.userLocation.latitude, uiState.userLocation.longitude)
                    mapView.controller.animateTo(userGeo)
                }

                updateMapOverlays(
                    context = mapView.context,
                    mapView = mapView,
                    uiState = uiState,
                    isFollowUserEnabled = isFollowUserEnabled,
                    onMarkerClicked = onMarkerClicked,
                    onHazardClicked = onHazardClicked,
                    onReportClicked = onReportClicked
                )
            },
            onRelease = { mapView ->
                try {
                    mapView.onPause()
                    mapView.onDetach()
                } catch (e: Exception) {
                    Log.w(TAG, "Error detaching MapView", e)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("real_osm_map_view")
        )

        // Mandatory OpenStreetMap Attribution Notice
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
            color = Color.White.copy(alpha = 0.88f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
        ) {
            Text(
                text = "© OpenStreetMap contributors",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Synchronizes native osmdroid overlays (polylines, polygons, markers) with domain [MapUiState].
 */
private fun updateMapOverlays(
    context: Context,
    mapView: MapView,
    uiState: MapUiState,
    isFollowUserEnabled: Boolean,
    onMarkerClicked: ((MapMarker) -> Unit)?,
    onHazardClicked: ((HazardZoneOverlay) -> Unit)?,
    onReportClicked: ((CitizenReportOverlay) -> Unit)?
) {
    try {
        mapView.overlays.clear()

        // 1. Draw Active OSRM Safe Route Polyline following real road geometries
        uiState.activeRoute?.let { route ->
            if (route.geometryPoints.isNotEmpty()) {
                val polyline = Polyline(mapView).apply {
                    outlinePaint.color = if (uiState.isAuthorityDetourActive) {
                        0xFF0284C7.toInt() // Verified detour deep blue-sky
                    } else {
                        0xFF005EB2.toInt() // ResQ Standard Royal Blue
                    }
                    outlinePaint.strokeWidth = 14f
                    outlinePaint.strokeCap = Paint.Cap.ROUND
                    outlinePaint.strokeJoin = Paint.Join.ROUND
                    title = "${route.name} (${String.format("%.1f", route.distanceKm)} km)"
                    snippet = "High-Ground Elevation: +${route.elevationGainMeters}m • ${route.source}"
                }

                val osmPoints = route.geometryPoints.map { OsmGeoPoint(it.latitude, it.longitude) }
                polyline.setPoints(osmPoints)
                mapView.overlays.add(polyline)
            }
        }

        // 2. Draw Impassable Flooded Route Alternative in Red (if present and flood layer active)
        if (uiState.isFloodLayerVisible) {
            uiState.alternativeRoute?.let { altRoute ->
                if (altRoute.geometryPoints.isNotEmpty()) {
                    val altPolyline = Polyline(mapView).apply {
                        outlinePaint.color = 0xAA93000A.toInt() // Danger Red
                        outlinePaint.strokeWidth = 8f
                        outlinePaint.strokeCap = Paint.Cap.ROUND
                        title = "UNSAFE: ${altRoute.name}"
                        snippet = "Avoid: Submerged low basin (48cm water)"
                    }
                    altPolyline.setPoints(altRoute.geometryPoints.map { OsmGeoPoint(it.latitude, it.longitude) })
                    mapView.overlays.add(altPolyline)
                }
            }
        }

        // 3. Draw Red-Zone Flood Hazard Polygons & Hazard Markers
        if (uiState.isFloodLayerVisible) {
            uiState.hazards.forEach { hazard ->
                if (hazard.polygonPoints.size >= 3) {
                    val polygon = Polygon(mapView).apply {
                        fillPaint.color = 0x38BA1A1A // Semi-transparent flood red
                        outlinePaint.color = 0xFFBA1A1A.toInt() // Solid red boundary
                        outlinePaint.strokeWidth = 4f
                        title = "FLOOD HAZARD: ${hazard.title}"
                        snippet = "Depth: ${hazard.waterDepthCm} cm (${hazard.severity})"
                        points = hazard.polygonPoints.map { OsmGeoPoint(it.latitude, it.longitude) }
                    }
                    polygon.setOnClickListener { _, _, _ ->
                        onHazardClicked?.invoke(hazard)
                        true
                    }
                    mapView.overlays.add(polygon)
                }

                // Hazard Warning Pin
                val hazardCenter = hazard.polygonPoints.firstOrNull() ?: GeoPoint(19.0578, 72.8305)
                val hazardMarker = Marker(mapView).apply {
                    position = OsmGeoPoint(hazardCenter.latitude, hazardCenter.longitude)
                    title = "Hazard #104: ${hazard.title}"
                    snippet = "Water Depth: ${hazard.waterDepthCm}cm • IMPASSABLE"
                    icon = MarkerIconCache.getHazardIcon(context)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                hazardMarker.setOnMarkerClickListener { _, _ ->
                    onHazardClicked?.invoke(hazard)
                    true
                }
                mapView.overlays.add(hazardMarker)
            }
        }

        // 4. Draw Citizen Incident Report Pins (Phase 4)
        if (uiState.isReportsLayerVisible) {
            uiState.reports.forEach { report ->
                val reportMarker = Marker(mapView).apply {
                    position = OsmGeoPoint(report.location.latitude, report.location.longitude)
                    title = "Incident ${report.reportNumber}: ${report.title}"
                    snippet = "Verified by ${report.confirmedCount} neighbors (${report.verificationPct}% consensus)"
                    icon = MarkerIconCache.getReportIcon(context)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                reportMarker.setOnMarkerClickListener { _, _ ->
                    onReportClicked?.invoke(report)
                    true
                }
                mapView.overlays.add(reportMarker)
            }
        }

        // 5. Draw Road Closures
        uiState.roadClosures.forEach { closure ->
            val closureMarker = Marker(mapView).apply {
                position = OsmGeoPoint(closure.position.latitude, closure.position.longitude)
                title = closure.title
                snippet = closure.snippet
                icon = MarkerIconCache.getRoadClosureIcon(context)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            closureMarker.setOnMarkerClickListener { _, _ ->
                onMarkerClicked?.invoke(closure)
                true
            }
            mapView.overlays.add(closureMarker)
        }

        // 6. Draw Hospitals & Critical Facilities
        if (uiState.isFacilitiesLayerVisible) {
            uiState.criticalFacilities.forEach { facility ->
                val facilityMarker = Marker(mapView).apply {
                    position = OsmGeoPoint(facility.position.latitude, facility.position.longitude)
                    title = facility.title
                    snippet = facility.snippet
                    icon = when (facility.type) {
                        MarkerType.FIRE_STATION -> MarkerIconCache.getFireStationIcon(context)
                        else -> MarkerIconCache.getHospitalIcon(context)
                    }
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                facilityMarker.setOnMarkerClickListener { _, _ ->
                    onMarkerClicked?.invoke(facility)
                    true
                }
                mapView.overlays.add(facilityMarker)
            }
        }

        // 7. Phase 5 Verified Authority Detour Pin / Corridor Marker
        if (uiState.isAuthorityDetourActive) {
            val detourMarker = Marker(mapView).apply {
                position = OsmGeoPoint(19.0610, 72.8340)
                title = "VERIFIED AUTHORITY DETOUR"
                snippet = uiState.authorityDetourDecree.ifBlank { "Decree #MCGM-2026: Bypass Canal Rd via Ridge Spine" }
                icon = MarkerIconCache.getAuthorityDetourIcon(context)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            detourMarker.setOnMarkerClickListener { _, _ ->
                onMarkerClicked?.invoke(
                    MapMarker(
                        id = "detour-decree",
                        title = "VERIFIED AUTHORITY DETOUR",
                        snippet = uiState.authorityDetourDecree,
                        position = GeoPoint(19.0610, 72.8340),
                        type = MarkerType.AUTHORITY_DETOUR
                    )
                )
                true
            }
            mapView.overlays.add(detourMarker)
        }

        // 8. Safe Haven Shelter Destination Marker
        val shelterMarker = Marker(mapView).apply {
            position = OsmGeoPoint(uiState.targetShelter.latitude, uiState.targetShelter.longitude)
            title = uiState.targetShelter.label.ifBlank { "St. Jude Safe Haven" }
            snippet = "Safe Haven Sanctuary • High Ground +32m • Beds Ready"
            icon = MarkerIconCache.getShelterIcon(context)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        shelterMarker.setOnMarkerClickListener { _, _ ->
            onMarkerClicked?.invoke(
                MapMarker(
                    id = "target-shelter",
                    title = uiState.targetShelter.label.ifBlank { "St. Jude Safe Haven" },
                    snippet = "Safe Haven Sanctuary • High Ground +32m • Beds Ready",
                    position = uiState.targetShelter,
                    type = MarkerType.SAFE_SHELTER
                )
            )
            true
        }
        mapView.overlays.add(shelterMarker)

        // 9. Current User GPS Location Marker
        val userGeo = OsmGeoPoint(uiState.userLocation.latitude, uiState.userLocation.longitude)
        val userMarker = Marker(mapView).apply {
            position = userGeo
            title = "Your Location"
            snippet = "${uiState.userLocation.toFormattedString()} (Live GPS / Evacuation Corridor)"
            icon = MarkerIconCache.getUserLocationIcon(context)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
        mapView.overlays.add(userMarker)

        // 10. Navigation Camera Handling
        if (isFollowUserEnabled) {
            mapView.controller.animateTo(userGeo)
        }

        mapView.invalidate()
    } catch (e: Exception) {
        Log.w(TAG, "Error updating map overlays", e)
    }
}
