/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /service/SimulationLocationProvider.kt
 *
 * PURPOSE & AIM:
 * Simulated GPS provider that steps smoothly along the evacuation corridor coordinates.
 * Allows deterministic validation of turn-by-turn maneuvers, speed calculations,
 * hazard proximity alerts, and shelter gate arrival without requiring physical movement.
 *
 * LINKINGS & CONNECTIONS:
 * - Implements: [LocationProvider].
 * - Consumed By: [LiveGuidanceScreen], [ResQRouteViewModel].
 */

package com.example.service

import com.example.data.model.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Simulates citizen movement along a predefined list of evacuation corridor waypoints.
 */
class SimulationLocationProvider(
    private var waypoints: List<GeoPoint> = emptyList(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val stepDelayMs: Long = 3500L
) : LocationProvider {

    private val defaultOrigin = GeoPoint(19.0545, 72.8285, 12.0, "Current Location (Simulated)")

    private val _currentLocation = MutableStateFlow(waypoints.firstOrNull() ?: defaultOrigin)
    override val currentLocation: StateFlow<GeoPoint> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _accuracyMeters = MutableStateFlow<Float?>(2.5f)
    override val accuracyMeters: StateFlow<Float?> = _accuracyMeters.asStateFlow()

    override val providerName: String = "Simulation Engine"

    private var simulationJob: Job? = null
    private var currentIndex = 0

    var onWaypointReached: ((Int, GeoPoint) -> Unit)? = null

    fun setPath(points: List<GeoPoint>) {
        if (points.isNotEmpty()) {
            waypoints = points
            currentIndex = 0
            _currentLocation.value = points.first()
        }
    }

    override fun start() {
        if (_isTracking.value || waypoints.isEmpty()) return
        _isTracking.value = true
        simulationJob = scope.launch {
            while (_isTracking.value && currentIndex < waypoints.size) {
                val pt = waypoints[currentIndex]
                _currentLocation.value = pt
                onWaypointReached?.invoke(currentIndex, pt)
                delay(stepDelayMs)
                if (currentIndex < waypoints.size - 1) {
                    currentIndex++
                } else {
                    _isTracking.value = false
                    break
                }
            }
        }
    }

    override fun stop() {
        simulationJob?.cancel()
        simulationJob = null
        _isTracking.value = false
    }

    override fun setLocation(point: GeoPoint) {
        _currentLocation.value = point
    }

    fun stepForward(): Boolean {
        if (currentIndex < waypoints.size - 1) {
            currentIndex++
            val pt = waypoints[currentIndex]
            _currentLocation.value = pt
            onWaypointReached?.invoke(currentIndex, pt)
            return true
        }
        return false
    }

    fun reset() {
        stop()
        currentIndex = 0
        if (waypoints.isNotEmpty()) {
            _currentLocation.value = waypoints.first()
        }
    }
}
