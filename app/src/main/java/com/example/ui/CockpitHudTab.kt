@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.GaslandsStaticData
import com.example.data.models.Vehicle
import com.example.ui.theme.*

@Composable
fun CockpitHudTab(
    vehicle: Vehicle,
    maxGear: Int,
    maxHull: Int,
    onGearChange: (Int) -> Unit,
    onHazardChange: (Int) -> Unit,
    onResetHazards: () -> Unit,
    onHullChange: (Int) -> Unit,
    onFireChange: (Boolean) -> Unit,
    onActivatedMarkerChange: (Boolean) -> Unit,
    onCrewChange: (Int) -> Unit
) {
    val chassis = remember(vehicle.chassisId) {
        GaslandsStaticData.chassisList.find { it.id == vehicle.chassisId } ?: GaslandsStaticData.chassisList[2]
    }
    val extraCrewCount = remember(vehicle.upgradesCsv) {
        vehicle.upgradesCsv.split(",").map { it.trim().split(":").first() }
            .count { it == "extra_crewmember" || it == "extra_crew" }
    }
    val adjustedCrew = chassis.crew + extraCrewCount
    val currentCrew = if (vehicle.currentCrew == -1) adjustedCrew else vehicle.currentCrew

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sponsor banner
        item {
            val sponsorObj = remember(vehicle.sponsorId) {
                GaslandsStaticData.sponsorList.find { it.id == vehicle.sponsorId } ?: GaslandsStaticData.sponsorList[0]
            }
            val parsedSponsorColor = parseSponsorColor(sponsorObj.styleColorHex)
            Card(
                modifier = Modifier.fillMaxWidth().testTag("cockpit_sponsor_banner"),
                colors = CardDefaults.cardColors(containerColor = CarbonDark),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, parsedSponsorColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ACTIVE SPONSOR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = TextMuted)
                    Text(sponsorObj.name.uppercase(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold), color = parsedSponsorColor)
                }
            }
        }

        // Gear + Hazards row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BigCounterCard(
                    modifier = Modifier.weight(1f),
                    label = "ACTIVE GEAR",
                    value = vehicle.currentGear,
                    valueColor = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    testTagCard = "gear_shifter_card",
                    testTagDown = "gear_down_btn",
                    testTagUp = "gear_up_btn",
                    upColor = MaterialTheme.colorScheme.primary,
                    onDecrement = { onGearChange(vehicle.currentGear - 1) },
                    onIncrement = { onGearChange(vehicle.currentGear + 1) }
                )
                BigCounterCard(
                    modifier = Modifier.weight(1f),
                    label = "HAZARDS",
                    value = vehicle.currentHazards,
                    valueColor = if (vehicle.currentHazards >= 6) LaserRed else if (vehicle.currentHazards >= 4) SandYellow else TextLight,
                    borderColor = if (vehicle.currentHazards >= 5) LaserRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    labelColor = if (vehicle.currentHazards >= 5) LaserRed else MaterialTheme.colorScheme.secondary,
                    testTagCard = "hazards_tracker_card",
                    testTagDown = "hazard_down_btn",
                    testTagUp = "hazard_up_btn",
                    upColor = if (vehicle.currentHazards >= 5) LaserRed else MaterialTheme.colorScheme.secondary,
                    onDecrement = { onHazardChange(vehicle.currentHazards - 1) },
                    onIncrement = { onHazardChange(vehicle.currentHazards + 1) }
                )
            }
        }

        // Handling + Crew row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatDisplayCard(
                    modifier = Modifier.weight(1f),
                    label = "HANDLING",
                    value = "${vehicle.getHandling()}",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) },
                    testTag = "specs_handling_card"
                )
                CrewCounterCard(
                    modifier = Modifier.weight(1f),
                    currentCrew = currentCrew,
                    maxCrew = adjustedCrew,
                    onCrewChange = onCrewChange
                )
            }
        }

        // Activation + Fire marker row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ToggleMarkerCard(
                    modifier = Modifier.weight(1f),
                    label = "ACTIVATION",
                    isActive = vehicle.activatedInCurrentGear,
                    activeColor = MaterialTheme.colorScheme.primary,
                    activeText = "Activated",
                    inactiveText = "Pending",
                    testTag = "activation_marker_card",
                    onClick = { onActivatedMarkerChange(!vehicle.activatedInCurrentGear) },
                    centerContent = {
                        Box(
                            modifier = Modifier.size(36.dp).background(
                                if (vehicle.activatedInCurrentGear) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (vehicle.activatedInCurrentGear) "ON" else "OFF",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CarbonDark)
                            )
                        }
                    }
                )
                ToggleMarkerCard(
                    modifier = Modifier.weight(1f),
                    label = "FIRE MARKER",
                    isActive = vehicle.isOnFire,
                    activeColor = LaserRed,
                    activeText = "ON FIRE!",
                    inactiveText = "Clear",
                    testTag = "fire_marker_card",
                    onClick = { onFireChange(!vehicle.isOnFire) },
                    centerContent = {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Flame Fire Marker",
                            tint = if (vehicle.isOnFire) LaserRed else Color.Gray,
                            modifier = Modifier.size(36.dp).testTag("fire_icon_toggle")
                        )
                    }
                )
            }
        }

        // Wipeout banner
        if (vehicle.currentHazards >= 6) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("wipe_out_banner"),
                    colors = CardDefaults.cardColors(containerColor = LaserRed.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LaserRed)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = LaserRed, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("WIPE OUT!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LaserRed)
                        Text(
                            "Wipeout triggered! Drop speed elements to Gear 1, gain no movement and clear Hazards.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onResetHazards,
                            colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                            modifier = Modifier.testTag("wipe_out_reset_button")
                        ) { Text("RESET HAZARDS") }
                    }
                }
            }
        }

        // Hull integrity
        item {
            val currentHull = if (vehicle.currentHull == -1) maxHull else vehicle.currentHull
            Card(
                modifier = Modifier.fillMaxWidth().testTag("hull_points_card"),
                colors = CardDefaults.cardColors(containerColor = CarbonDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("HULL INTEGRITY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            Text("Damage state indicator", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Text(
                            "$currentHull / $maxHull HP",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (currentHull <= maxHull / 3) LaserRed else TextLight
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val progressFraction = if (maxHull > 0) (currentHull.toFloat() / maxHull.toFloat()).coerceIn(0f, 1f) else 1f
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = if (progressFraction <= 0.33f) LaserRed else if (progressFraction <= 0.6f) SandYellow else HazardGreen,
                        trackColor = MaterialTheme.colorScheme.background
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (currentHull <= 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LaserRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("destroyed_banner")
                        ) {
                            Text(
                                "CRITICAL: VEHICLE SLAG & DESTROYED",
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = LaserRed
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(
                            onClick = { onHullChange(currentHull - 1) },
                            colors = ButtonDefaults.buttonColors(containerColor = CarbonDarkElevated),
                            modifier = Modifier.weight(1f).padding(end = 8.dp).testTag("hull_down_btn")
                        ) { Text("HIT (-1 HP)") }
                        Button(
                            onClick = { onHullChange(currentHull + 1) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).padding(start = 8.dp).testTag("hull_up_btn")
                        ) { Text("REPAIR (+1 HP)") }
                    }
                }
            }
        }
    }
}

// Helper composables

@Composable
private fun BigCounterCard(
    modifier: Modifier,
    label: String,
    value: Int,
    valueColor: Color,
    borderColor: Color,
    labelColor: Color = MaterialTheme.colorScheme.secondary,
    upColor: Color,
    testTagCard: String,
    testTagDown: String,
    testTagUp: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Card(
        modifier = modifier.testTag(testTagCard),
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = labelColor)
            Spacer(modifier = Modifier.height(12.dp))
            Text("$value", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 56.sp), color = valueColor)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilledIconButton(onClick = onDecrement, modifier = Modifier.size(40.dp).testTag(testTagDown)) {
                    Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                FilledIconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(40.dp).testTag(testTagUp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = upColor)
                ) {
                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatDisplayCard(
    modifier: Modifier,
    label: String,
    value: String,
    accentColor: Color,
    icon: @Composable () -> Unit,
    testTag: String
) {
    Card(
        modifier = modifier.testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accentColor)
            Box(
                modifier = Modifier.size(36.dp).background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) { icon() }
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = TextLight)
        }
    }
}

@Composable
private fun ToggleMarkerCard(
    modifier: Modifier,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    activeText: String,
    inactiveText: String,
    testTag: String,
    onClick: () -> Unit,
    centerContent: @Composable () -> Unit
) {
    Card(
        modifier = modifier.testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) activeColor.copy(alpha = 0.15f) else CarbonDark
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isActive) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (isActive) activeColor else TextLight)
            centerContent()
            Text(
                if (isActive) activeText else inactiveText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) activeColor else TextMuted
            )
        }
    }
}

@Composable
private fun CrewCounterCard(
    modifier: Modifier,
    currentCrew: Int,
    maxCrew: Int,
    onCrewChange: (Int) -> Unit
) {
    Card(
        modifier = modifier.testTag("specs_crew_card"),
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "ACTIVE CREW",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "$currentCrew / $maxCrew",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextLight
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { onCrewChange(currentCrew - 1) },
                    modifier = Modifier.size(28.dp).testTag("crew_down_btn"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CarbonDarkElevated,
                        contentColor = TextLight
                    )
                ) {
                    Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                
                FilledIconButton(
                    onClick = { onCrewChange(currentCrew + 1) },
                    modifier = Modifier.size(28.dp).testTag("crew_up_btn"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

internal fun parseSponsorColor(hex: String): Color {
    val safeHex = if (hex.startsWith("#")) hex else "#90A4AE"
    return try { Color(android.graphics.Color.parseColor(safeHex)) } catch (e: Exception) { Color(0xFF90A4AE) }
}
