/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /data/local/dao/Daos.kt
 *
 * PURPOSE & AIM:
 * Declares Room Data Access Object (DAO) interfaces providing reactive Kotlin Flow queries
 * and transactional mutation functions for the local SQLite database.
 * Supports offline-first evacuation reads and instant writes with conflict replacement.
 *
 * LINKINGS & CONNECTIONS:
 * - DAOs: [ShelterDao], [HazardDao], [GoBagDao], [UserProfileDao],
 *   [ShelterUpdateDao], [RoadEdgeOverlayDao].
 * - Entities Used: [ShelterEntity], [HazardEntity], [GoBagEntity], [UserProfileEntity],
 *   [ShelterUpdateEntity], [RoadEdgeOverlayEntity].
 * - Consumed By: [ResQRouteRepository] for reactive state flows.
 */

package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GoBagEntity
import com.example.data.local.entity.HazardEntity
import com.example.data.local.entity.RoadEdgeOverlayEntity
import com.example.data.local.entity.ShelterEntity
import com.example.data.local.entity.ShelterUpdateEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for managing emergency shelter capacity and registry.
 */
@Dao
interface ShelterDao {
    @Query("SELECT * FROM shelters WHERE id = :id LIMIT 1")
    fun getShelterById(id: String): Flow<ShelterEntity?>

    @Query("SELECT * FROM shelters WHERE isActive = 1 ORDER BY distanceKm ASC")
    fun getAllActiveShelters(): Flow<List<ShelterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelter(shelter: ShelterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelters(shelters: List<ShelterEntity>)

    @Update
    suspend fun updateShelter(shelter: ShelterEntity)

    @Query("UPDATE shelters SET occupiedCount = :occupiedCount, availableSpaces = totalCapacity - :occupiedCount, occupancyPct = ROUND((:occupiedCount * 100.0) / totalCapacity) WHERE id = :id")
    suspend fun updateOccupancy(id: String, occupiedCount: Int)
}

/**
 * Data Access Object for localized flood hazard incidents and road closures.
 */
@Dao
interface HazardDao {
    @Query("SELECT * FROM hazards WHERE id = :id LIMIT 1")
    fun getHazardById(id: String): Flow<HazardEntity?>

    @Query("SELECT * FROM hazards ORDER BY isClosed DESC, waterDepthCm DESC")
    fun getAllHazards(): Flow<List<HazardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazard(hazard: HazardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazards(hazards: List<HazardEntity>)

    @Query("UPDATE hazards SET neighborsConfirmed = neighborsConfirmed + 1, communityConfirmationPct = MIN(communityConfirmationPct + 1, 99) WHERE id = :id")
    suspend fun incrementConfirmation(id: String)

    @Query("UPDATE hazards SET isClosed = :isClosed, severityLevel = :severity WHERE id = :id")
    suspend fun updateClosureStatus(id: String, isClosed: Boolean, severity: String)

    @Query("UPDATE hazards SET waterDepthCm = :depthCm, isClosed = :isClosed, severityLevel = :severity WHERE id = :id")
    suspend fun updateWaterDepth(id: String, depthCm: Int, isClosed: Boolean, severity: String)
}

/**
 * Data Access Object for family emergency go-bag supplies.
 */
@Dao
interface GoBagDao {
    @Query("SELECT * FROM go_bag_items")
    fun getAllItems(): Flow<List<GoBagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<GoBagEntity>)

    @Query("UPDATE go_bag_items SET packed = :packed WHERE id = :id")
    suspend fun updatePackedStatus(id: String, packed: Boolean)
}

/**
 * Data Access Object for user identity and accessibility profiles.
 */
@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET wheelchairAccessible = :wheelchair, avoidSteepSlopes = :slopes, medicalPriority = :medical, audioHapticGuidance = :audioHaptics WHERE id = 'user-1'")
    suspend fun updateAccessibility(wheelchair: Boolean, slopes: Boolean, medical: Boolean, audioHaptics: Boolean)
}

/**
 * Data Access Object for real-time situational ground bulletins.
 */
@Dao
interface ShelterUpdateDao {
    @Query("SELECT * FROM shelter_updates LIMIT 10")
    fun getRecentUpdates(): Flow<List<ShelterUpdateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdates(updates: List<ShelterUpdateEntity>)
}

/**
 * Data Access Object for road network topology and bypass edges.
 */
@Dao
interface RoadEdgeOverlayDao {
    @Query("SELECT * FROM road_edge_overlays WHERE edgeId = :edgeId LIMIT 1")
    fun getEdgeById(edgeId: String): Flow<RoadEdgeOverlayEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEdge(edge: RoadEdgeOverlayEntity)
}
