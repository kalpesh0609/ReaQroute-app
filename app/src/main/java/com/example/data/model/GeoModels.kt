/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /data/model/GeoModels.kt
 *
 * PURPOSE & AIM:
 * Declares all geospatial data structures, coordinate abstractions, marker definitions,
 * Open Source Routing Machine (OSRM) payloads, and overarching map UI state models.
 * Serves as the primary contract between routing services, database overlays, and the
 * pluggable map placeholder architecture.
 *
 * LINKINGS & CONNECTIONS:
 * - Classes & Enums: [GeoPoint], [MarkerType], [MapMarker], [HazardZoneOverlay],
 *   [CitizenReportOverlay], [OsrmStepData], [OsrmRouteData], [MapUiState].
 * - Consumed By: [OsrmRoutingService], [ResQRouteRepository], [ResQRouteViewModel],
 *   [ResQMapPlaceholder], [MicroGisRadarCanvas], and all evacuation navigation screens.
 */

package com.example.data.model

/**
 * Standard Geographic coordinate representation with altitude and optional semantic label.
 *
 * @property latitude WGS84 decimal latitude.
 * @property longitude WGS84 decimal longitude.
 * @property altitudeM Altitude or elevation in meters above sea level.
 * @property label Semantic descriptive name (e.g., "Current Location", "St. Jude Safe Haven").
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double = 0.0,
    val label: String = ""
) {
    /** Formats coordinates into standard human-readable GPS notation. */
    fun toFormattedString(): String = String.format("%.5f°N, %.5f°E", latitude, longitude)
}

/**
 * Categorization of map marker types for custom pin styling and iconography.
 */
enum class MarkerType {
    USER_LOCATION,
    HAZARD_IMPASSABLE,
    CITIZEN_REPORT,
    SAFE_SHELTER,
    WAYPOINT
}

/**
 * Visual marker entity ready to be projected onto custom canvases or native MapView SDKs.
 *
 * @property id Unique identifier for hit-testing and selection callbacks.
 * @property title Primary label shown in callouts or bottom sheets.
 * @property snippet Detailed subtitle describing state or elevation.
 * @property position Geographic location of the marker.
 * @property type Category defining pin color, icon, and priority.
 */
data class MapMarker(
    val id: String,
    val title: String,
    val snippet: String,
    val position: GeoPoint,
    val type: MarkerType,
    val tag: String = "",
    val extraData: String = ""
)

/**
 * Geospatial polygon defining a red-zone hazard area (e.g. flooded basin or culvert overtopping).
 *
 * @property id Identifier of the corresponding municipal or citizen hazard node.
 * @property title Descriptive name of the flooded area.
 * @property waterDepthCm Depth of water in centimeters.
 * @property polygonPoints Array of geographic boundary coordinates forming the perimeter.
 * @property severity Qualitative risk classification ("IMPASSABLE", "CRITICAL").
 * @property reportedAgo Elapsed time since latest ground verification.
 */
data class HazardZoneOverlay(
    val id: String,
    val title: String,
    val waterDepthCm: Int,
    val polygonPoints: List<GeoPoint>,
    val severity: String = "IMPASSABLE",
    val reportedAgo: String = "6m ago"
)

/**
 * Citizen-reported incident point for crowdsourced verification.
 *
 * @property id Unique identifier for the incident.
 * @property reportNumber Display badge (e.g. "#104").
 * @property title Description of observed obstacle.
 * @property location Geographic coordinates of report.
 * @property confirmedCount Number of local neighbors who have concurred.
 * @property verificationPct Calculated consensus percentage.
 */
data class CitizenReportOverlay(
    val id: String,
    val reportNumber: String,
    val title: String,
    val location: GeoPoint,
    val confirmedCount: Int,
    val verificationPct: Int,
    val severity: String,
    val timestamp: String
)

/**
 * Individual turn-by-turn navigation maneuver instruction parsed from OSRM response.
 *
 * @property name Street or corridor name.
 * @property instruction Actionable guidance (e.g., "Turn left onto Ridge Road").
 * @property distanceMeters Distance of this maneuver segment in meters.
 * @property durationSeconds Estimated time to complete segment.
 * @property maneuverType OSRM maneuver identifier ("turn", "depart", "arrive").
 */
data class OsrmStepData(
    val name: String,
    val instruction: String,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val maneuverType: String = "turn"
)

/**
 * Complete evacuation route parsed from OSRM containing geometry polyline points and steps.
 *
 * @property id Unique route token.
 * @property name Corridor designation.
 * @property isSafe Flag indicating if corridor avoids all known flood basins.
 * @property distanceMeters Total transit distance in meters.
 * @property durationSeconds Estimated transit duration in seconds.
 * @property elevationGainMeters Total net elevation climbed.
 * @property geometryPoints Polyline coordinates for GIS rendering.
 * @property steps List of step-by-step turn instructions.
 */
data class OsrmRouteData(
    val id: String,
    val name: String,
    val isSafe: Boolean,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val elevationGainMeters: Int,
    val geometryPoints: List<GeoPoint>,
    val steps: List<OsrmStepData>,
    val avoidedHazardsCount: Int = 0,
    val summary: String = "",
    val source: String = "OSRM v5.24.0 (OpenStreetMap)"
) {
    /** Distance formatted in kilometers */
    val distanceKm: Double get() = distanceMeters / 1000.0
    /** Duration formatted in whole minutes */
    val durationMinutes: Int get() = (durationSeconds / 60.0).toInt().coerceAtLeast(1)
}

/**
 * Immutable UI state model consumed by map components across the app.
 *
 * Purpose: Decouples rendering layers from routing mechanisms. Can be bound directly
 * to custom Compose Canvas renderers or to native Google Maps / MapLibre Android SDKs.
 */
data class MapUiState(
    val userLocation: GeoPoint = GeoPoint(19.0545, 72.8285, 12.0, "Current Location"),
    val targetShelter: GeoPoint = GeoPoint(19.0665, 72.8365, 32.0, "St. Jude Safe Haven"),
    val activeRoute: OsrmRouteData? = null,
    val alternativeRoute: OsrmRouteData? = null,
    val hazards: List<HazardZoneOverlay> = emptyList(),
    val markers: List<MapMarker> = emptyList(),
    val reports: List<CitizenReportOverlay> = emptyList(),
    val isRealMapApiConnected: Boolean = false,
    val mapProviderName: String = "OSRM + OpenStreetMap GIS Layer",
    val zoomLevel: Float = 14.5f,
    val isFloodLayerVisible: Boolean = true,
    val isElevationContoursVisible: Boolean = true,
    val isReportsLayerVisible: Boolean = true
)
