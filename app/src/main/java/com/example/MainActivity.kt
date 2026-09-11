/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /MainActivity.kt
 *
 * PURPOSE & AIM:
 * The single-activity entry point for the ResQRoute Android application.
 * Bootstraps the local Room database, initializes the data repository, injects the view model,
 * enables Android edge-to-edge system insets, and hosts the top-level Compose navigation scaffold.
 *
 * LINKINGS & CONNECTIONS:
 * - Activity: [MainActivity] (ComponentActivity).
 * - Root Composable: [ResQRouteApp].
 * - State Management: [ResQRouteViewModel] providing reactive state flows.
 * - Persistent Data: [ResQRouteDatabase] and [ResQRouteRepository].
 * - Navigation: Handles Android back-press via [BackHandler] and switches between [ActiveScreen] destinations:
 *   [EvacuationRadarScreen], [PreparednessHubScreen], [RouteComparisonScreen], [LiveGuidanceScreen],
 *   [SafeHavenDossierScreen], [CitizenDossierScreen], [SmsGatewayScreen], [AuthorityConsoleScreen], [ProfileScreen].
 * - App Chrome: [ResQTopBar] and [ResQBottomNav].
 */

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.ResQRouteDatabase
import com.example.data.model.ActiveScreen
import com.example.data.model.MobileTab
import com.example.data.model.OperatingMode
import com.example.data.repository.ResQRouteRepository
import com.example.ui.components.ResQBottomNav
import com.example.ui.components.ResQTopBar
import com.example.ui.screens.AuthorityConsoleScreen
import com.example.ui.screens.CitizenDossierScreen
import com.example.ui.screens.EvacuationRadarScreen
import com.example.ui.screens.LiveGuidanceScreen
import com.example.ui.screens.PreparednessHubScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RouteComparisonScreen
import com.example.ui.screens.SafeHavenDossierScreen
import com.example.ui.screens.SmsGatewayScreen
import com.example.ui.theme.ResQRouteTheme
import com.example.ui.viewmodel.ResQRouteViewModel

/**
 * Main application Activity. Bootstraps the application lifecycle and sets the Compose content root.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enables edge-to-edge system insets for modern Android display compliance
        enableEdgeToEdge()

        // Initialize local SQLite Room database and single-source-of-truth repository
        val database = ResQRouteDatabase.getInstance(applicationContext)
        val repository = ResQRouteRepository(database)

        setContent {
            ResQRouteTheme {
                // Instantiate central ViewModel with custom factory
                val viewModel: ResQRouteViewModel = viewModel(
                    factory = ResQRouteViewModel.Factory(repository)
                )

                // Root application composable
                ResQRouteApp(viewModel = viewModel)
            }
        }
    }
}

/**
 * Root Composable orchestrating top-level scaffold, top bar, bottom navigation, and screen routing.
 *
 * @param viewModel Central ViewModel emitting reactive states.
 */
@Composable
fun ResQRouteApp(viewModel: ResQRouteViewModel) {
    // Collect reactive state flows from the ViewModel
    val activeScreen by viewModel.activeScreen.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val operatingMode by viewModel.operatingMode.collectAsState()
    val shelter by viewModel.shelter.collectAsState()
    val hazard by viewModel.hazard.collectAsState()
    val goBagItems by viewModel.goBagItems.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val routeEvaluation by viewModel.routeEvaluation.collectAsState()
    val mapUiState by viewModel.mapUiState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Host state for displaying transient snackbar notifications
    val snackbarHostState = remember { SnackbarHostState() }

    // Display transient toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Intercept hardware system Back button to guarantee graceful drill-down backstack navigation
    if (activeScreen != ActiveScreen.Radar && activeScreen != ActiveScreen.PreparednessHub) {
        BackHandler {
            when (activeScreen) {
                ActiveScreen.RouteComparison,
                ActiveScreen.LiveGuidance,
                ActiveScreen.SmsGateway,
                ActiveScreen.AuthorityConsole -> {
                    viewModel.navigateTo(ActiveScreen.Radar)
                }
                ActiveScreen.ShelterDetail,
                ActiveScreen.CitizenDossier,
                ActiveScreen.Profile -> {
                    viewModel.selectTab(MobileTab.RADAR)
                }
                else -> {
                    viewModel.selectTab(MobileTab.RADAR)
                }
            }
        }
    }

    // Hide top and bottom bars during full-screen turn-by-turn guidance
    val showTopAndBottomBars = activeScreen != ActiveScreen.LiveGuidance

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showTopAndBottomBars) {
                ResQTopBar(
                    operatingMode = operatingMode,
                    onToggleOperatingMode = {
                        val nextMode = when (operatingMode) {
                            OperatingMode.PEACETIME -> OperatingMode.DRILL
                            OperatingMode.DRILL -> OperatingMode.EMERGENCY
                            OperatingMode.EMERGENCY -> OperatingMode.PEACETIME
                        }
                        viewModel.setOperatingMode(nextMode)
                    },
                    onOpenSmsGateway = { viewModel.navigateTo(ActiveScreen.SmsGateway) },
                    onOpenAuthorityConsole = { viewModel.navigateTo(ActiveScreen.AuthorityConsole) }
                )
            }
        },
        bottomBar = {
            if (showTopAndBottomBars) {
                ResQBottomNav(
                    activeTab = activeTab,
                    onTabSelected = { tab -> viewModel.selectTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        // Main Screen Viewport Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(innerPadding)
        ) {
            // Dynamic Screen Switching based on activeScreen state
            when (activeScreen) {
                ActiveScreen.Radar -> {
                    EvacuationRadarScreen(
                        shelter = shelter,
                        mapUiState = mapUiState,
                        onStartEvacuation = { viewModel.navigateTo(ActiveScreen.LiveGuidance) },
                        onRequestRescue = { viewModel.navigateTo(ActiveScreen.SmsGateway) },
                        onViewRouteDetails = { viewModel.navigateTo(ActiveScreen.RouteComparison) },
                        onViewShelterDetails = { viewModel.navigateTo(ActiveScreen.ShelterDetail) }
                    )
                }

                ActiveScreen.PreparednessHub -> {
                    PreparednessHubScreen(
                        shelter = shelter,
                        goBagItems = goBagItems,
                        operatingMode = operatingMode,
                        onToggleMode = { mode -> viewModel.setOperatingMode(mode) },
                        onToggleGoBagItem = { id -> viewModel.toggleGoBagItem(id) },
                        onNavigateToRoute = { viewModel.navigateTo(ActiveScreen.RouteComparison) },
                        onNavigateToShelter = { viewModel.navigateTo(ActiveScreen.ShelterDetail) },
                        onNavigateToSMS = { viewModel.navigateTo(ActiveScreen.SmsGateway) },
                        onNavigateToSOS = { viewModel.navigateTo(ActiveScreen.SmsGateway) }
                    )
                }

                ActiveScreen.RouteComparison -> {
                    RouteComparisonScreen(
                        routingResult = routeEvaluation,
                        shelter = shelter,
                        mapUiState = mapUiState,
                        onBack = { viewModel.navigateTo(ActiveScreen.Radar) },
                        onConfirmSafestRoute = { viewModel.navigateTo(ActiveScreen.LiveGuidance) }
                    )
                }

                ActiveScreen.LiveGuidance -> {
                    LiveGuidanceScreen(
                        shelter = shelter,
                        mapUiState = mapUiState,
                        onBack = { viewModel.navigateTo(ActiveScreen.Radar) },
                        onEndRoute = { viewModel.navigateTo(ActiveScreen.Radar) },
                        onShelterCheckIn = { viewModel.checkInToShelter() }
                    )
                }

                ActiveScreen.ShelterDetail -> {
                    SafeHavenDossierScreen(
                        shelter = shelter,
                        mapUiState = mapUiState,
                        onBack = { viewModel.selectTab(MobileTab.RADAR) },
                        onGetDirections = { viewModel.navigateTo(ActiveScreen.LiveGuidance) }
                    )
                }

                ActiveScreen.CitizenDossier -> {
                    CitizenDossierScreen(
                        hazard = hazard,
                        mapUiState = mapUiState,
                        onBack = { viewModel.selectTab(MobileTab.RADAR) },
                        onSelectDetour = { viewModel.navigateTo(ActiveScreen.RouteComparison) },
                        onConfirmFlooded = { viewModel.confirmHazardFlooded() },
                        onReportCleared = { viewModel.reportHazardCleared() }
                    )
                }

                ActiveScreen.SmsGateway -> {
                    SmsGatewayScreen(
                        onBack = { viewModel.navigateTo(ActiveScreen.Radar) }
                    )
                }

                ActiveScreen.AuthorityConsole -> {
                    AuthorityConsoleScreen(
                        hazard = hazard,
                        onBack = { viewModel.navigateTo(ActiveScreen.Radar) },
                        onApproveDetour = { viewModel.approveAndBroadcastDetour() }
                    )
                }

                ActiveScreen.Profile -> {
                    ProfileScreen(
                        userProfile = userProfile,
                        onToggleAccessibility = { key -> viewModel.toggleAccessibilityPreference(key) },
                        onOpenAuthorityConsole = { viewModel.navigateTo(ActiveScreen.AuthorityConsole) }
                    )
                }
            }
        }
    }
}
