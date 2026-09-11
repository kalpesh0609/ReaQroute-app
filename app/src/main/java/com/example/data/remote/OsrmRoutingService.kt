/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /data/remote/OsrmRoutingService.kt
 *
 * PURPOSE & AIM:
 * Communicates with the Open Source Routing Machine (OSRM) HTTP API to calculate real-world
 * walking and driving routes based on OpenStreetMap road geometries.
 * Features an automatic offline fallback system: when connectivity fails or times out
 * during a disaster event, it injects deterministic, cached high-ground corridors so
 * citizens are never left without routing guidance.
 *
 * LINKINGS & CONNECTIONS:
 * - Classes & Objects: [OsrmRoutingService] (Networking & Geometry Parser).
 * - Models Consumed & Produced: [GeoPoint], [OsrmRouteData], [OsrmStepData].
 * - Consumed By: [ResQRouteRepository] to construct the live [MapUiState].
 */

package com.example.data.remote

import android.util.Log
import com.example.data.model.GeoPoint
import com.example.data.model.OsrmRouteData
import com.example.data.model.OsrmStepData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Service class handling HTTP requests to the OSRM routing daemon and parsing GeoJSON coordinates.
 *
 * @param okHttpClient HTTP client configured with aggressive 5-second timeouts for disaster resilience.
 */
class OsrmRoutingService(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "OsrmRoutingService"
        // Standard OpenStreetMap OSRM public demonstration router
        private const val OSRM_BASE_URL = "https://router.project-osrm.org/route/v1"
    }

    /**
     * Queries OSRM to generate both the recommended safe ridge route and the direct unsafe canal route.
     *
     * Aim:
     * Provides geometry data comparing the dry elevated ridge vs. low flooded basin.
     * Automatically falls back to local disaster corridors if offline.
     *
     * @param origin Starting GPS coordinate of the citizen.
     * @param shelter Destination safe haven shelter coordinates.
     * @param hazardNode Coordinates of the active culvert flood hazard.
     * @return Pair of [OsrmRouteData] where first is the safe corridor and second is the direct corridor.
     */
    suspend fun getEvacuationRoutes(
        origin: GeoPoint = GeoPoint(19.0545, 72.8285, 12.0, "Current Location"),
        shelter: GeoPoint = GeoPoint(19.0665, 72.8365, 32.0, "St. Jude Safe Haven"),
        hazardNode: GeoPoint = GeoPoint(19.0578, 72.8305, 2.0, "Culvert Node #104")
    ): Pair<OsrmRouteData, OsrmRouteData> = withContext(Dispatchers.IO) {
        try {
            // Attempt to query live OSRM endpoint for Safe Ridge Route (routed via elevated waypoint)
            val ridgeWaypoint = GeoPoint(19.0610, 72.8340, 32.0, "Ridge Road Spine")
            val liveSafeRoute = queryOsrm(
                points = listOf(origin, ridgeWaypoint, shelter),
                profile = "foot",
                routeName = "Route B • Ridge Road Spine",
                isSafe = true,
                elevationGain = 32,
                avoidedHazards = 2
            )

            // Direct route that passes low-lying basin
            val liveDirectRoute = queryOsrm(
                points = listOf(origin, hazardNode, shelter),
                profile = "driving",
                routeName = "Route A • Canal Road Expressway",
                isSafe = false,
                elevationGain = 2,
                avoidedHazards = 0
            )

            Pair(liveSafeRoute, liveDirectRoute)
        } catch (e: Exception) {
            // Disaster offline fallback mode
            Log.w(TAG, "OSRM remote request failed or offline (${e.message}). Engaging cached OSRM disaster geometry.", e)
            Pair(createFallbackSafeRidgeRoute(), createFallbackUnsafeRoute())
        }
    }

    /**
     * Executes HTTP GET against the OSRM route service and parses GeoJSON coordinates and turn maneuvers.
     */
    private fun queryOsrm(
        points: List<GeoPoint>,
        profile: String,
        routeName: String,
        isSafe: Boolean,
        elevationGain: Int,
        avoidedHazards: Int
    ): OsrmRouteData {
        // Format coordinates as longitude,latitude;longitude,latitude...
        val coordsStr = points.joinToString(";") { "${it.longitude},${it.latitude}" }
        val url = "$OSRM_BASE_URL/$profile/$coordsStr?overview=full&geometries=geojson&steps=true"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "ResQRoute-DisasterClient/1.0")
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("OSRM HTTP response code: ${response.code}")
        }

        val jsonBody = response.body?.string() ?: throw IllegalStateException("Empty body from OSRM")
        val root = JSONObject(jsonBody)

        val code = root.optString("code")
        if (code != "Ok") {
            throw IllegalStateException("OSRM error code: $code")
        }

        val routesArray = root.getJSONArray("routes")
        if (routesArray.length() == 0) {
            throw IllegalStateException("No routes returned by OSRM")
        }

        val routeObj = routesArray.getJSONObject(0)
        val distance = routeObj.optDouble("distance", 2600.0)
        val duration = routeObj.optDouble("duration", 840.0)

        // Parse GeoJSON coordinates: [[lon, lat], [lon, lat], ...]
        val geometry = routeObj.getJSONObject("geometry")
        val coordsArray = geometry.getJSONArray("coordinates")
        val geometryPoints = mutableListOf<GeoPoint>()
        for (i in 0 until coordsArray.length()) {
            val pt = coordsArray.getJSONArray(i)
            val lon = pt.getDouble(0)
            val lat = pt.getDouble(1)
            geometryPoints.add(GeoPoint(latitude = lat, longitude = lon))
        }

        // Parse steps / turn-by-turn guidance maneuvers
        val stepsList = mutableListOf<OsrmStepData>()
        val legsArray = routeObj.optJSONArray("legs")
        if (legsArray != null) {
            for (i in 0 until legsArray.length()) {
                val leg = legsArray.getJSONObject(i)
                val steps = leg.optJSONArray("steps")
                if (steps != null) {
                    for (j in 0 until steps.length()) {
                        val step = steps.getJSONObject(j)
                        val name = step.optString("name", "Unnamed Road")
                        val dist = step.optDouble("distance", 0.0)
                        val dur = step.optDouble("duration", 0.0)
                        val maneuver = step.optJSONObject("maneuver")
                        val maneuverType = maneuver?.optString("type", "turn") ?: "turn"
                        val instruction = maneuver?.optString("modifier", "Continue straight") ?: "Continue"

                        stepsList.add(
                            OsrmStepData(
                                name = if (name.isBlank()) "Ridge Approach" else name,
                                instruction = "$instruction on $name",
                                distanceMeters = dist,
                                durationSeconds = dur,
                                maneuverType = maneuverType
                            )
                        )
                    }
                }
            }
        }

        return OsrmRouteData(
            id = if (isSafe) "osrm-safe-ridge" else "osrm-unsafe-canal",
            name = routeName,
            isSafe = isSafe,
            distanceMeters = distance,
            durationSeconds = duration,
            elevationGainMeters = elevationGain,
            geometryPoints = if (geometryPoints.isNotEmpty()) geometryPoints else createFallbackSafeRidgeRoute().geometryPoints,
            steps = if (stepsList.isNotEmpty()) stepsList else createFallbackSafeRidgeRoute().steps,
            avoidedHazardsCount = avoidedHazards,
            summary = routeObj.optString("weight_name", "routability"),
            source = "OSRM Live Engine (router.project-osrm.org)"
        )
    }

    /**
     * Fallback safe corridor constructed using verified local Bandra Ridge GIS coordinates.
     */
    private fun createFallbackSafeRidgeRoute(): OsrmRouteData {
        val points = listOf(
            GeoPoint(19.0545, 72.8285, 12.0, "Current Location"),
            GeoPoint(19.0558, 72.8298, 16.0, "Turn onto Hill Road"),
            GeoPoint(19.0574, 72.8318, 22.0, "St. Anne High Street"),
            GeoPoint(19.0595, 72.8335, 28.0, "Bandra Ridge Ascent"),
            GeoPoint(19.0610, 72.8340, 32.0, "Ridge Spine High-Ground"),
            GeoPoint(19.0635, 72.8352, 32.0, "Dry Paved Walkway"),
            GeoPoint(19.0652, 72.8360, 32.0, "Safe Haven Approach"),
            GeoPoint(19.0665, 72.8365, 32.0, "St. Jude Safe Haven Gate")
        )

        val steps = listOf(
            OsrmStepData("Hill Road", "Depart northeast on Hill Road away from low basin", 220.0, 140.0, "depart"),
            OsrmStepData("St. Anne High Street", "Turn slight left onto St. Anne High Street", 350.0, 180.0, "turn"),
            OsrmStepData("Ridge Ascent Road", "Ascend along Ridge Road following high ground signs", 800.0, 320.0, "turn"),
            OsrmStepData("Ridge Spine", "Continue along dry paved spine (+32m elevation)", 900.0, 360.0, "continue"),
            OsrmStepData("Sanctuary Gate", "Arrive at St. Jude Safe Haven on right", 330.0, 120.0, "arrive")
        )

        return OsrmRouteData(
            id = "osrm-fallback-safe-ridge",
            name = "Route B • Ridge Road Spine",
            isSafe = true,
            distanceMeters = 2600.0,
            durationSeconds = 840.0,
            elevationGainMeters = 32,
            geometryPoints = points,
            steps = steps,
            avoidedHazardsCount = 2,
            summary = "Elevated Spine Bypass (+32m)",
            source = "OSRM Cached Offline Corridor (Disaster Fallback)"
        )
    }

    /**
     * Fallback unsafe corridor representing low-elevation Canal Road traversing the flooded culvert node.
     */
    private fun createFallbackUnsafeRoute(): OsrmRouteData {
        val points = listOf(
            GeoPoint(19.0545, 72.8285, 12.0, "Current Location"),
            GeoPoint(19.0552, 72.8290, 8.0, "Canal Feeder"),
            GeoPoint(19.0560, 72.8295, 4.0, "Culvert Inflow"),
            GeoPoint(19.0578, 72.8305, 2.0, "Culvert Node #104 (Flooded)"),
            GeoPoint(19.0600, 72.8320, 6.0, "Waterlogged Lowland"),
            GeoPoint(19.0630, 72.8345, 14.0, "Lower Ridge Base"),
            GeoPoint(19.0665, 72.8365, 32.0, "St. Jude Safe Haven")
        )

        val steps = listOf(
            OsrmStepData("Canal Road", "Head straight along Canal Road Low Basin", 600.0, 180.0, "depart"),
            OsrmStepData("Culvert Node #104", "DANGER: 48cm standing flood water across roadway", 400.0, 240.0, "danger"),
            OsrmStepData("Lower Basin Extension", "Submerged roadway with stalled vehicles", 600.0, 180.0, "continue")
        )

        return OsrmRouteData(
            id = "osrm-fallback-unsafe-canal",
            name = "Route A • Canal Road Expressway",
            isSafe = false,
            distanceMeters = 1600.0,
            durationSeconds = 480.0,
            elevationGainMeters = 2,
            geometryPoints = points,
            steps = steps,
            avoidedHazardsCount = 0,
            summary = "Direct Low Basin (48cm Flooded)",
            source = "OSRM Cached Offline Corridor (Disaster Fallback)"
        )
    }
}
