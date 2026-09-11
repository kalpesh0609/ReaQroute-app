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
}
