/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /ui/viewmodel/ResQRouteViewModel.kt
 *
 * PURPOSE & AIM:
 * Central MVVM ViewModel responsible for managing interactive application state,
 * routing user commands, updating preparedness checklists, processing crowd hazard confirmations,
 * logging shelter check-ins, and emitting reactive state flows for Jetpack Compose UI screens.
 *
 * LINKINGS & CONNECTIONS:
 * - Class: [ResQRouteViewModel] (Android Architecture ViewModel).
 * - Companion Factory: [ResQRouteViewModel.Factory] for ViewModelProvider injection.
 * - Injected Dependency: [ResQRouteRepository] (Single source of truth).
 * - Flows Emitted: [operatingMode], [activeTab], [activeScreen], [shelter], [hazard],
 *   [goBagItems], [userProfile], [routeEvaluation], [mapUiState], [toastMessage], [compassHeading].
 * - Consumed By: [ResQRouteApp] in [MainActivity], and all subordinate screen composables.
 */

package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccessibilityProfile
import com.example.data.model.ActiveScreen
import com.example.data.model.GoBagItem
import com.example.data.model.HazardReport
import com.example.data.model.MapUiState
import com.example.data.model.MobileTab
import com.example.data.model.OperatingMode
import com.example.data.model.Shelter
import com.example.data.model.UserProfile
import com.example.data.repository.ResQRouteRepository
import com.example.domain.RoutingEngine
import com.example.domain.RoutingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Main ViewModel managing UI state for the ResQRoute platform.
 *
 * @param repository Data repository abstracting Room database and OSRM network operations.
 */
class ResQRouteViewModel(
    private val repository: ResQRouteRepository
) : ViewModel() {

    // =========================================================================
    // STATE FLOW EMITTERS (Observed by Compose Screens)
    // =========================================================================

    /** Current operating mode (PEACETIME, DRILL, EMERGENCY) */
    val operatingMode: StateFlow<OperatingMode> = repository.operatingMode

    /** Currently selected tab on bottom navigation */
    private val _activeTab = MutableStateFlow(MobileTab.RADAR)
    val activeTab: StateFlow<MobileTab> = _activeTab.asStateFlow()

    /** Currently visible screen composable in the scaffold */
    private val _activeScreen = MutableStateFlow<ActiveScreen>(ActiveScreen.Radar)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    /** Safe haven shelter carrying capacity and amenities */
    val shelter: StateFlow<Shelter> = repository.currentShelter
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Shelter())

    /** Red Zone hazard report and verification consensus */
    val hazard: StateFlow<HazardReport> = repository.currentHazard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HazardReport())

    /** Emergency go-bag checklist items */
    val goBagItems: StateFlow<List<GoBagItem>> = repository.goBagList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Citizen profile and accessibility preferences */
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    /** Algorithmic corridor safety evaluation */
    val routeEvaluation: StateFlow<RoutingResult> = repository.routeEvaluation
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            RoutingEngine.evaluateCorridors(true, 48, Shelter(), AccessibilityProfile())
        )

    /** Map UI state with OSRM routes and hazard polygons */
    val mapUiState: StateFlow<MapUiState> = repository.mapUiState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MapUiState())

    /** Ephemeral user toast / snackbar notification text */
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    /** Heading orientation for the live navigation compass */
    private val _compassHeading = MutableStateFlow(24f)
    val compassHeading: StateFlow<Float> = _compassHeading.asStateFlow()

    // =========================================================================
    // USER ACTIONS & INTENT DISPATCHERS
    // =========================================================================

    /**
     * Navigates directly to any screen in the application.
     */
    fun navigateTo(screen: ActiveScreen) {
        _activeScreen.value = screen
    }

    /**
     * Updates bottom navigation tab selection and automatically switches to primary screen for that tab.
     */
    fun selectTab(tab: MobileTab) {
        _activeTab.value = tab
        _activeScreen.value = when (tab) {
            MobileTab.RADAR -> ActiveScreen.Radar
            MobileTab.PREPAREDNESS -> ActiveScreen.PreparednessHub
            MobileTab.SAFE_ZONES -> ActiveScreen.ShelterDetail
            MobileTab.DOSSIER -> ActiveScreen.CitizenDossier
            MobileTab.PROFILE -> ActiveScreen.Profile
        }
    }

    /**
     * Changes operational mode (Peacetime / Drill / Emergency) and refocuses to Radar screen in crisis.
     */
    fun setOperatingMode(mode: OperatingMode) {
        repository.setOperatingMode(mode)
        if (mode == OperatingMode.EMERGENCY || mode == OperatingMode.DRILL) {
            selectTab(MobileTab.RADAR)
        }
    }

    /**
     * Toggles packed state of a Go-Bag checklist item.
     */
    fun toggleGoBagItem(id: String) {
        val item = goBagItems.value.find { it.id == id } ?: return
        viewModelScope.launch {
            repository.toggleGoBagItem(id, !item.packed)
        }
    }

    /**
     * Submits a crowdsourced confirmation of flood depth.
     */
    fun confirmHazardFlooded() {
        val h = hazard.value
        viewModelScope.launch {
            repository.confirmFloodHazard(h.id)
            showToast("Report recorded (+1). Nearby neighbors notified.")
        }
    }

    /**
     * Reports hazard cleared after storm drain clears.
     */
    fun reportHazardCleared() {
        val h = hazard.value
        viewModelScope.launch {
            repository.markHazardCleared(h.id)
            showToast("Notice sent: Water level cleared at Sector 17 basin.")
        }
    }

    /**
     * Broadcasts emergency detour across neighborhood citizen devices.
     */
    fun approveAndBroadcastDetour() {
        val h = hazard.value
        viewModelScope.launch {
            repository.approveAndBroadcastDetour(h.id)
            showToast("Detour broadcast active to 1,420 nearby citizen handsets.")
        }
    }

    /**
     * Registers a verified shelter check-in transaction.
     */
    fun checkInToShelter() {
        val current = shelter.value
        viewModelScope.launch {
            repository.appendShelterCheckIn(current.id, current.occupiedCount, current.totalCapacity)
            showToast("Checked In: QR verified at St. Jude Safe Haven ✓")
        }
    }

    /**
     * Toggles a user accessibility preference and updates the database.
     */
    fun toggleAccessibilityPreference(key: String) {
        val prof = userProfile.value.accessibilityProfile
        val updated = when (key) {
            "wheelchair" -> prof.copy(wheelchairAccessible = !prof.wheelchairAccessible)
            "slopes" -> prof.copy(avoidSteepSlopes = !prof.avoidSteepSlopes)
            "medical" -> prof.copy(medicalPriority = !prof.medicalPriority)
            "audioHaptics" -> prof.copy(audioHapticGuidance = !prof.audioHapticGuidance)
            else -> prof
        }
        viewModelScope.launch {
            repository.updateAccessibilityProfile(updated)
            showToast("Mobility preferences synced.")
        }
    }

    /**
     * Displays a transient snackbar toast message.
     */
    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    /**
     * Clears current snackbar message after display.
     */
    fun clearToast() {
        _toastMessage.value = null
    }

    /**
     * Custom ViewModelProvider.Factory for instantiating [ResQRouteViewModel] with repository dependency.
     */
    class Factory(private val repository: ResQRouteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ResQRouteViewModel(repository) as T
        }
    }
}
