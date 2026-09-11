/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /data/model/Models.kt
 *
 * PURPOSE & AIM:
 * Canonical domain models for user profiles, disaster shelters, go-bag preparedness checklists,
 * road corridor options, and application navigation states. Matches and extends the core
 * platform data schema designed in platformData.ts.
 *
 * LINKINGS & CONNECTIONS:
 * - Data Classes: [AccessibilityProfile], [UserProfile], [FacilityItem], [GroundUpdateItem],
 *   [VolunteerHome], [Shelter], [HazardReport], [RouteOption], [GoBagItem].
 * - Enums & Sealed Classes: [OperatingMode], [MobileTab], [ActiveScreen].
 * - Consumed By: Room database entities ([Entities.kt]), [ResQRouteRepository],
 *   [ResQRouteViewModel], and UI screens across the application.
 */

package com.example.data.model

/**
 * Citizen accessibility and vulnerability settings.
 *
 * Purpose: Ensures evacuation routing adapts to physical mobility restrictions, medical needs,
 * and low-connectivity sensory requirements (audio/haptics).
 */
data class AccessibilityProfile(
    val wheelchairAccessible: Boolean = true,
    val avoidSteepSlopes: Boolean = true,
    val medicalPriority: Boolean = false,
    val audioHapticGuidance: Boolean = true
)

/**
 * Local user profile containing citizen identity, household address, and emergency safewords.
 */
data class UserProfile(
    val id: String = "user-1",
    val name: String = "Drashti",
    val phone: String = "+91 98200 12345",
    val sector: String = "Bandra Basin • Sector 17",
    val address: String = "Flat 402, Sea Green Apts, Hill Rd",
    val familySafeWord: String = "BLUE RIVER",
    val accessibilityProfile: AccessibilityProfile = AccessibilityProfile()
)

/**
 * Facility or amenity available inside a safe haven shelter.
 */
data class FacilityItem(
    val id: String,
    val name: String,
    val iconName: String,
    val statusText: String
)

/**
 * Real-time ground status report posted by shelter staff or emergency coordinators.
 */
data class GroundUpdateItem(
    val timeAgo: String,
    val text: String
)

/**
 * Neighborhood volunteer host providing auxiliary emergency sanctuary slots.
 */
data class VolunteerHome(
    val name: String,
    val distance: String,
    val spots: Int,
    val features: List<String>
)

/**
 * Emergency safe haven shelter representation with carrying capacity metrics.
 *
 * Purpose: Tracks physical space availability, elevation, and facility amenities to
 * prevent facility stampedes and ensure equitable capacity distribution.
 */
data class Shelter(
    val id: String = "shelter-st-jude",
    val name: String = "St. Jude Safe Haven",
    val sector: String = "Sector 17 • Ridge Spine",
    val distanceKm: Double = 2.6,
    val walkTimeMins: Int = 14,
    val elevationM: Int = 32,
    val occupiedCount: Int = 195,
    val totalCapacity: Int = 250,
    val availableSpaces: Int = 55,
    val occupancyPct: Int = 78,
    val arrivalRatePerMin: Int = 3,
    val expectedFullMins: Int = 45,
    val wheelchairBedsLeft: Int = 12,
    val visitorPositivePct: Int = 94,
    val hasWheelchairRamp: Boolean = true,
    val hasMedicalGenerator: Boolean = true,
    val hasFoodPower: Boolean = true,
    val facilities: List<FacilityItem> = emptyList(),
    val groundUpdates: List<GroundUpdateItem> = emptyList(),
    val volunteerHomes: List<VolunteerHome> = emptyList()
)

/**
 * Verified or pending community hazard event (e.g. culvert overtopping or flooded roadway).
 */
data class HazardReport(
    val id: String = "haz-104",
    val reportNumber: String = "#104",
    val title: String = "Flash Flood Emergency • Sector 17 Basin",
    val locationName: String = "Canal Road Expressway",
    val latitude: Double = 19.0760,
    val longitude: Double = 72.8777,
    val waterDepthCm: Int = 48,
    val culvertNode: String = "Culvert Node #104",
    val communityConfirmationPct: Int = 87,
    val neighborsConfirmed: Int = 14,
    val detourName: String = "Ridge Road Corridor (+32m)",
    val detourSafeUsersCount: Int = 340,
    val isConfirmedFlooded: Boolean = true,
    val isClosed: Boolean = true,
    val severityLevel: String = "FLOODED",
    val reportedAgo: String = "6 mins ago"
)

/**
 * Candidate evacuation corridor option evaluated by the routing engine.
 */
data class RouteOption(
    val id: String,
    val name: String,
    val corridorName: String,
    val travelTimeMins: Int,
    val distanceKm: Double,
    val elevationGainM: Int,
    val isSafe: Boolean,
    val isFlooded: Boolean,
    val description: String,
    val avoidedHazardsCount: Int = 0
)

/**
 * Preparedness go-bag checklist item for offline family emergency kits.
 */
data class GoBagItem(
    val id: String,
    val name: String,
    val category: String,
    val packed: Boolean,
    val essential: Boolean,
    val note: String
)

/**
 * Operational mode governing system alert posture and UI severity styling.
 */
enum class OperatingMode {
    PEACETIME,
    EMERGENCY,
    DRILL
}

/**
 * Top-level navigation tabs available on the mobile navigation bar.
 */
enum class MobileTab(val label: String) {
    RADAR("Radar"),
    PREPAREDNESS("Kit & Plan"),
    SAFE_ZONES("Safe Zones"),
    DOSSIER("Dossier"),
    PROFILE("Profile")
}

/**
 * Sealed destination hierarchy representing all reachable screens in the application.
 */
sealed class ActiveScreen {
    data object Radar : ActiveScreen()
    data object PreparednessHub : ActiveScreen()
    data object RouteComparison : ActiveScreen()
    data object LiveGuidance : ActiveScreen()
    data object ShelterDetail : ActiveScreen()
    data object CitizenDossier : ActiveScreen()
    data object SmsGateway : ActiveScreen()
    data object AuthorityConsole : ActiveScreen()
    data object Profile : ActiveScreen()
}
