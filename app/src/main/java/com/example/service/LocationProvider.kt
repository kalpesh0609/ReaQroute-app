/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /service/LocationProvider.kt
 *
 * PURPOSE & AIM:
 * Pluggable location provider abstraction supporting:
 * 1. [LiveLocationProvider] - Real Android Fused GPS / Framework LocationManager.
 * 2. [SimulationLocationProvider] - Automated corridor progression for testing and drills.
 * 3. Future Phase 4 Providers (SMS dead-reckoning, Bluetooth Low Energy peer mesh triangulation).
 *
 * LINKINGS & CONNECTIONS:
 * - Interface: [LocationProvider].
 * - Consumed By: [ResQRouteRepository], [ResQRouteViewModel], [LiveGuidanceScreen].
 */

package com.example.service

import com.example.data.model.GeoPoint
import kotlinx.coroutines.flow.StateFlow

/**
 * Universal contract for location telemetry in ResQRoute.
 */
interface LocationProvider {
    /** Reactive stream of current latitude/longitude coordinates */
    val currentLocation: StateFlow<GeoPoint>

    /** Whether the provider is actively streaming position updates */
    val isTracking: StateFlow<Boolean>

    /** Estimated horizontal accuracy in meters (e.g., ±4.0m) */
    val accuracyMeters: StateFlow<Float?>

    /** Human-readable identifier for UI telemetry status */
    val providerName: String

    /** Starts receiving or generating location updates */
    fun start()

    /** Halts updates to preserve device battery during crises */
    fun stop()

    /** Manually injects or overrides position (used in drills and simulations) */
    fun setLocation(point: GeoPoint)
}
