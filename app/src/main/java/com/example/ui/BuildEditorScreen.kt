@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildEditorScreen(
    viewModel: GaslandsViewModel,
    onBack: () -> Unit
) {
    val editingVehicleId by viewModel.editingVehicleId.collectAsStateWithLifecycle()
    val editName by viewModel.editName.collectAsStateWithLifecycle()
    val editSponsorId by viewModel.editSponsorId.collectAsStateWithLifecycle()
    val editChassisId by viewModel.editChassisId.collectAsStateWithLifecycle()
    val editWeapons by viewModel.editWeapons.collectAsStateWithLifecycle()
    val editUpgrades by viewModel.editUpgrades.collectAsStateWithLifecycle()
    val editNotes by viewModel.editNotes.collectAsStateWithLifecycle()

    val totalCans by viewModel.totalCans.collectAsStateWithLifecycle()
    val slotsTotal by viewModel.slotsTotal.collectAsStateWithLifecycle()
    val slotsUsed by viewModel.slotsUsed.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }
    var activeVehicleSubTab by remember { mutableStateOf(0) }
    var activeWeaponsSubTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingVehicleId == null) "BUILD GARAGE" else "FITTING SHACK") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            EditorBottomBar(
                totalCans = totalCans,
                slotsUsed = slotsUsed,
                slotsTotal = slotsTotal,
                onSave = { viewModel.saveVehicle() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Summary", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Vehicle", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("weapons, upgrades & perks", fontWeight = FontWeight.Bold) }
                )
            }

            if (slotsUsed > slotsTotal) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LaserRed.copy(alpha = 0.15f)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = LaserRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Warning: Slots Limit Exceeded! Check weapons slots fitting budget.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLight
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    0 -> EditorTabSummary(
                        editName = editName,
                        onNameChange = { viewModel.editName.value = it },
                        notes = editNotes,
                        onNotesChange = { viewModel.editNotes.value = it },
                        editChassisId = editChassisId,
                        editSponsorId = editSponsorId,
                        editWeapons = editWeapons,
                        editUpgrades = editUpgrades,
                        totalCans = totalCans,
                        slotsUsed = slotsUsed,
                        slotsTotal = slotsTotal
                    )
                    1 -> Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = activeVehicleSubTab,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Tab(
                                selected = activeVehicleSubTab == 0,
                                onClick = { activeVehicleSubTab = 0 },
                                text = { Text("TYPE", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("vehicle_subtab_type")
                            )
                            Tab(
                                selected = activeVehicleSubTab == 1,
                                onClick = { activeVehicleSubTab = 1 },
                                text = { Text("SPONSOR", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("vehicle_subtab_sponsor")
                            )
                        }
                        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                            if (activeVehicleSubTab == 0) {
                                EditorTabVehicleType(
                                    editChassisId = editChassisId,
                                    onChassisChange = { viewModel.editChassisId.value = it },
                                    editUpgrades = editUpgrades
                                )
                            } else {
                                EditorTabSponsorSelect(
                                    editSponsorId = editSponsorId,
                                    onSponsorChange = { viewModel.editSponsorId.value = it }
                                )
                            }
                        }
                    }
                    2 -> Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = activeWeaponsSubTab,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Tab(
                                selected = activeWeaponsSubTab == 0,
                                onClick = { activeWeaponsSubTab = 0 },
                                text = { Text("WEAPONS", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("weapons_subtab_weapons")
                            )
                            Tab(
                                selected = activeWeaponsSubTab == 1,
                                onClick = { activeWeaponsSubTab = 1 },
                                text = { Text("UPGRADES", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("weapons_subtab_upgrades")
                            )
                            Tab(
                                selected = activeWeaponsSubTab == 2,
                                onClick = { activeWeaponsSubTab = 2 },
                                text = { Text("PERKS", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("weapons_subtab_perks")
                            )
                        }
                        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                            when (activeWeaponsSubTab) {
                                0 -> EditorTabWeapons(viewModel = viewModel, selectedWeapons = editWeapons)
                                1 -> EditorTabUpgradesSelect(viewModel = viewModel, selectedUpgrades = editUpgrades)
                                2 -> EditorTabPerksSelect(
                                    viewModel = viewModel,
                                    editSponsorId = editSponsorId,
                                    selectedUpgrades = editUpgrades,
                                    onToggleUpgrade = { viewModel.toggleUpgradeSelection(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorBottomBar(
    totalCans: Int,
    slotsUsed: Int,
    slotsTotal: Int,
    onSave: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("LIVE CANS TOTAL", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$totalCans Cans",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                        color = if (slotsUsed > slotsTotal) LaserRed.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Slots: $slotsUsed/$slotsTotal",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (slotsUsed > slotsTotal) LaserRed else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Button(
                onClick = onSave,
                enabled = slotsUsed <= slotsTotal,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                modifier = Modifier.testTag("save_vehicle_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE BUILD")
            }
        }
    }
}
