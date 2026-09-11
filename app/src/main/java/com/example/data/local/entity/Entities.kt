/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /data/local/entity/Entities.kt
 *
 * PURPOSE & AIM:
 * Declares Room persistence entities for local offline-first SQLite database storage.
 * Maps relational tables for shelters, Red Zone flood hazards, go-bag disaster kits,
 * citizen profiles, shelter updates, and road network segment overlays.
 *
 * LINKINGS & CONNECTIONS:
 * - Entities: [ShelterEntity], [HazardEntity], [GoBagEntity], [UserProfileEntity],
 *   [ShelterUpdateEntity], [RoadEdgeOverlayEntity].
 * - Consumed By: [ResQRouteDatabase], [ShelterDao], [HazardDao], [GoBagDao],
 *   [UserProfileDao], [ShelterUpdateDao], [RoadEdgeOverlayDao], and [ResQRouteRepository].
 */

package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted emergency safe haven shelter entity.
 */
@Entity(tableName = "shelters")
data class ShelterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sector: String,
    val distanceKm: Double,
    val walkTimeMins: Int,
    val elevationM: Int,
    val occupiedCount: Int,
    val totalCapacity: Int,
    val availableSpaces: Int,
    val occupancyPct: Int,
    val arrivalRatePerMin: Int,
    val expectedFullMins: Int,
    val wheelchairBedsLeft: Int,
    val visitorPositivePct: Int,
    val hasWheelchairRamp: Boolean,
    val hasMedicalGenerator: Boolean,
    val hasFoodPower: Boolean,
    val isActive: Boolean = true
)

/**
 * Persisted localized flood hazard and road closure entity.
 */
@Entity(tableName = "hazards")
data class HazardEntity(
    @PrimaryKey val id: String,
    val reportNumber: String,
    val title: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val waterDepthCm: Int,
    val culvertNode: String,
    val communityConfirmationPct: Int,
    val neighborsConfirmed: Int,
    val detourName: String,
    val detourSafeUsersCount: Int,
    val isConfirmedFlooded: Boolean,
    val isClosed: Boolean,
    val severityLevel: String,
    val reportedAgo: String
)

/**
 * Persisted family preparedness go-bag checklist item.
 */
@Entity(tableName = "go_bag_items")
data class GoBagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val packed: Boolean,
    val essential: Boolean,
    val note: String
)

/**
 * Persisted citizen profile and accessibility preferences.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val sector: String,
    val address: String,
    val familySafeWord: String,
    val wheelchairAccessible: Boolean,
    val avoidSteepSlopes: Boolean,
    val medicalPriority: Boolean,
    val audioHapticGuidance: Boolean
)

/**
 * Persisted real-time situational ground bulletins for shelters.
 */
@Entity(tableName = "shelter_updates")
data class ShelterUpdateEntity(
    @PrimaryKey val id: String,
    val timeAgo: String,
    val text: String
)

/**
 * Persisted topological road network edge state for offline routing graph pruning.
 */
@Entity(tableName = "road_edge_overlays")
data class RoadEdgeOverlayEntity(
    @PrimaryKey val edgeId: String,
    val roadName: String,
    val isClosed: Boolean,
    val closureReason: String,
    val waterDepthCm: Int
)
