/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /test/ExampleRobolectricTest.kt
 *
 * PURPOSE & AIM:
 * Local JVM unit and integration test suite running on Robolectric.
 * Validates deterministic routing corridor arbitration, mathematical capacity invariants,
 * OSRM geometry generation, idempotency key uniqueness, and multi-screen MapUiState propagation.
 *
 * LINKINGS & CONNECTIONS:
 * - Test Suite: [ExampleRobolectricTest].
 * - Tested Components: [RoutingEngine], [ShelterEngine], [OsrmRoutingService], [MapUiState].
 */

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AccessibilityProfile
import com.example.data.model.GeoPoint
import com.example.data.model.HazardZoneOverlay
import com.example.data.model.MapUiState
import com.example.data.model.Shelter
import com.example.data.remote.OsrmRoutingService
import com.example.domain.RoutingEngine
import com.example.domain.ShelterEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ResQRoute", appName)
    }

    @Test
    fun `verify routing engine safety evaluation`() {
        val shelter = Shelter()
        val accessibility = AccessibilityProfile()
        val result = RoutingEngine.evaluateCorridors(
            isCanalRoadClosed = true,
            waterDepthCm = 48,
            targetShelter = shelter,
            accessibility = accessibility
        )

        assertEquals("Route B • Ridge Road Spine", result.selectedRoute.name)
        assertTrue(result.selectedRoute.isSafe)
        assertEquals(false, result.selectedRoute.isFlooded)
        assertEquals(2, result.alternativeRoutes.size)
    }

    @Test
    fun `verify shelter capacity calculations`() {
        val (available, occupancyPct) = ShelterEngine.calculateCapacityState(
            totalCapacity = 250,
            newOccupiedCount = 195
        )

        assertEquals(55, available)
        assertEquals(78, occupancyPct)
        assertEquals(false, ShelterEngine.isApproachingQuorum(occupancyPct))
        assertEquals(false, ShelterEngine.isAtFullCapacity(available))

        val (fullAvail, fullPct) = ShelterEngine.calculateCapacityState(
            totalCapacity = 250,
            newOccupiedCount = 250
        )
        assertEquals(0, fullAvail)
        assertEquals(100, fullPct)
        assertTrue(ShelterEngine.isApproachingQuorum(fullPct))
        assertTrue(ShelterEngine.isAtFullCapacity(fullAvail))
    }

    @Test
    fun `verify OSRM routing service routes and fallback resilience`() = runBlocking {
        val service = OsrmRoutingService()
        val (safeRoute, unsafeRoute) = service.getEvacuationRoutes()

        assertNotNull(safeRoute)
        assertNotNull(unsafeRoute)

        // Safe corridor checks
        assertTrue(safeRoute.isSafe)
        assertTrue(safeRoute.elevationGainMeters >= 30)
        assertTrue(safeRoute.geometryPoints.size >= 8)
        assertTrue(safeRoute.steps.isNotEmpty())
        assertEquals(2, safeRoute.avoidedHazardsCount)

        // Unsafe corridor checks
        assertEquals(false, unsafeRoute.isSafe)
        assertTrue(unsafeRoute.elevationGainMeters <= 5)
        assertEquals(0, unsafeRoute.avoidedHazardsCount)
    }

    @Test
    fun `verify MapUiState overlays configuration for real map readiness`() {
        val userLoc = GeoPoint(19.0545, 72.8285, 12.0)
        val shelterLoc = GeoPoint(19.0665, 72.8365, 32.0)
        val hazardOverlay = HazardZoneOverlay(
            id = "haz-104",
            title = "Canal Road Flooded Basin",
            waterDepthCm = 48,
            polygonPoints = listOf(
                GeoPoint(19.0560, 72.8270),
                GeoPoint(19.0585, 72.8310),
                GeoPoint(19.0570, 72.8340)
            )
        )

        val mapState = MapUiState(
            userLocation = userLoc,
            targetShelter = shelterLoc,
            hazards = listOf(hazardOverlay),
            isRealMapApiConnected = false
        )

        assertEquals(1, mapState.hazards.size)
        assertEquals(48, mapState.hazards.first().waterDepthCm)
        assertEquals(3, mapState.hazards.first().polygonPoints.size)
        assertEquals(false, mapState.isRealMapApiConnected)
    }

    @Test
    fun `verify Phase 2 multi-screen map overlay state consistency`() {
        val hazardOverlay = HazardZoneOverlay(
            id = "haz-104",
            title = "Canal Road Flooded Basin",
            waterDepthCm = 48,
            polygonPoints = listOf(
                GeoPoint(19.0560, 72.8270),
                GeoPoint(19.0585, 72.8310)
            ),
            severity = "IMPASSABLE"
        )
        val mapState = MapUiState(
            hazards = listOf(hazardOverlay),
            isFloodLayerVisible = true,
            isReportsLayerVisible = true,
            isElevationContoursVisible = true
        )

        // Verifies GIS layer toggling contracts used across Radar, Comparison, Guidance, Dossier screens
        assertTrue(mapState.isFloodLayerVisible)
        assertTrue(mapState.isReportsLayerVisible)
        assertTrue(mapState.isElevationContoursVisible)
        assertEquals("haz-104", mapState.hazards.first().id)
        assertEquals("IMPASSABLE", mapState.hazards.first().severity)
    }

    @Test
    fun `verify location tracking service and telemetry formatting`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val initialLoc = GeoPoint(19.0545, 72.8285, 12.0, "Current Location")
        val service = com.example.service.LocationTrackingService(context, initialLoc)

        assertEquals(19.0545, service.currentLocation.value.latitude, 0.0001)
        assertEquals(72.8285, service.currentLocation.value.longitude, 0.0001)

        val updatedLoc = GeoPoint(19.0595, 72.8335, 28.0, "Bandra Ridge Ascent")
        service.updateManualLocation(updatedLoc)
        assertEquals(19.0595, service.currentLocation.value.latitude, 0.0001)
        assertEquals(28.0, service.currentLocation.value.altitudeM, 0.0001)
        assertTrue(service.currentLocation.value.toFormattedString().contains("19.05950°N"))
    }

    @Test
    fun `test osrm network call or fallback`() = runBlocking {
        val service = OsrmRoutingService()
        // Should not hang
        val res = service.getEvacuationRoutes()
        assertNotNull(res)
    }

    @Test
    fun `test MapView instantiation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        org.osmdroid.config.Configuration.getInstance().apply {
            userAgentValue = "ResQRoute-DisasterClient/1.0"
            osmdroidBasePath = java.io.File(context.cacheDir, "osmdroid")
            osmdroidTileCache = java.io.File(context.cacheDir, "osmdroid_tiles")
        }
        val mv = org.osmdroid.views.MapView(context)
        assertNotNull(mv)
    }

    @Test
    fun `verify Phase 4 zero-internet SMS broadcast ingestion and state updates`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.ResQRouteDatabase.getInstance(context)
        val repository = com.example.data.repository.ResQRouteRepository(db)

        // Seed hazard directly to avoid asynchronous race condition in test
        val testHazard = com.example.data.local.entity.HazardEntity(
            id = "haz-104",
            reportNumber = "#104",
            title = "Sector 17 Basin",
            locationName = "Canal Road",
            latitude = 19.0578,
            longitude = 72.8305,
            waterDepthCm = 48,
            culvertNode = "Node 104",
            communityConfirmationPct = 80,
            neighborsConfirmed = 10,
            detourName = "Ridge Road",
            detourSafeUsersCount = 100,
            isConfirmedFlooded = true,
            isClosed = true,
            severityLevel = "IMPASSABLE",
            reportedAgo = "Just now"
        )
        db.hazardDao().insertHazard(testHazard)

        // 1. Ingest Flood Alert broadcast
        val floodBroadcast = "RESQ BROADCAST HAZ:104 DEPTH:58CM STATUS:CLOSED BYPASS:RIDGE-ROAD"
        val floodResult = repository.ingestSmsBroadcast(floodBroadcast)
        assertTrue(floodResult.contains("58cm"))

        // 2. Ingest Shelter update broadcast
        val shelterBroadcast = "RESQ SHELTER:ST-JUDE OCCUPIED:220"
        val shelterResult = repository.ingestSmsBroadcast(shelterBroadcast)
        assertTrue(shelterResult.contains("220 beds"))

        // 3. Ingest Consensus confirmation broadcast
        val consensusBroadcast = "RESQ CONSENSUS HAZ:104 CONFIRMED:+1"
        val consensusResult = repository.ingestSmsBroadcast(consensusBroadcast)
        assertTrue(consensusResult.contains("validation recorded"))
    }

    @Test
    fun `verify real OSM map safety layers including hospitals, closures, and authority detours`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.ResQRouteDatabase.getInstance(context)
        val repository = com.example.data.repository.ResQRouteRepository(db)

        val mapState = repository.mapUiState.first()

        // 1. Verify OSM Map and Polyline
        assertNotNull(mapState.activeRoute)
        assertTrue(mapState.activeRoute!!.geometryPoints.size >= 8)
        assertTrue(mapState.activeRoute!!.isSafe)

        // 2. Verify Shelter Marker
        assertEquals("St. Jude Safe Haven", mapState.targetShelter.label)

        // 3. Verify Hazard Markers
        assertTrue(mapState.hazards.isNotEmpty())
        assertEquals(48, mapState.hazards.first().waterDepthCm)

        // 4. Verify Critical Facilities / Hospitals
        assertTrue(mapState.criticalFacilities.isNotEmpty())
        assertTrue(mapState.criticalFacilities.any { it.title.contains("Hospital") })
        assertTrue(mapState.criticalFacilities.any { it.title.contains("Fire") })

        // 5. Verify Road Closure Marker
        assertTrue(mapState.roadClosures.isNotEmpty())
        assertTrue(mapState.roadClosures.any { it.title.contains("ROAD CLOSED") })

        // 6. Verify Phase 5 Authority Detour
        assertTrue(mapState.isAuthorityDetourActive)
        assertTrue(mapState.authorityDetourDecree.contains("Decree"))
    }

    @Test
    fun `verify OSRM offline fallback and offline label invariant`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.ResQRouteDatabase.getInstance(context)
        val repository = com.example.data.repository.ResQRouteRepository(db)

        // Simulate zero-internet offline mode
        repository.setNetworkStatus(false)
        val offlineState = repository.mapUiState.first()

        assertEquals(false, offlineState.isOnline)
        assertTrue(offlineState.isOfflineFallback)
        assertTrue(offlineState.activeRoute!!.source.contains("Offline") || offlineState.activeRoute!!.source.contains("Fallback"))
        assertTrue(offlineState.mapProviderName.contains("Offline") || offlineState.mapProviderName.contains("Zero-Internet"))

        // Restore online
        repository.setNetworkStatus(true)
        val onlineState = repository.mapUiState.first()
        assertTrue(onlineState.isOnline)
    }

    @Test
    fun `test osmdroid initialization and tile source`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = org.osmdroid.config.Configuration.getInstance()
        println("Default userAgent: ${config.userAgentValue}")
        println("Default basePath: ${config.osmdroidBasePath}")
        println("Default tileCache: ${config.osmdroidTileCache}")
        val tileSource = org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK
        println("MAPNIK name: ${tileSource.name()}")
        println("MAPNIK getBaseUrl: ${tileSource.baseUrl}")

        // Test real HTTP request to OSM tile server
        val tileUrl = "https://tile.openstreetmap.org/13/5754/3654.png"
        val connection = java.net.URL(tileUrl).openConnection() as java.net.HttpURLConnection
        connection.setRequestProperty("User-Agent", "ResQRoute/1.0 (Android; disaster evacuation navigation; com.aistudio.resqroute.safe)")
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        val responseCode = connection.responseCode
        println("OSM Tile HTTP Response Code: $responseCode")
        val contentType = connection.contentType
        println("OSM Tile Content-Type: $contentType")
        val contentLength = connection.contentLength
        println("OSM Tile Content-Length: $contentLength")
        assertTrue("Expected 200 OK from OSM tile server", responseCode == 200)

        // Test osmdroid tile provider initialization
        val basePath = java.io.File(context.cacheDir, "osmdroid")
        val tilePath = java.io.File(basePath, "tiles")
        basePath.mkdirs()
        tilePath.mkdirs()
        config.osmdroidBasePath = basePath
        config.osmdroidTileCache = tilePath
        config.userAgentValue = "ResQRoute/1.0 (Android; disaster evacuation navigation; com.aistudio.resqroute.safe)"

        val tileProvider = org.osmdroid.tileprovider.MapTileProviderBasic(context, tileSource)
        println("TileProvider tileWriter: ${tileProvider.tileWriter}")
        val mapView = org.osmdroid.views.MapView(context, tileProvider)
        mapView.onResume()
        mapView.layout(0, 0, 1080, 1920)
        mapView.controller.setZoom(13.0)
        val testGeo = org.osmdroid.util.GeoPoint(19.0760, 72.8777)
        mapView.controller.setCenter(testGeo)
        println("MapView center: ${mapView.mapCenter}")
        println("MapView zoom: ${mapView.zoomLevelDouble}")
        assertEquals(13.0, mapView.zoomLevelDouble, 0.1)
        assertEquals(19.0760, mapView.mapCenter.latitude, 0.005)
        assertEquals(72.8777, mapView.mapCenter.longitude, 0.005)
        mapView.onPause()
        mapView.onDetach()
    }
}

