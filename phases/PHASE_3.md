# Phase 3: Live Turn-by-Turn Guidance, Turn Maneuvers & Corridor Safety Enforcement

## Phase Name
**Live Turn-by-Turn Guidance, Turn Maneuvers & Corridor Safety Enforcement**

## Phase Objective
Deliver full-screen, high-stress live evacuation navigation that guides citizens through active floodwaters along safe, elevated corridors. Enforce turn-by-turn guidance prompts derived from OSRM leg steps, real-time distance and estimated time of arrival (ETA) recalculation, hazard alert overlays during active navigation, automated Text-to-Speech (TTS) voice announcements, precision haptic feedback pulses, dynamic compass heading orientation, and immediate shelter admission verification via QR check-in.

---

## Features Included
1. **Full-Screen Turn-by-Turn Guidance Mode (`LiveGuidanceScreen`)**:
   - Immersive navigation UI hiding distracting top and bottom navigation chrome for maximum operational focus.
   - High-contrast maneuver cards with turn direction indicators (depart, turn, continue, arrive) and distance-to-turn countdowns.
   - Linear route progress indicator tracking completion percentage along the corridor.
   - Live simulated speed display (KM/H) and compass bearing indicator.
2. **Corridor Trajectory HUD & Animated Visualizers**:
   - `LiveGuidanceCanvas`: Pulsing GPS beacon animation, forward trajectory path, turning waypoints, and destination beacon.
   - Dynamic user position interpolation advancing along the trajectory curve according to route progress.
   - Dual-view switcher allowing toggling between vector map tracking and animated tactical radar HUD.
3. **Hardware Sensor & Voice Orchestration (`GuidanceFeedbackHelper`)**:
   - Hands-free emergency Text-to-Speech (TTS) audio narration for every turn maneuver and safety alert.
   - Precision tactile vibration patterns via Android `Vibrator` / `VibrationEffect`:
     - Upcoming turn alert (100m warning).
     - Immediate maneuver execution.
     - Urgent flood/hazard proximity warning.
     - Safe haven gate arrival celebration.
   - Compass bearing tracking via `Sensor.TYPE_ROTATION_VECTOR` dynamically rotating orientation pointers.
4. **Interactive Corridor Progression & Testing**:
   - "Simulate Walk" automatic playback loop to test complete evacuation navigation end-to-end in emulator environments.
   - Manual next maneuver stepper for granular milestone auditing.
   - Dynamic reroute alert for Canal Road flooding with one-tap detour acceptance.
5. **Shelter Gate Arrival & Digital Check-In**:
   - Automated detection of sanctuary arrival at final route step.
   - High-contrast QR admission dialog displaying unique admission tokens (e.g., `SEC17-STJ-4481`).
   - Transactional shelter check-in logic (`checkInToShelter()` in `ResQRouteViewModel`) that updates bed availability in SQLite.

---

## Features Completed
- [x] Full-screen `LiveGuidanceScreen.kt` implementation with top/bottom bar suppression in `MainActivity.kt`.
- [x] OSRM step maneuver extraction and presentation with dynamic maneuver iconography.
- [x] Animated GPS pulse and trajectory canvas `LiveGuidanceCanvas.kt` with user interpolation and bearing arrow.
- [x] Dual-mode view toggle (OSRM Vector Map vs. Radar HUD).
- [x] Hardware sensory service `GuidanceFeedbackHelper.kt` with Text-to-Speech and tailored haptic vibration patterns.
- [x] Dynamic compass rotation integration via Android SensorManager rotation vector.
- [x] Runtime GPS location permission launcher (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`).
- [x] Auto-walk corridor simulation playback engine for container testing.
- [x] Interactive QR admission dialog with transactional shelter check-in logic (`checkInToShelter()`).
- [x] Dynamic flood detour alert prompt with one-tap bypass activation.
- [x] Exit navigation and return-to-radar workflows.

## Features Currently in Progress
- *None (Core Phase 3 capabilities complete and verified).*

## Features Remaining
- *Ready for Phase 4 (Zero-Internet 2G SMS Broadcast Ingestion & Peer Mesh).*

---

## How the Features Work
- **Navigation Initialization**: Launching "Evacuate via Ridge Spine" from the Radar or Route Comparison screen sets `activeScreen = ActiveScreen.LiveGuidance`. `MainActivity` suppresses the top bar and bottom nav for distraction-free navigation.
- **Maneuver Execution**: The screen reads `mapUiState.activeRoute.steps` produced by OSRM. When advancing between steps, `GuidanceFeedbackHelper` announces the spoken instruction via TTS and pulses the device vibrator.
- **Corridor Simulation**: Citizens or testing engineers can tap the play FAB to activate simulated walking, advancing coordinates along the corridor every 3.5 seconds with realistic speed telemetry.
- **Capacity Handshake**: Arriving at the final step opens the QR gate pass. Tapping "Confirm Admission" triggers `viewModel.checkInToShelter()`, recording the shelter admission event in SQLite and reducing remaining bed availability.

---

## Current Status
**Status: COMPLETED**

---

## Important Files & Components Related to this Phase
- `/app/src/main/java/com/example/ui/screens/LiveGuidanceScreen.kt`
- `/app/src/main/java/com/example/ui/components/LiveGuidanceCanvas.kt`
- `/app/src/main/java/com/example/ui/components/ResQMapPlaceholder.kt`
- `/app/src/main/java/com/example/service/GuidanceFeedbackHelper.kt`
- `/app/src/main/java/com/example/service/LocationTrackingService.kt`
- `/app/src/main/java/com/example/data/remote/OsrmRoutingService.kt`
- `/app/src/main/java/com/example/domain/ShelterEngine.kt`
- `/app/src/main/java/com/example/ui/viewmodel/ResQRouteViewModel.kt`
- `/app/src/main/java/com/example/MainActivity.kt`
