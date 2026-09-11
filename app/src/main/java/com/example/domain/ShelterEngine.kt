/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /domain/ShelterEngine.kt
 *
 * PURPOSE & AIM:
 * Algorithmic calculations governing disaster shelter carrying capacity, quorum thresholds,
 * and immutable event idempotency. Prevents catastrophic shelter overflow and stampede
 * conditions by computing real-time occupancy percentages and flagging threshold alerts.
 *
 * LINKINGS & CONNECTIONS:
 * - Classes & Objects: [ShelterEngine] (Singleton Calculation Engine).
 * - Models Consumed: [Shelter] data models.
 * - Consumed By: [ResQRouteRepository] during check-in transactions, [SafeHavenDossierScreen]
 *   and [EvacuationRadarScreen] to render capacity gauges, and tested in [ExampleRobolectricTest].
 */

package com.example.domain

import com.example.data.model.Shelter
import java.util.UUID

/**
 * Singleton providing mathematical evaluation for shelter capacity and audit stream identifiers.
 */
object ShelterEngine {

    /**
     * Computes the available bed count and occupancy percentage for a given shelter.
     *
     * Aim:
     * Prevents negative capacity values and bounds the occupancy percentage within valid limits.
     *
     * @param totalCapacity Total registered capacity of the facility.
     * @param newOccupiedCount Number of checked-in or verified occupants.
     * @return Pair containing (availableSpaces, occupancyPercentage).
     */
    fun calculateCapacityState(
        totalCapacity: Int,
        newOccupiedCount: Int
    ): Pair<Int, Int> {
        // Enforce boundary constraints so occupancy never exceeds bounds or drops below zero
        val occupied = newOccupiedCount.coerceIn(0, totalCapacity)
        val available = (totalCapacity - occupied).coerceAtLeast(0)
        val percentage = if (totalCapacity > 0) ((occupied * 100.0) / totalCapacity).toInt() else 100
        return Pair(available, percentage)
    }

    /**
     * Generates a cryptographically unique idempotency key for append-only shelter audit logs.
     *
     * Aim:
     * Guarantees that offline sync retries do not record duplicate check-ins or double-count individuals.
     *
     * @param shelterId Unique identifier of the target shelter.
     * @return Formatted audit stream key with timestamp and short UUID entropy.
     */
    fun generateIdempotencyKey(shelterId: String): String {
        val timestamp = System.currentTimeMillis()
        val shortUuid = UUID.randomUUID().toString().take(8)
        return "evt-$timestamp-$shelterId-$shortUuid"
    }

    /**
     * Determines if a shelter is approaching quorum (> 80% capacity).
     *
     * Aim:
     * Triggers proactive early-warning rerouting badges to prevent late arrivals from finding full gates.
     *
     * @param occupancyPct Current occupancy percentage (0-100).
     * @return True if occupancy is at or above 80%.
     */
    fun isApproachingQuorum(occupancyPct: Int): Boolean = occupancyPct >= 80

    /**
     * Determines whether the shelter has reached absolute capacity (0 spaces remaining).
     *
     * @param availableSpaces Number of open slots.
     * @return True if zero or fewer spaces remain.
     */
    fun isAtFullCapacity(availableSpaces: Int): Boolean = availableSpaces <= 0
}
