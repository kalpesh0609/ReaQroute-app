# Phase 1: Core Foundation, Offline Room Architecture & Evacuation Cockpit

## Phase Name
**Core Foundation, Offline Room Database Architecture & Situation Cockpit**

## Phase Objective
Establish the primary offline-first Android application baseline, local Room persistence layer, and core evacuation UI workflows for disaster and flood crisis scenarios in the Bandra Basin (Sector 17). Provide immediate, decisive answers to the citizen's life-critical question: *"AM I SAFE?"* with dynamic threat banners, essential preparedness planning, and local SQLite data caching.

---

## Features Included
1. **Material Design 3 Theme & ResQ Tokens**:
   - High-contrast color tokens (`ResQBluePrimary`, `ResQSafeGreen`, `ResQAmberWarning`, `ResQDangerRed`) calibrated for outdoor readability, heavy rainfall, and low-light battery saver states.
2. **Local Room Database (`ResQRouteDatabase`)**:
   - Entities for shelters (`ShelterEntity`), hazards (`HazardEntity`), preparedness items (`GoBagEntity`), citizen identity (`UserProfileEntity`), ground updates (`ShelterUpdateEntity`), and road network edge states (`RoadEdgeOverlayEntity`).
   - Reactive DAO interfaces with Kotlin `Flow` queries (`ShelterDao`, `HazardDao`, `GoBagDao`, `UserProfileDao`, `ShelterUpdateDao`, `RoadEdgeOverlayDao`).
3. **Deterministic Safety Domain Engines**:
   - `RoutingEngine`: Implements safety-first route evaluation ("Shortest Route ≠ Safest Route") disqualifying low-basin flooded roads (>15cm depth or municipal closures) in favor of dry ridge spines.
   - `ShelterEngine`: Computes mathematical occupancy percentages, remaining capacity thresholds, quorum alerts (>80%), and creates unique event idempotency keys for append-only audit tracking.
4. **Central MVVM Architecture**:
   - `ResQRouteRepository`: Single source of truth combining Room DAO flows with seed records and domain evaluations.
   - `ResQRouteViewModel`: Exposes reactive `StateFlow` streams for operating modes, navigation, shelters, hazards, and user profile preferences.
5. **Core Application Screens & Navigation**:
   - `EvacuationRadarScreen`: Life threat status banner ("LIFE THREAT IN SECTOR 17"), rapid evacuation actions, shelter capacity meters, and SOS triggers.
   - `PreparednessHubScreen`: 72-hour family Go-Bag checklist with category grouping, essential tags, and interactive check-off states.
   - `ProfileScreen`: Citizen address details, family emergency safe words ("BLUE RIVER"), and mobility accessibility toggles.
   - `ResQTopBar` & `ResQBottomNav`: Operational mode switcher (Peacetime / Drill / Emergency) and 5-tab Material 3 navigation.

---

## Features Completed
- [x] Room database implementation with schema definitions and automatic seeding on first boot.
- [x] Repository and ViewModel reactive pipeline with unidirectional data flow (UDF).
- [x] `RoutingEngine.kt` and `ShelterEngine.kt` domain business logic.
- [x] High-contrast Material 3 theming in `Color.kt`, `Theme.kt`, and `Type.kt`.
- [x] Evacuation Radar situation room with animated pulsing threat banners.
- [x] 72-hour Go-Bag preparedness checklist with SQLite persistence.
- [x] Profile screen with accessibility preference toggles (wheelchair, slopes, medical, audio/haptic).
- [x] JVM Robolectric test suite validating routing safety arbitration and shelter capacity invariants.

## Features Currently in Progress
- None (Phase 1 foundation is fully completed and operational).

## Features Remaining
- None for Phase 1.

---

## How the Features Work
- **Database Bootstrapping**: On startup, `MainActivity` initializes `ResQRouteDatabase.getInstance(context)`. The repository checks if records exist; if empty, it populates initial baseline data (St. Jude Safe Haven, Hazard #104 at Culvert Node, citizen profile, and Go-Bag supplies).
- **Reactive UI Binding**: The `ResQRouteViewModel` collects database flows and transforms them using `stateIn(viewModelScope)`. Jetpack Compose screens observe these states using `collectAsState()`, ensuring automatic UI recomposition whenever hazard or capacity data changes.
- **Corridor Safety Logic**: When water depth at Canal Road exceeds 15cm or is flagged as closed, `RoutingEngine.evaluateCorridors()` automatically tags Candidate Route A as unsafe and elevates Candidate Route B (Ridge Road Spine, +32m elevation) as the recommended corridor.

---

## Current Status
**Status: COMPLETED**

---

## Important Files & Components Related to this Phase
- `/app/src/main/java/com/example/MainActivity.kt`
- `/app/src/main/java/com/example/data/local/ResQRouteDatabase.kt`
- `/app/src/main/java/com/example/data/local/entity/Entities.kt`
- `/app/src/main/java/com/example/data/local/dao/Daos.kt`
- `/app/src/main/java/com/example/data/model/Models.kt`
- `/app/src/main/java/com/example/data/repository/ResQRouteRepository.kt`
- `/app/src/main/java/com/example/domain/RoutingEngine.kt`
- `/app/src/main/java/com/example/domain/ShelterEngine.kt`
- `/app/src/main/java/com/example/ui/viewmodel/ResQRouteViewModel.kt`
- `/app/src/main/java/com/example/ui/screens/EvacuationRadarScreen.kt`
- `/app/src/main/java/com/example/ui/screens/PreparednessHubScreen.kt`
- `/app/src/main/java/com/example/ui/screens/ProfileScreen.kt`
- `/app/src/main/java/com/example/ui/components/ResQTopBar.kt`
- `/app/src/main/java/com/example/ui/components/ResQBottomNav.kt`
- `/app/src/main/java/com/example/ui/theme/Color.kt`
- `/app/src/main/java/com/example/ui/theme/Theme.kt`
