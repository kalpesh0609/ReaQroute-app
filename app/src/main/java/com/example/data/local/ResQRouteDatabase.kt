/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /data/local/ResQRouteDatabase.kt
 *
 * PURPOSE & AIM:
 * Declares the local Room Object-Relational Mapping (ORM) database powering offline-first resilience.
 * Houses SQLite schemas for emergency shelters, localized flood hazards, go-bag checklists,
 * citizen profiles, audit log streams, and road network topological overlays.
 *
 * LINKINGS & CONNECTIONS:
 * - Database Class: [ResQRouteDatabase] (Abstract RoomDatabase).
 * - Entities Managed: [ShelterEntity], [HazardEntity], [GoBagEntity], [UserProfileEntity],
 *   [ShelterUpdateEntity], [RoadEdgeOverlayEntity].
 * - DAOs Exposed: [ShelterDao], [HazardDao], [GoBagDao], [UserProfileDao],
 *   [ShelterUpdateDao], [RoadEdgeOverlayDao].
 * - Consumed By: [ResQRouteRepository] initialized in [MainActivity].
 */

package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.GoBagDao
import com.example.data.local.dao.HazardDao
import com.example.data.local.dao.RoadEdgeOverlayDao
import com.example.data.local.dao.ShelterDao
import com.example.data.local.dao.ShelterUpdateDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.GoBagEntity
import com.example.data.local.entity.HazardEntity
import com.example.data.local.entity.RoadEdgeOverlayEntity
import com.example.data.local.entity.ShelterEntity
import com.example.data.local.entity.ShelterUpdateEntity
import com.example.data.local.entity.UserProfileEntity

/**
 * Main application Room database definition with schema versioning and thread-safe singleton builder.
 */
@Database(
    entities = [
        ShelterEntity::class,
        HazardEntity::class,
        GoBagEntity::class,
        UserProfileEntity::class,
        ShelterUpdateEntity::class,
        RoadEdgeOverlayEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ResQRouteDatabase : RoomDatabase() {

    // Data Access Object providers
    abstract fun shelterDao(): ShelterDao
    abstract fun hazardDao(): HazardDao
    abstract fun goBagDao(): GoBagDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun shelterUpdateDao(): ShelterUpdateDao
    abstract fun roadEdgeOverlayDao(): RoadEdgeOverlayDao

    companion object {
        @Volatile
        private var INSTANCE: ResQRouteDatabase? = null

        /**
         * Returns or instantiates the singleton SQLite database instance.
         */
        fun getInstance(context: Context): ResQRouteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ResQRouteDatabase::class.java,
                    "resqroute_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
