@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.GaslandsStaticData
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vehicleIds: List<Int>,
    viewModel: GaslandsViewModel,
    onBack: () -> Unit
) {
    val activeVehicles by viewModel.activeVehiclesFlow.collectAsStateWithLifecycle()

    LaunchedEffect(vehicleIds) {
        viewModel.loadActiveVehicles(vehicleIds)
    }

    if (activeVehicles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var selectedVehicleId by remember(vehicleIds) { mutableStateOf(vehicleIds.firstOrNull()) }
    val vehicle = activeVehicles.find { it.id == selectedVehicleId } ?: activeVehicles[0]
    val chassis = GaslandsStaticData.chassisList.find { it.id == vehicle.chassisId } ?: GaslandsStaticData.chassisList[2]
    val maxHull = vehicle.getMaxHull()

    var currentViewTab by remember { mutableStateOf(0) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    BackHandler { showExitConfirmation = true }

    if (showExitConfirmation) {
        ExitCockpitDialog(
            onConfirm = {
                showExitConfirmation = false
                viewModel.resetActiveVehiclesAndExit(onBack)
            },
            onDismiss = { showExitConfirmation = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "PLAYER COCKPIT",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        )
                        Text(
                            "Active Squad • ${activeVehicles.size} Vehicles Deployed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitConfirmation = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Cockpit HUD")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(WastelandBlack)
        ) {
            if (activeVehicles.size > 1) {
                SquadSwitcherRow(
                    activeVehicles = activeVehicles,
                    selectedVehicleId = vehicle.id,
                    onSelect = { selectedVehicleId = it }
                )
            }

            SharedControlsRow(
                vehicle = vehicle,
                onSpectatorVotesChange = { viewModel.updateSpectatorVotes(it) },
                onTurnGearChange = { viewModel.updateCurrentTurnGear(it) }
            )

            TabRow(
                selectedTabIndex = currentViewTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = currentViewTab == 0, onClick = { currentViewTab = 0 }, text = {
                    Text(vehicle.name.uppercase(), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                })
                Tab(selected = currentViewTab == 1, onClick = { currentViewTab = 1 }, text = {
                    Text("ARMAMENT", fontWeight = FontWeight.Bold)
                })
                Tab(selected = currentViewTab == 2, onClick = { currentViewTab = 2 }, text = {
                    Text("REF SHEET", fontWeight = FontWeight.Bold)
                })
            }

            Box(modifier = Modifier.fillMaxSize().weight(1f).padding(16.dp)) {
                when (currentViewTab) {
                    0 -> CockpitHudTab(
                        vehicle = vehicle,
                        maxGear = vehicle.getMaxGear(),
                        maxHull = maxHull,
                        onGearChange = { viewModel.updateGear(vehicle, it) },
                        onHazardChange = { viewModel.updateHazards(vehicle, it) },
                        onResetHazards = { viewModel.resetHazards(vehicle) },
                        onHullChange = { viewModel.updateHull(vehicle, it) },
                        onFireChange = { viewModel.updateFireState(vehicle, it) },
                        onActivatedMarkerChange = { viewModel.updateActivatedInCurrentGear(vehicle, it) },
                        onCrewChange = { viewModel.updateCrew(vehicle, it) }
                    )
                    1 -> ArmamentTab(
                        vehicle = vehicle,
                        onUseAmmo = { viewModel.toggleUseAmmo(vehicle, it) },
                        onReloadAmmo = { viewModel.reloadAmmo(vehicle, it) }
                    )
                    2 -> ReferenceSheetTab(vehicle)
                }
            }
        }
    }
}

@Composable
private fun ExitCockpitDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = LaserRed, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SHUTDOWN COCKPIT?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = TextLight)
            }
        },
        text = {
            Text(
                text = "Are you sure you want to end this combat run and power down the dashboard cockpit? Your crew's current hull integrity, ammo, active gear, and hazard parameters will be reset to default ready status for the next battle run.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = LaserRed)) {
                Text("POWER DOWN & EXIT", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("RESUME BATTLE", fontWeight = FontWeight.Bold, color = TextLight)
            }
        },
        containerColor = CarbonDark,
        tonalElevation = 6.dp
    )
}

@Composable
private fun SquadSwitcherRow(
    activeVehicles: List<com.example.data.models.Vehicle>,
    selectedVehicleId: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(CarbonDark)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(activeVehicles) { squadVehicle ->
            val isCurrent = squadVehicle.id == selectedVehicleId
            val sChassis = GaslandsStaticData.chassisList.find { it.id == squadVehicle.chassisId } ?: GaslandsStaticData.chassisList[2]
            val sMaxHull = squadVehicle.getMaxHull()

            Surface(
                modifier = Modifier.clickable { onSelect(squadVehicle.id) }.widthIn(min = 120.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isCurrent) MaterialTheme.colorScheme.primary else CarbonDarkElevated,
                border = BorderStroke(1.dp, if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(
                        text = squadVehicle.name.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else TextLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val textColor = if (isCurrent) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondary
                        Text("G:${squadVehicle.currentGear}", style = MaterialTheme.typography.labelSmall, color = textColor)
                        Text(
                            "H:${if (squadVehicle.currentHull == -1) sMaxHull else squadVehicle.currentHull}/$sMaxHull",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else LaserRed
                        )
                        Text(
                            "Hz:${squadVehicle.currentHazards}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else RustOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedControlsRow(
    vehicle: com.example.data.models.Vehicle,
    onSpectatorVotesChange: (Int) -> Unit,
    onTurnGearChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WastelandBlack)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CounterCard(
            modifier = Modifier.weight(1f),
            label = "SPECTATOR VOTES",
            value = vehicle.spectatorVotes,
            valueColor = MaterialTheme.colorScheme.secondary,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            plusColor = MaterialTheme.colorScheme.secondary,
            testTagDown = "votes_down_btn",
            testTagUp = "votes_up_btn",
            testTagCard = "spectator_votes_card",
            onDecrement = { onSpectatorVotesChange(vehicle.spectatorVotes - 1) },
            onIncrement = { onSpectatorVotesChange(vehicle.spectatorVotes + 1) }
        )
        CounterCard(
            modifier = Modifier.weight(1f),
            label = "GAMETURN GEAR",
            value = vehicle.currentTurnGear,
            valueColor = MaterialTheme.colorScheme.primary,
            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            plusColor = MaterialTheme.colorScheme.primary,
            testTagDown = "turn_gear_down_btn",
            testTagUp = "turn_gear_up_btn",
            testTagCard = "gameturn_gear_card",
            onDecrement = { onTurnGearChange(vehicle.currentTurnGear - 1) },
            onIncrement = { onTurnGearChange(vehicle.currentTurnGear + 1) }
        )
    }
}

@Composable
private fun CounterCard(
    modifier: Modifier,
    label: String,
    value: Int,
    valueColor: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color,
    plusColor: androidx.compose.ui.graphics.Color,
    testTagDown: String,
    testTagUp: String,
    testTagCard: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Card(
        modifier = modifier.then(Modifier.then(Modifier.then(Modifier.then(Modifier)))).testTag(testTagCard),
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = valueColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledIconButton(onClick = onDecrement, modifier = Modifier.size(32.dp).testTag(testTagDown)) {
                    Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = valueColor
                )
                FilledIconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(32.dp).testTag(testTagUp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = plusColor)
                ) {
                    Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
