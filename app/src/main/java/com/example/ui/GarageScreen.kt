@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.GaslandsStaticData
import com.example.data.models.Vehicle
import com.example.ui.theme.*

@Composable
fun GarageScreen(
    vehicles: List<Vehicle>,
    onAddNew: () -> Unit,
    onEdit: (Vehicle) -> Unit,
    onDelete: (Vehicle) -> Unit,
    onLaunchDashboard: (List<Int>) -> Unit
) {
    val selectedIds = remember { mutableStateListOf<Int>() }
    var activeTab by remember { mutableStateOf(0) } // 0 = Garage, 1 = About

    Scaffold(
        floatingActionButton = {
            if (activeTab == 0) {
                if (selectedIds.isEmpty()) {
                    FloatingActionButton(
                        onClick = onAddNew,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("add_vehicle_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Custom Vehicle")
                    }
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { onLaunchDashboard(selectedIds.toList()) },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.testTag("launch_squad_fab"),
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        text = { Text("Launch Squad (${selectedIds.size})") }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CarbonDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    label = { Text("Garage", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_garage_btn")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("About", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RustOrange,
                        selectedTextColor = RustOrange,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = RustOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_about_btn")
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(colors = listOf(WastelandBlack, Color(0xFF0C0E12)))
                )
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (activeTab == 0) {
                    if (vehicles.isEmpty()) {
                        EmptyStateView(onAddNew)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item { TipsBanner() }
                            items(vehicles, key = { it.id }) { vehicle ->
                                val isSelected = selectedIds.contains(vehicle.id)
                                VehicleConfigCard(
                                    vehicle = vehicle,
                                    isSelected = isSelected,
                                    onSelectToggle = {
                                        if (isSelected) selectedIds.remove(vehicle.id)
                                        else selectedIds.add(vehicle.id)
                                    },
                                    onEdit = { onEdit(vehicle) },
                                    onDelete = { onDelete(vehicle) },
                                    onLaunchDashboard = { onLaunchDashboard(listOf(vehicle.id)) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                } else {
                    AboutTab()
                }
            }
        }
    }
}

@Composable
fun TipsBanner() {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("tips_banner"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Player Cockpit Included",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Launch active play (▶) to control gear speed, damage points, hazard wipeouts and rocket/booster ammunition.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(onAddNew: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "GARAGE IS VACANT",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Gaslands Companion persistent SQLite storage is completely offline and ready. Design multi-slot war rigs and enter the active play arena.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddNew,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ASSEMBLE CUSTOM ENGINE")
        }
    }
}

@Composable
fun VehicleConfigCard(
    vehicle: Vehicle,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLaunchDashboard: () -> Unit
) {
    val chassis = GaslandsStaticData.chassisList.find { it.id == vehicle.chassisId }
        ?: GaslandsStaticData.chassisList[2]
    val sponsor = GaslandsStaticData.sponsorList.find { it.id == vehicle.sponsorId }
        ?: GaslandsStaticData.sponsorList[0]
    val sponsorColor = Color(android.graphics.Color.parseColor(sponsor.styleColorHex))

    Card(
        modifier = Modifier.fillMaxWidth().testTag("vehicle_card_${vehicle.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header row: checkbox + name/sponsor + play button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectToggle() },
                        modifier = Modifier.testTag("vehicle_select_${vehicle.id}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = vehicle.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = sponsorColor.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, sponsorColor.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    text = sponsor.name.uppercase(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = sponsorColor
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${chassis.name} • ${chassis.weight}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        val sponsoredPerks = getSponsoredPerks(sponsor)
                        if (sponsoredPerks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sponsored Perks: " + sponsoredPerks.joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onLaunchDashboard,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(36.dp)
                        .testTag("play_hud_button_${vehicle.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Active HUD",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Specs row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("COST", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        "${vehicle.calculateTotalCans()} Cans",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HULL", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    val maxHullWithArmor = vehicle.getMaxHull()
                    Text(
                        "${if (vehicle.currentHull == -1) maxHullWithArmor else vehicle.currentHull} / $maxHullWithArmor",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LaserRed
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HANDLING", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        "${vehicle.getHandling()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CREW", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    val maxCrewVal = vehicle.getCrew()
                    val currentCrewVal = if (vehicle.currentCrew == -1) maxCrewVal else vehicle.currentCrew
                    Text(
                        "$currentCrewVal / $maxCrewVal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val slotsUsed = vehicle.getSlotsUsed()
                    val currentSlotsTotal = vehicle.getSlotsTotal()

                    Text("SLOTS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        "$slotsUsed/$currentSlotsTotal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (slotsUsed > currentSlotsTotal) LaserRed else TextLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weapons / upgrades summary
            if (vehicle.weaponsCsv.isNotEmpty() || vehicle.upgradesCsv.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (vehicle.weaponsCsv.isNotEmpty()) {
                        val names = vehicle.weaponsCsv.split(",").mapNotNull { item ->
                            val parts = item.split(":")
                            val wId = parts.first()
                            val mount = parts.getOrNull(1)
                            val weaponObj = GaslandsStaticData.weaponList.find { it.id == wId }
                            if (weaponObj != null) {
                                val isCrewFired = weaponObj.specialRules.contains("crew-fired", ignoreCase = true) || weaponObj.slots == 0
                                if (mount != null && !isCrewFired) "${weaponObj.name} ($mount)" else weaponObj.name
                            } else null
                        }
                        Text(
                            "Weapons: " + names.joinToString(", "),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLight.copy(alpha = 0.7f)
                        )
                    }
                    if (vehicle.upgradesCsv.isNotEmpty()) {
                        val names = vehicle.upgradesCsv.split(",").mapNotNull { uId ->
                            val cleanId = uId.trim().split(":").first()
                            GaslandsStaticData.upgradeList.find { it.id == cleanId }?.name
                                ?: GaslandsStaticData.perkList.find { it.id == cleanId }?.name
                        }
                        Text(
                            "Upgrades/Perks: " + names.joinToString(", "),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLight.copy(alpha = 0.7f)
                        )
                    }
                    val localUpgIds = if (vehicle.upgradesCsv.isEmpty()) emptyList()
                    else vehicle.upgradesCsv.split(",").map { it.split(":").first().trim() }
                    val displaySpecialRules = if (localUpgIds.any { it.startsWith("trailer", ignoreCase = true) }) {
                        if (chassis.specialRules.isNotEmpty()) "${chassis.specialRules}, articulated, pondering, piledriver"
                        else "articulated, pondering, piledriver"
                    } else {
                        chassis.specialRules
                    }
                    if (displaySpecialRules.isNotEmpty()) {
                        Text(
                            "Rules: $displaySpecialRules",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_button_${vehicle.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EDIT")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_button_${vehicle.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LaserRed.copy(alpha = 0.8f))
                }
            }
        }
    }
}
