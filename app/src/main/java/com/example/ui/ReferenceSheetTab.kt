@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.GaslandsStaticData
import com.example.data.models.Vehicle
import com.example.ui.theme.*

@Composable
fun ReferenceSheetTab(vehicle: Vehicle) {
    val chassis = remember(vehicle.chassisId) {
        GaslandsStaticData.chassisList.find { it.id == vehicle.chassisId } ?: GaslandsStaticData.chassisList[2]
    }
    val sponsor = remember(vehicle.sponsorId) {
        GaslandsStaticData.sponsorList.find { it.id == vehicle.sponsorId } ?: GaslandsStaticData.sponsorList[0]
    }
    val parsedSponsorColor = parseSponsorColor(sponsor.styleColorHex)

    val maxHull = vehicle.getMaxHull()
    val adjustedCrew = vehicle.getCrew()

    val baseWeapons = if (vehicle.weaponsCsv.isEmpty()) emptyList() else vehicle.weaponsCsv.split(",")
    val weaponsList = if (baseWeapons.any { it.startsWith("handgun", ignoreCase = true) }) baseWeapons
    else baseWeapons + "handgun:Crew"
    val upgradesAndPerksList = if (vehicle.upgradesCsv.isEmpty()) emptyList() else vehicle.upgradesCsv.split(",")

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(
                "TABLETOP QUICK REFERENCE CHEAT-SHEET",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Vehicle specs
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ref_specs_card"),
                colors = CardDefaults.cardColors(containerColor = CarbonDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("VEHICLE SPECIFICATIONS", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CHASSIS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(chassis.name.uppercase(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("MAX HULL", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("$maxHull HP", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextLight)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("CREW", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("$adjustedCrew", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextLight)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text("HANDLING", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${vehicle.getHandling()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextLight)
                        }
                    }

                    val hasTrailer = upgradesAndPerksList.map { it.split(":").first().trim() }
                        .any { it.startsWith("trailer", ignoreCase = true) }
                    val displaySpecialRules = buildTrailerRules(chassis.specialRules, hasTrailer)
                    if (displaySpecialRules.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("SPECIAL RULES", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(displaySpecialRules, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.secondary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("SPONSOR", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(sponsor.name.uppercase(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = parsedSponsorColor)
                    if (sponsor.perkClasses.any { it.isNotEmpty() }) {
                        Text("Perk Classes: ${sponsor.perkClasses.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }

        // Arsenal reference
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ref_weapons_card"),
                colors = CardDefaults.cardColors(containerColor = CarbonDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("EQUIPPED WEAPONS ARSENAL", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (weaponsList.isEmpty() || weaponsList.all { it.isBlank() }) {
                        Text("No weapons equipped.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            weaponsList.forEach { weaponEntry ->
                                val parts = weaponEntry.split(":")
                                val wId = parts[0].trim()
                                val mount = parts.getOrNull(1)?.trim()
                                val weapon = GaslandsStaticData.weaponList.find { it.id == wId }
                                if (weapon != null) {
                                    val isCrewFired = weapon.specialRules.contains("crew-fired", ignoreCase = true) || weapon.slots == 0
                                    val mountText = if (mount != null) " (${mount.uppercase()})" else ""
                                    val crewFiredText = if (isCrewFired) " [CREW-FIRED]" else ""
                                    Column {
                                        Text(
                                            "• ${weapon.name}$mountText$crewFiredText",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextLight
                                        )
                                        val weaponMaxAmmo = if (vehicle.sponsorId == "rutherford" && weapon.maxAmmo == 3) 4 else weapon.maxAmmo
                                        Text(
                                            "Range: ${weapon.range} | Damage: ${weapon.damage} | Ammo: ${if (weaponMaxAmmo > 0) weaponMaxAmmo else "∞"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                        if (weapon.specialRules.isNotEmpty()) {
                                            Text(
                                                "Rules: ${weapon.specialRules}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upgrades & perks reference
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ref_upgrades_card"),
                colors = CardDefaults.cardColors(containerColor = CarbonDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("EQUIPPED UPGRADES & PERKS", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (upgradesAndPerksList.isEmpty() || upgradesAndPerksList.all { it.isBlank() }) {
                        Text("No upgrades or perks equipped.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            upgradesAndPerksList.forEach { upgradeEntry ->
                                val uId = upgradeEntry.split(":").first().trim()
                                val upgObj = GaslandsStaticData.upgradeList.find { it.id == uId }
                                val perkObj = GaslandsStaticData.perkList.find { it.id == uId }
                                when {
                                    upgObj != null -> Column {
                                        Text("• ${upgObj.name} (Upgrade)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextLight)
                                        Text(upgObj.description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    }
                                    perkObj != null -> Column {
                                        Text("• ${perkObj.name} (${perkObj.perkClass} Perk)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                                        Text("Class: ${perkObj.perkClass} | Cost: ${perkObj.cost} Cans", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            ReferenceSection(
                title = "SKID DICE SYMBOLS REFERENCE",
                content = "• SLIDE: Force slide check, gain 1 Hazard.\n" +
                        "• SPIN: Spin check, rotate car up to 90 degrees at exit line, gain 1 Hazard.\n" +
                        "• SHIFT: Modify gear up or down, or cancel 1 hazard template.\n" +
                        "• HAZARD: Gain 1 Hazard directly."
            )
        }
        item {
            ReferenceSection(
                title = "GEAR PHASE ACTIVATION SEQUENCE",
                content = "Gear Phase 1: All active cars move.\n" +
                        "Gear Phase 2: Cars in Gear 2 and above move.\n" +
                        "Gear Phase 3: Cars in Gear 3 and above move.\n" +
                        "Gear Phase 4: Cars in Gear 4 and above move."
            )
        }
    }
}

@Composable
fun ReferenceSection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp), color = TextLight.copy(alpha = 0.8f))
        }
    }
}
