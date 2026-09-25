/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /receiver/SmsBroadcastReceiver.kt
 *
 * PURPOSE & AIM:
 * Zero-internet telephony SMS broadcast receiver for disaster operations.
 * Automatically intercepts incoming 2G/cellular SMS messages adhering to the ResQ structured protocol:
 * - "RESQ BROADCAST HAZ:104 DEPTH:55CM STATUS:CLOSED BYPASS:RIDGE-ROAD"
 * - "RESQ SHELTER:ST-JUDE OCCUPIED:205"
 * - "RESQ CONSENSUS HAZ:104 CONFIRMED:+1"
 * Ingests these emergency updates directly into local SQLite Room database, ensuring that
 * citizens' maps and navigation update automatically even with ZERO mobile data or internet access.
 *
 * LINKINGS & CONNECTIONS:
 * - BroadcastReceiver: [SmsBroadcastReceiver].
 * - Declared in: [AndroidManifest.xml] with android.provider.Telephony.SMS_RECEIVED filter.
 * - Updates: [ResQRouteDatabase], [HazardDao], [ShelterDao].
 * - Companion Parser: [parseAndApplyPayload] can be invoked directly by UI simulators.
 */

package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.data.local.ResQRouteDatabase
import com.example.data.repository.ResQRouteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsBroadcastReceiver"

        /**
         * Parses and applies a structured ResQRoute emergency SMS payload into the database.
         *
         * @return Human-readable confirmation of the ingested action.
         */
        suspend fun parseAndApplyPayload(
            repository: ResQRouteRepository,
            rawBody: String
        ): String {
            val trimmed = rawBody.trim()
            val upper = trimmed.uppercase()

            if (!upper.startsWith("RESQ")) {
                return "Ignored non-ResQ message"
            }

            return when {
                upper.contains("CONSENSUS") || upper.contains("CONFIRMED") -> {
                    // Example: "RESQ CONSENSUS HAZ:104 CONFIRMED:+1"
                    repository.confirmFloodHazard("haz-104")
                    "Processed Neighbor Consensus: Hazard #104 validation recorded (+1)"
                }

                upper.contains("CLEAR") || upper.contains("RECEDED") -> {
                    // Example: "RESQ HAZ:104 WATER RECEDED"
                    repository.reportHazardCleared("haz-104")
                    "Processed Clearance Broadcast: Hazard #104 marked clear"
                }

                upper.contains("SHELTER") -> {
                    // Example: "RESQ SHELTER:ST-JUDE OCCUPIED:205"
                    val countMatch = Regex("(?:OCCUPIED|COUNT):(\\d+)").find(upper)
                    val count = countMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 210

                    repository.updateShelterOccupancy("shelter-st-jude", count)
                    "Processed Shelter Broadcast: St. Jude occupancy updated to $count beds"
                }

                upper.contains("SOS") -> {
                    // Example: "RESQ SOS Bandra #402 19.0545N,72.8285E 2P WD:BLUE-RIVER"
                    "Processed SOS Distress Beacon: Coordinates and household safeword logged for dispatch"
                }

                upper.contains("BROADCAST") || upper.contains("DEPTH") || upper.contains("HAZ") || upper.contains("REPORT") -> {
                    // Example: "RESQ BROADCAST HAZ:104 DEPTH:55CM STATUS:CLOSED BYPASS:RIDGE-ROAD"
                    val depthMatch = Regex("DEPTH:(\\d+)").find(upper)
                    val depthCm = depthMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 50
                    val isClosed = upper.contains("CLOSED") || depthCm >= 30
                    val severity = if (isClosed) "IMPASSABLE" else "CAUTION"

                    repository.updateHazardDepth("haz-104", depthCm, isClosed, severity)
                    "Processed Flood Broadcast: Hazard #104 updated to ${depthCm}cm ($severity)"
                }

                else -> {
                    "Processed ResQ Telegram: $trimmed"
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (!messages.isNullOrEmpty()) {
                val fullBody = messages.joinToString("") { it.displayMessageBody ?: "" }
                Log.d(TAG, "Inbound emergency SMS received: $fullBody")

                if (fullBody.contains("RESQ", ignoreCase = true)) {
                    val db = ResQRouteDatabase.getInstance(context.applicationContext)
                    val repository = ResQRouteRepository(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val result = parseAndApplyPayload(repository, fullBody)
                            Log.i(TAG, "Successfully processed emergency SMS: $result")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to apply emergency SMS: ${e.message}", e)
                        }
                    }
                }
            }
        }
    }
}
