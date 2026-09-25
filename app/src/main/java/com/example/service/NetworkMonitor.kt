/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /service/NetworkMonitor.kt
 *
 * PURPOSE & AIM:
 * Reactive network connectivity monitor for disaster environments.
 * Detects transitions between ONLINE (cellular/Wi-Fi active) and OFFLINE (blackout/no-internet).
 * Emits reactive StateFlow consumed by the repository to switch between live OSRM queries
 * and cached offline disaster corridors, while updating UI state badges.
 */

package com.example.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(context: Context) {
    companion object {
        private const val TAG = "NetworkMonitor"
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInitialState())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available. Operating in ONLINE mode.")
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            Log.w(TAG, "Network connection lost. Operating in OFFLINE mode.")
            _isOnline.value = checkInitialState()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            _isOnline.value = hasInternet
        }
    }

    init {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register NetworkCallback: ${e.message}")
        }
    }

    fun checkInitialState(): Boolean {
        return try {
            val active = connectivityManager?.activeNetwork ?: return false
            val caps = connectivityManager.getNetworkCapabilities(active) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    fun destroy() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignored
        }
    }
}
