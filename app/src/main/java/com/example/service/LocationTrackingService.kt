/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /service/LocationTrackingService.kt
 *
 * PURPOSE & AIM:
 * Core location tracking and geospatial telemetry service for disaster evacuation.
 * Combines Google Play Services FusedLocationProviderClient with an offline-resilient
 * Android framework LocationManager fallback to ensure uninterrupted position telemetry
 * during cellular blackouts and infrastructure collapses.
 *
 * LINKINGS & CONNECTIONS:
 * - Class: [LocationTrackingService].
 * - Consumed By: [ResQRouteRepository], [ResQRouteViewModel], [LiveGuidanceScreen], [EvacuationRadarScreen].
 * - Platform Providers: [FusedLocationProviderClient], [LocationManager].
 * - Emits: [currentLocation] StateFlow of [GeoPoint], [isTracking] StateFlow, [gpsAccuracyMeters] StateFlow.
 */

package com.example.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.GeoPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages high-precision GPS and coarse location tracking for active evacuation corridors.
 */
class LocationTrackingService(
    private val context: Context,
    initialLocation: GeoPoint = GeoPoint(19.0545, 72.8285, 12.0, "Current Location")
) : LocationProvider {
    companion object {
        private const val TAG = "LocationTrackingService"
        private const val UPDATE_INTERVAL_MS = 3000L
        private const val FASTEST_INTERVAL_MS = 1500L
    }

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    // Reactive State Flows
    private val _currentLocation = MutableStateFlow(initialLocation)
    override val currentLocation: StateFlow<GeoPoint> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _gpsAccuracyMeters = MutableStateFlow<Float?>(4.0f)
    override val accuracyMeters: StateFlow<Float?> = _gpsAccuracyMeters.asStateFlow()
    val gpsAccuracyMeters: StateFlow<Float?> = _gpsAccuracyMeters.asStateFlow()

    override val providerName: String = "Fused Location + Android GPS"

    private val _isGpsHardwareLocked = MutableStateFlow(false)
    val isGpsHardwareLocked: StateFlow<Boolean> = _isGpsHardwareLocked.asStateFlow()

    override fun start() = startTracking()
    override fun stop() = stopTracking()
    override fun setLocation(point: GeoPoint) = updateManualLocation(point)

    // Fused Location Callback
    private val fusedLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                processNewAndroidLocation(loc, source = "FusedLocation")
            }
        }
    }

    // Framework Location Listener Fallback
    private val frameworkLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            processNewAndroidLocation(location, source = "FrameworkGPS")
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {
            _isGpsHardwareLocked.value = true
        }
        override fun onProviderDisabled(provider: String) {
            _isGpsHardwareLocked.value = false
        }
    }

    /**
     * Checks whether location permissions are granted.
     */
    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    /**
     * Starts continuous high-accuracy location tracking.
     */
    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Cannot start tracking: location permissions missing")
            return
        }

        if (_isTracking.value) return

        try {
            // Build high-accuracy disaster request
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                UPDATE_INTERVAL_MS
            ).apply {
                setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
                setWaitForAccurateLocation(false)
            }.build()

            fusedClient.requestLocationUpdates(
                locationRequest,
                fusedLocationCallback,
                Looper.getMainLooper()
            ).addOnSuccessListener {
                _isTracking.value = true
                Log.d(TAG, "Fused location tracking initialized successfully.")
            }.addOnFailureListener { error ->
                Log.w(TAG, "Fused tracking failed: ${error.message}. Initiating framework fallback.")
                startFrameworkFallback()
            }

            // Immediately query last known position for instant responsiveness
            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                lastLoc?.let { processNewAndroidLocation(it, source = "LastKnown") }
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException initiating location: ${e.message}")
            startFrameworkFallback()
        }
    }

    /**
     * Offline and fallback provider using Android framework LocationManager.
     */
    @SuppressLint("MissingPermission")
    private fun startFrameworkFallback() {
        if (!hasLocationPermission() || locationManager == null) return
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    UPDATE_INTERVAL_MS,
                    1.0f,
                    frameworkLocationListener,
                    Looper.getMainLooper()
                )
                _isTracking.value = true
                _isGpsHardwareLocked.value = true
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    UPDATE_INTERVAL_MS,
                    1.0f,
                    frameworkLocationListener,
                    Looper.getMainLooper()
                )
                _isTracking.value = true
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Framework fallback failed with security exception: ${e.message}")
        }
    }

    /**
     * Stops location updates to preserve battery during prolonged blackouts.
     */
    fun stopTracking() {
        try {
            fusedClient.removeLocationUpdates(fusedLocationCallback)
        } catch (e: Exception) {
            Log.w(TAG, "Error removing fused updates: ${e.message}")
        }

        try {
            locationManager?.removeUpdates(frameworkLocationListener)
        } catch (e: Exception) {
            Log.w(TAG, "Error removing framework updates: ${e.message}")
        }

        _isTracking.value = false
    }

    /**
     * Updates internal state from incoming location sensor fixes.
     */
    private fun processNewAndroidLocation(loc: Location, source: String) {
        val geo = GeoPoint(
            latitude = loc.latitude,
            longitude = loc.longitude,
            altitudeM = if (loc.hasAltitude()) loc.altitude else 12.0,
            label = "Current Location ($source)"
        )
        _currentLocation.value = geo
        _gpsAccuracyMeters.value = if (loc.hasAccuracy()) loc.accuracy else null
        _isGpsHardwareLocked.value = true
        Log.d(TAG, "Location fix from $source: ${geo.latitude}, ${geo.longitude} (±${loc.accuracy}m)")
    }

    /**
     * Explicitly sets user location (used during disaster simulations or manual pin positioning).
     */
    fun updateManualLocation(point: GeoPoint) {
        _currentLocation.value = point
    }
}
