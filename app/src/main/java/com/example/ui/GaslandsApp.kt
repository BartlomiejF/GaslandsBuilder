@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GaslandsApp(viewModel: GaslandsViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenNavigation"
        ) { screen ->
            when (screen) {
                is AppScreen.ListScreen -> GarageScreen(
                    vehicles = vehicles,
                    onAddNew = { viewModel.startNewBuild() },
                    onEdit = { viewModel.startEditBuild(it) },
                    onDelete = { viewModel.deleteVehicle(it) },
                    onLaunchDashboard = { viewModel.navigateTo(AppScreen.DashboardScreen(it)) }
                )
                is AppScreen.BuildEditorScreen -> BuildEditorScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(AppScreen.ListScreen) }
                )
                is AppScreen.DashboardScreen -> DashboardScreen(
                    vehicleIds = screen.vehicleIds,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(AppScreen.ListScreen) }
                )
            }
        }
    }
}
