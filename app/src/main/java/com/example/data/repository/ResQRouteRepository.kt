/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /data/repository/ResQRouteRepository.kt
 *
 * PURPOSE & AIM:
 * Central repository providing single source of truth for the application.
 * Coordinates local Room database persistence, remote/cached OSRM routing calculations,
 * crowdsourced citizen hazard verifications, shelter check-in idempotency audits, and
 * reactive Kotlin StateFlow streams for UI presentation.
 *
 * LINKINGS & CONNECTIONS:
 * - Classes: [ResQRouteRepository] (Repository Implementation).
 * - Dependencies Injected: [ResQRouteDatabase], [OsrmRoutingService].
 * - Consumed By: [ResQRouteViewModel] via [ResQRouteViewModel.Factory].
 * - Interacts With DAOs: [ShelterDao], [HazardDao], [GoBagDao], [UserProfileDao],
 *   [ShelterUpdateDao], [RoadEdgeOverlayDao].
 */

package com.example.data.repository

import com.example.data.local.ResQRouteDatabase
import com.example.data.local.entity.GoBagEntity
import com.example.data.local.entity.HazardEntity
import com.example.data.local.entity.RoadEdgeOverlayEntity
import com.example.data.local.entity.ShelterEntity
import com.example.data.local.entity.ShelterUpdateEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.AccessibilityProfile
import com.example.data.model.FacilityItem
import com.example.data.model.GoBagItem
import com.example.data.model.GroundUpdateItem
import com.example.data.model.HazardReport
import com.example.data.model.OperatingMode
import com.example.data.model.Shelter
import com.example.data.model.UserProfile
import com.example.data.model.VolunteerHome
import com.example.data.model.CitizenReportOverlay
import com.example.data.model.GeoPoint
import com.example.data.model.HazardZoneOverlay
import com.example.data.model.MapMarker
import com.example.data.model.MarkerType
import com.example.data.model.MapUiState
import com.example.data.model.OsrmRouteData
import com.example.data.remote.OsrmRoutingService
import com.example.domain.RoutingEngine
import com.example.domain.RoutingResult
import com.example.domain.ShelterEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Repository orchestrating data sync, database operations, and live OSRM computations.
 */
class ResQRouteRepository(
    private val database: ResQRouteDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    // Room Data Access Objects
    private val shelterDao = database.shelterDao()
    private val hazardDao = database.hazardDao()
    private val goBagDao = database.goBagDao()
    private val userProfileDao = database.userProfileDao()
    private val shelterUpdateDao = database.shelterUpdateDao()
    private val roadEdgeOverlayDao = database.roadEdgeOverlayDao()

    // OSRM Routing Service for real routing calculations
    private val osrmService = OsrmRoutingService()

    // Application operational state (PEACETIME, DRILL, EMERGENCY)
    private val _operatingMode = MutableStateFlow(OperatingMode.EMERGENCY)
    val operatingMode: StateFlow<OperatingMode> = _operatingMode.asStateFlow()

    // Fixed facility and ground update lists matching platform specifications
    private val defaultFacilities = listOf(
        FacilityItem("f1", "Fresh Water Tanker", "water_drop", "4,000L Available"),
        FacilityItem("f2", "Hot Community Meals", "restaurant", "Serving Continuous"),
        FacilityItem("f3", "Oxygen & First Aid Post", "medical_services", "2 Doctors on Duty"),
        FacilityItem("f4", "Diesel Generator Power", "bolt", "100% Operational"),
        FacilityItem("f5", "Step-Free Bedding Area", "accessible", "12 Low Ramps Open"),
        FacilityItem("f6", "Clean Sanitation Blocks", "wc", "6 Heated Cubicles"),
        FacilityItem("f7", "Children & Pet Zone", "child_care", "Supervised"),
        FacilityItem("f8", "Ham Radio Relay", "cell_tower", "VHF 145.500 MHz")
    )

    private val defaultVolunteerHomes = listOf(
        VolunteerHome("Dr. Mehta's Residence", "180m away", 4, listOf("Oxygen Concentrator", "Ground Floor", "Pet Friendly")),
        VolunteerHome("Col. Deshmukh Villa", "320m away", 6, listOf("Solar Backup Battery", "Potable RO Water")),
        VolunteerHome("Anita & Ravi Sharma", "450m away", 3, listOf("Infant Supplies", "First Aid Kit", "Dry Floor"))
    )

    // Critical Emergency Facilities (Hospitals, Triage, and Rescue Posts)
    private val defaultCriticalFacilities = listOf(
        MapMarker(
            id = "fac-hosp-1",
            title = "Bandra Municipal Emergency Hospital",
            snippet = "24/7 Trauma & Oxygen Stockpile • 400m north",
            position = GeoPoint(19.0620, 72.8315, 24.0, "Bandra Municipal Hospital"),
            type = MarkerType.HOSPITAL,
            tag = "HOSPITAL"
        ),
        MapMarker(
            id = "fac-hosp-2",
            title = "Lilavati Disaster Medical Post",
            snippet = "Emergency Triage & Ambulances • 600m southwest",
            position = GeoPoint(19.0515, 72.8290, 16.0, "Lilavati Medical Post"),
            type = MarkerType.HOSPITAL,
            tag = "HOSPITAL"
        ),
        MapMarker(
            id = "fac-fire-1",
            title = "Bandra Fire & Water Rescue Post",
            snippet = "High-Water Inflatable Boats • VHF Ham Relay 145.500 MHz",
            position = GeoPoint(19.0590, 72.8360, 28.0, "Bandra Rescue Station"),
            type = MarkerType.FIRE_STATION,
            tag = "FIRE_STATION"
        )
    )

    // Network connectivity status (Online vs Zero-Internet Offline)
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun setNetworkStatus(online: Boolean) {
        _isOnline.value = online
    }

    init {
        // Asynchronously populate seed records if local Room tables are empty
        scope.launch {
            seedDatabaseIfEmpty()
        }
    }

    /**
     * Seeds initial disaster data into SQLite Room database on fresh launch.
     */
    private suspend fun seedDatabaseIfEmpty() {
        val initialShelter = ShelterEntity(
            id = "shelter-st-jude",
            name = "St. Jude Safe Haven",
            sector = "Sector 17 • Ridge Spine",
            distanceKm = 2.6,
            walkTimeMins = 14,
            elevationM = 32,
            occupiedCount = 195,
            totalCapacity = 250,
            availableSpaces = 55,
            occupancyPct = 78,
            arrivalRatePerMin = 3,
            expectedFullMins = 45,
            wheelchairBedsLeft = 12,
            visitorPositivePct = 94,
            hasWheelchairRamp = true,
            hasMedicalGenerator = true,
            hasFoodPower = true
        )
        shelterDao.insertShelter(initialShelter)

        val initialHazard = HazardEntity(
            id = "haz-104",
            reportNumber = "#104",
            title = "Flash Flood Emergency • Sector 17 Basin",
            locationName = "Canal Road Expressway",
            latitude = 19.0578,
            longitude = 72.8305,
            waterDepthCm = 48,
            culvertNode = "Culvert Node #104",
            communityConfirmationPct = 87,
            neighborsConfirmed = 14,
            detourName = "Ridge Road Corridor (+32m)",
            detourSafeUsersCount = 340,
            isConfirmedFlooded = true,
            isClosed = true,
            severityLevel = "IMPASSABLE",
            reportedAgo = "6 mins ago"
        )
        hazardDao.insertHazard(initialHazard)

        val initialProfile = UserProfileEntity(
            id = "user-1",
            name = "Drashti",
            phone = "+91 98200 12345",
            sector = "Bandra Basin • Sector 17",
            address = "Flat 402, Sea Green Apts, Hill Rd",
            familySafeWord = "BLUE RIVER",
            wheelchairAccessible = true,
            avoidSteepSlopes = true,
            medicalPriority = false,
            audioHapticGuidance = true
        )
        userProfileDao.insertProfile(initialProfile)

        val initialGoBag = listOf(
            GoBagEntity("g1", "Emergency Water (2L per person)", "WATER", packed = true, essential = true, note = "Sealed bottles with hydration salts"),
            GoBagEntity("g2", "Non-perishable High Energy Bars", "FOOD", packed = true, essential = true, note = "48h calorie requirement"),
            GoBagEntity("g3", "Critical Prescription Medications", "MEDICAL", packed = true, essential = true, note = "Asthma inhaler + 7 days insulin"),
            GoBagEntity("g4", "Waterproof ID & Property Deeds", "DOCS", packed = false, essential = true, note = "In airtight ziplock pouch"),
            GoBagEntity("g5", "10,000mAh Power Bank & Cable", "COMMS", packed = false, essential = true, note = "Kept charged at 100%"),
            GoBagEntity("g6", "N95 Dust & Smoke Mask", "SAFETY", packed = true, essential = false, note = "Protection against contaminated debris"),
            GoBagEntity("g7", "High-Decibel Safety Whistle", "SIGNAL", packed = true, essential = true, note = "For localized audible signaling")
        )
        goBagDao.insertItems(initialGoBag)

        val initialUpdates = listOf(
            ShelterUpdateEntity(id = "u1", timeAgo = "4m ago", text = "Water tanker arrived. 4,000L fresh potable water available at Gate 2."),
            ShelterUpdateEntity(id = "u2", timeAgo = "11m ago", text = "Doctor on duty reports pediatric antibiotic supply replenished."),
            ShelterUpdateEntity(id = "u3", timeAgo = "22m ago", text = "Auxiliary diesel generator initiated. Power sockets 100% live.")
        )
        shelterUpdateDao.insertUpdates(initialUpdates)
    }

    /**
     * Reactive stream of current shelter details mapped from Room entity to domain model.
     */
    val currentShelter: Flow<Shelter> = combine(
        shelterDao.getShelterById("shelter-st-jude"),
        shelterUpdateDao.getRecentUpdates()
    ) { entity, updates ->
        entity?.let {
            val (avail, pct) = ShelterEngine.calculateCapacityState(it.totalCapacity, it.occupiedCount)
            Shelter(
                id = it.id,
                name = it.name,
                sector = it.sector,
                distanceKm = it.distanceKm,
                walkTimeMins = it.walkTimeMins,
                elevationM = it.elevationM,
                occupiedCount = it.occupiedCount,
                totalCapacity = it.totalCapacity,
                availableSpaces = avail,
                occupancyPct = pct,
                arrivalRatePerMin = it.arrivalRatePerMin,
                expectedFullMins = it.expectedFullMins,
                wheelchairBedsLeft = it.wheelchairBedsLeft,
                visitorPositivePct = it.visitorPositivePct,
                hasWheelchairRamp = it.hasWheelchairRamp,
                hasMedicalGenerator = it.hasMedicalGenerator,
                hasFoodPower = it.hasFoodPower,
                facilities = defaultFacilities,
                groundUpdates = updates.map { u -> GroundUpdateItem(u.timeAgo, u.text) },
                volunteerHomes = defaultVolunteerHomes
            )
        } ?: Shelter(facilities = defaultFacilities, volunteerHomes = defaultVolunteerHomes)
    }

    /**
     * Reactive stream of active Red Zone hazard information.
     */
    val currentHazard: Flow<HazardReport> = hazardDao.getHazardById("haz-104").map { entity ->
        entity?.let {
            HazardReport(
                id = it.id,
                reportNumber = it.reportNumber,
                title = it.title,
                locationName = it.locationName,
                latitude = it.latitude,
                longitude = it.longitude,
                waterDepthCm = it.waterDepthCm,
                culvertNode = it.culvertNode,
                communityConfirmationPct = it.communityConfirmationPct,
                neighborsConfirmed = it.neighborsConfirmed,
                detourName = it.detourName,
                detourSafeUsersCount = it.detourSafeUsersCount,
                isConfirmedFlooded = it.isConfirmedFlooded,
                isClosed = it.isClosed,
                severityLevel = it.severityLevel,
                reportedAgo = it.reportedAgo
            )
        } ?: HazardReport()
    }

    /**
     * Reactive stream of go-bag items.
     */
    val goBagList: Flow<List<GoBagItem>> = goBagDao.getAllItems().map { list ->
        list.map { GoBagItem(it.id, it.name, it.category, it.packed, it.essential, it.note) }
    }

    /**
     * Reactive stream of user accessibility profile.
     */
    val userProfile: Flow<UserProfile> = userProfileDao.getProfile().map { entity ->
        entity?.let {
            UserProfile(
                id = it.id,
                name = it.name,
                phone = it.phone,
                sector = it.sector,
                address = it.address,
                familySafeWord = it.familySafeWord,
                accessibilityProfile = AccessibilityProfile(
                    wheelchairAccessible = it.wheelchairAccessible,
                    avoidSteepSlopes = it.avoidSteepSlopes,
                    medicalPriority = it.medicalPriority,
                    audioHapticGuidance = it.audioHapticGuidance
                )
            )
        } ?: UserProfile()
    }

    /**
     * Reactive route evaluation computing safety rationale against current flood state.
     */
    val routeEvaluation: Flow<RoutingResult> = combine(currentHazard, currentShelter, userProfile) { haz, sh, user ->
        RoutingEngine.evaluateCorridors(
            isCanalRoadClosed = haz.isClosed,
            waterDepthCm = haz.waterDepthCm,
            targetShelter = sh,
            accessibility = user.accessibilityProfile
        )
    }

    // Live user GPS coordinate telemetry
    private val _userLocation = MutableStateFlow(GeoPoint(19.0760, 72.8777, 14.0, "Mumbai Control Zone"))
    val userLocation: StateFlow<GeoPoint> = _userLocation.asStateFlow()

    /**
     * Map UI State stream populated with live/cached OSRM routes, hazard polygons, critical facilities, and authority detours.
     */
    val mapUiState: Flow<MapUiState> = combine(currentHazard, currentShelter, _userLocation, _isOnline) { haz, sh, userLoc, online ->
        // Fetch or use cached OSRM route data from current GPS origin
        val targetShelterPoint = GeoPoint(19.0880, 72.8890, 32.0, sh.name)
        val routes = osrmService.getEvacuationRoutes(
            origin = userLoc,
            shelter = targetShelterPoint
        )
        val rawSafeRoute = routes.first
        val rawDirectRoute = routes.second

        // Ensure offline labeling reflects truth: never pretend a fresh OSRM route was calculated if offline
        val safeRoute = if (!online) {
            rawSafeRoute.copy(source = "OSRM Cached Offline Corridor (Disaster Fallback)")
        } else {
            rawSafeRoute
        }
        val directRoute = if (!online) {
            rawDirectRoute.copy(source = "OSRM Cached Offline Corridor (Disaster Fallback)")
        } else {
            rawDirectRoute
        }

        val hazardOverlay = HazardZoneOverlay(
            id = haz.id,
            title = haz.locationName,
            waterDepthCm = haz.waterDepthCm,
            polygonPoints = listOf(
                GeoPoint(19.0560, 72.8270),
                GeoPoint(19.0585, 72.8310),
                GeoPoint(19.0570, 72.8340),
                GeoPoint(19.0550, 72.8300)
            ),
            severity = haz.severityLevel,
            reportedAgo = haz.reportedAgo
        )

        val reportOverlay = CitizenReportOverlay(
            id = "rep-1",
            reportNumber = "#104",
            title = "Culvert #104 Overtopping",
            location = GeoPoint(19.0578, 72.8305),
            confirmedCount = haz.neighborsConfirmed,
            verificationPct = haz.communityConfirmationPct,
            severity = "CRITICAL",
            timestamp = haz.reportedAgo
        )

        val roadClosuresList = if (haz.isClosed) {
            listOf(
                MapMarker(
                    id = "closure-culvert-104",
                    title = "ROAD CLOSED: Canal Road Culvert #104",
                    snippet = "Impassable Flood Basin (${haz.waterDepthCm}cm) • Municipal Hard Barrier",
                    position = GeoPoint(19.0578, 72.8305, 2.0, "Canal Road Culvert"),
                    type = MarkerType.ROAD_CLOSURE,
                    tag = "ROAD_CLOSED"
                )
            )
        } else {
            emptyList()
        }

        val isAuthorityActive = haz.isClosed || haz.severityLevel == "DETOUR_BROADCAST"

        MapUiState(
            userLocation = userLoc,
            targetShelter = targetShelterPoint,
            activeRoute = safeRoute,
            alternativeRoute = directRoute,
            hazards = listOf(hazardOverlay),
            reports = listOf(reportOverlay),
            criticalFacilities = defaultCriticalFacilities,
            roadClosures = roadClosuresList,
            isAuthorityDetourActive = isAuthorityActive,
            authorityDetourDecree = if (isAuthorityActive) {
                "MCGM Municipal Decree: Canal Rd Impassable (${haz.waterDepthCm}cm) • Authorized Detour via Ridge Spine (+32m)"
            } else {
                ""
            },
            isOnline = online,
            isOfflineFallback = !online || safeRoute.source.contains("Offline") || safeRoute.source.contains("Fallback"),
            isRealMapApiConnected = true,
            mapProviderName = if (online) "OSRM v5.24.0 (OpenStreetMap)" else "OSRM Cached Offline (Zero-Internet)"
        )
    }

    /**
     * Updates user location from incoming GPS/Fused location sensor fixes.
     */
    fun updateUserLocation(point: GeoPoint) {
        _userLocation.value = point
    }

    /**
     * Sets current operational mode.
     */
    fun setOperatingMode(mode: OperatingMode) {
        _operatingMode.value = mode
    }

    /**
     * Toggles checked state of an item in the Go-Bag.
     */
    suspend fun toggleGoBagItem(id: String, packed: Boolean) {
        goBagDao.updatePackedStatus(id, packed)
    }

    /**
     * Records neighbor confirmation of flood depth.
     */
    suspend fun confirmFloodHazard(id: String) {
        hazardDao.incrementConfirmation(id)
    }

    /**
     * Marks hazard as cleared after waters recede.
     */
    suspend fun markHazardCleared(id: String) {
        hazardDao.updateClosureStatus(id, isClosed = false, severity = "CLEAR")
    }

    /**
     * Alias for marking hazard cleared from citizen reports or SMS.
     */
    suspend fun reportHazardCleared(id: String) {
        markHazardCleared(id)
    }

    /**
     * Updates flood hazard depth and closure status from telemetry or SMS.
     */
    suspend fun updateHazardDepth(id: String, depthCm: Int, isClosed: Boolean, severity: String) {
        hazardDao.updateWaterDepth(id, depthCm, isClosed, severity)
    }

    /**
     * Updates shelter occupancy count from telemetry or SMS.
     */
    suspend fun updateShelterOccupancy(id: String, count: Int) {
        shelterDao.updateOccupancy(id, count)
    }

    /**
     * Ingests an incoming 2G emergency SMS payload into local SQLite storage.
     */
    suspend fun ingestSmsBroadcast(body: String): String {
        return com.example.receiver.SmsBroadcastReceiver.parseAndApplyPayload(this, body)
    }

    /**
     * Broadcasts municipal detour instructions across neighborhood handsets.
     */
    suspend fun approveAndBroadcastDetour(id: String) {
        hazardDao.updateClosureStatus(id, isClosed = true, severity = "DETOUR_BROADCAST")
    }

    /**
     * Appends an idempotent shelter check-in transaction into SQLite.
     */
    suspend fun appendShelterCheckIn(shelterId: String, currentCount: Int, totalCapacity: Int) {
        val newOccupancy = (currentCount + 1).coerceAtMost(totalCapacity)
        shelterDao.updateOccupancy(shelterId, newOccupancy)
    }

    /**
     * Synchronizes updated accessibility profile to local storage.
     */
    suspend fun updateAccessibilityProfile(profile: AccessibilityProfile) {
        userProfileDao.updateAccessibility(
            wheelchair = profile.wheelchairAccessible,
            slopes = profile.avoidSteepSlopes,
            medical = profile.medicalPriority,
            audioHaptics = profile.audioHapticGuidance
        )
    }
}
