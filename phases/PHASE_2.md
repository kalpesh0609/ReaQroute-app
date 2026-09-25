# Phase 2: Open Source Routing Machine (OSRM) & Interactive GIS Micro-Map

## Phase Name
**OSRM Evacuation Routing & Interactive GIS Vector Micro-Map Integration**

## Phase Objective
Integrate real-world geospatial routing via Open Source Routing Machine (OSRM) backed by OpenStreetMap road network geometries. Build an adaptive vector GIS map placeholder capable of rendering multi-tier overlays (safe ridge polylines, flooded canal hazard zones, citizen incident pins, elevation contours, and shelter approach corridors) with offline disaster fallback caching. Embed map views across all navigation, guidance, and dossier screens.

---

## Features Included
1. **OSRM Remote Service (`OsrmRoutingService`)**:
   - HTTP OkHttpClient integration querying the Open Source Routing Machine API (`https://router.project-osrm.org/route/v1`).
   - Parses GeoJSON geometries, coordinate streams, and step-by-step turn maneuvers.
   - Built-in disaster offline fallback: automatically switches to pre-cached Bandra Ridge GIS geometries when network connectivity is lost or times out (5-second threshold).
2. **Geospatial Data Models (`GeoModels.kt`)**:
   - `GeoPoint`, `MapMarker`, `MarkerType`, `HazardZoneOverlay`, `CitizenReportOverlay`, `OsrmStepData`, `OsrmRouteData`, and `MapUiState`.
3. **Adaptive GIS Vector Map (`ResQMapPlaceholder.kt`)**:
   - Canvas-rendered vector GIS map with pan/zoom gestures, custom pin rendering, animated flood zone polygons, and contour visualization.
   - Pluggable architecture designed with `defaultMinSize` layout constraints to serve as a drop-in contract for Google Maps SDK or MapLibre Native SDK.
   - Filter chips to toggle flood zones, elevation contours, and citizen reports dynamically.
   - Dialog inspecting raw OSRM GeoJSON geometry and routing metadata.
4. **End-to-End Screen Map Integration**:
   - `RouteComparisonScreen`: Dual-mode toggle switching between interactive OSRM GIS vector map and cross-sectional `ElevationProfileCanvas`.
   - `LiveGuidanceScreen`: Seamless GIS layer toggle (`OSRM Map` / `Radar HUD`) for live corridor tracking.
   - `SafeHavenDossierScreen`: Embedded high-ground approach corridor map leading directly into the shelter sanctuary gate.
   - `CitizenDossierScreen`: Embedded detour map illustrating culvert hazard node, inundated zone, and bypass alternative road.
5. **Phase 2 State & Regression Testing**:
   - JVM Robolectric test suite validating multi-screen `MapUiState` propagation, route point continuity, and layer filter states.

---

## Features Completed
- [x] `OsrmRoutingService.kt` with live OSRM network calling and offline cached disaster fallback corridors.
- [x] Geospatial data structures defined in `GeoModels.kt`.
- [x] Interactive vector map component `ResQMapPlaceholder.kt` with layer controls and tap detection.
- [x] Dual-mode map toggle integrated into `RouteComparisonScreen.kt`.
- [x] Dual-mode GIS/HUD toggle integrated into `LiveGuidanceScreen.kt`.
- [x] Shelter approach map integrated into `SafeHavenDossierScreen.kt`.
- [x] Detour and culvert hazard map integrated into `CitizenDossierScreen.kt`.
- [x] Robolectric test verifying multi-screen map overlay state consistency (`ExampleRobolectricTest.kt`).

## Features Currently in Progress
- None (Phase 2 implementation and verification are fully completed).

## Features Remaining
- None for Phase 2.

---

## How the Features Work
- **Routing Query**: `OsrmRoutingService.getEvacuationRoutes()` executes an HTTP call querying OSRM for two corridors:
  1. Safe Ridge Route: Routed along high-elevation waypoints (+32m) away from low water collection points.
  2. Direct Route: Passes through the low-lying basin at Culvert Node #104.
- **Offline Fallback**: If the network is unavailable or times out after 5 seconds, the service catches the exception and immediately returns high-resolution, deterministic local Bandra Ridge GeoJSON geometries without blocking the user.
- **Unified Map State**: `ResQRouteRepository.mapUiState` combines active hazards, citizen reports, and calculated OSRM polylines into an immutable `MapUiState` Flow.
- **Component Rendering**: `ResQMapPlaceholder` maps `MapUiState.activeRoute.geometryPoints` onto canvas pixel coordinates, drawing safe green/blue ridge lines and dashed red flooded sections according to bounding-box normalization.

---

## Current Status
**Status: COMPLETED**

---

## Important Files & Components Related to this Phase
- `/app/src/main/java/com/example/data/remote/OsrmRoutingService.kt`
- `/app/src/main/java/com/example/data/model/GeoModels.kt`
- `/app/src/main/java/com/example/ui/components/ResQMapPlaceholder.kt`
- `/app/src/main/java/com/example/ui/screens/RouteComparisonScreen.kt`
- `/app/src/main/java/com/example/ui/screens/LiveGuidanceScreen.kt`
- `/app/src/main/java/com/example/ui/screens/SafeHavenDossierScreen.kt`
- `/app/src/main/java/com/example/ui/screens/CitizenDossierScreen.kt`
- `/app/src/main/java/com/example/ui/components/ElevationProfileCanvas.kt`
- `/app/src/test/java/com/example/ExampleRobolectricTest.kt`
