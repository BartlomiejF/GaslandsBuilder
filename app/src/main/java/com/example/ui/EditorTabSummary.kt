@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.models.GaslandsStaticData
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTabSummary(
    editName: String,
    onNameChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    editChassisId: String,
    editSponsorId: String,
    editWeapons: List<String>,
    editUpgrades: List<String>,
    totalCans: Int,
    slotsUsed: Int,
    slotsTotal: Int
) {
    val currentChassis = remember(editChassisId) {
        GaslandsStaticData.chassisList.find { it.id == editChassisId } ?: GaslandsStaticData.chassisList[2]
    }
    val currentSponsor = remember(editSponsorId) {
        GaslandsStaticData.sponsorList.find { it.id == editSponsorId } ?: GaslandsStaticData.sponsorList[0]
    }

    val armorCount = editUpgrades.map { it.split(":").first() }.count { it == "armour_plating" || it == "armor_plating" }
    val extraCrew = editUpgrades.map { it.split(":").first() }.count { it == "extra_crewmember" }
    val adjustedMaxHull = com.example.data.models.calculateMaxHull(currentChassis.id, editUpgrades)
    val adjustedCrew = currentChassis.crew + extraCrew

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Vehicle name input
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("vehicle_name_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VEHICLE DESIGNATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = onNameChange,
                        label = { Text("Vehicle Designation") },
                        placeholder = { Text("E.g. Shrapnel Racer, Fire Storm") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("vehicle_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
            }
        }

        // 2. Chassis specs overview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonDarkElevated),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CHASSIS / VEHICLE CLASS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                currentChassis.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$totalCans CANS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(10.dp))
                    ChassisStatsRow(
                        weight = currentChassis.weight,
                        maxHull = adjustedMaxHull,
                        maxGear = com.example.data.models.calculateMaxGear(currentChassis.id, editUpgrades),
                        handling = com.example.data.models.calculateHandling(currentChassis.id, editUpgrades),
                        slotsTotal = slotsTotal,
                        crew = adjustedCrew
                    )

                    val hasTrailer = editUpgrades.map { it.split(":").first().trim() }
                        .any { it.startsWith("trailer", ignoreCase = true) }
                    val displaySpecialRules = buildTrailerRules(currentChassis.specialRules, hasTrailer)
                    if (displaySpecialRules.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("SPECIAL CHASSIS RULES", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            displaySpecialRules,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // 3. Active sponsor
        item {
            val sColorHex = if (currentSponsor.styleColorHex.startsWith("#")) currentSponsor.styleColorHex else "#90A4AE"
            val parsedColor = try {
                androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(sColorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonDark),
                border = BorderStroke(1.dp, parsedColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACTIVE COALITION SPONSOR", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = currentSponsor.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = parsedColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = currentSponsor.description, style = MaterialTheme.typography.bodySmall, color = TextLight)

                    val sponsoredPerks = getSponsoredPerks(currentSponsor)
                    if (sponsoredPerks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SPONSORED PERKS (FOR IN-GAME ACTIONS)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = parsedColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sponsoredPerks.joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // 4. Fitted weapons list
        item {
            Text(
                text = "FITTED TACTICAL WEAPONS (${editWeapons.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (editWeapons.isEmpty()) {
                Surface(color = CarbonDark, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No weapons fitted. Use the Weapons tab to select tactical weapons.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    editWeapons.forEach { weaponItem ->
                        val parts = weaponItem.split(":")
                        val wId = parts.first()
                        val mount = parts.getOrNull(1) ?: "Front"
                        val weapon = GaslandsStaticData.weaponList.find { it.id == wId }
                        if (weapon != null) {
                            val isTurret = mount.equals("turret", ignoreCase = true)
                            val finalCost = if (isTurret) weapon.cost * 3 else weapon.cost
                            val isCrewFired = weapon.specialRules.contains("crew-fired", ignoreCase = true) || weapon.slots == 0
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CarbonDark),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            weapon.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextLight
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(
                                                text = if (isCrewFired) "CREW-FIRED" else "Mount: ${mount.uppercase()}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isCrewFired) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                            )
                                            if (weapon.slots > 0) {
                                                Text("Slots: ${weapon.slots}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            }
                                        }
                                    }
                                    Text(
                                        "$finalCost Cans",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Perks & upgrades list
        item {
            val upgradesList = editUpgrades.mapNotNull { uId ->
                val upgObj = GaslandsStaticData.upgradeList.find { it.id == uId }
                val perkObj = GaslandsStaticData.perkList.find { it.id == uId }
                if (upgObj != null) Pair(upgObj.name, "${upgObj.cost} Cans (Upgrade)")
                else if (perkObj != null) Pair(perkObj.name, "${perkObj.cost} Cans (${perkObj.perkClass} Perk)")
                else null
            }
            Text(
                text = "EQUIPPED PERKS & UPGRADES (${upgradesList.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (upgradesList.isEmpty()) {
                Surface(color = CarbonDark, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No upgrades or perks equipped. Use the Perks & Sponsors tab.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    upgradesList.forEach { (name, label) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonDark),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextLight)
                                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }

        // 6. Build notes
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("vehicle_notes_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GARAGE CUSTOM BUILD NOTES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        label = { Text("Garage custom build notes") },
                        placeholder = { Text("Enter driver background, modifications or custom rules...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("vehicle_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        ),
                        maxLines = 4
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
internal fun ChassisStatsRow(
    weight: String,
    maxHull: Int,
    maxGear: Int,
    handling: Int,
    slotsTotal: Int,
    crew: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatCell("WEIGHT", weight.uppercase())
            StatCell("MAX HULL", "$maxHull")
            StatCell("MAX GEAR", "$maxGear")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatCell("HANDLING", "$handling")
            StatCell("BUILD SLOTS", "$slotsTotal")
            StatCell("CREW", "$crew")
        }
    }
}

@Composable
internal fun RowScope.StatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextLight)
    }
}

internal fun buildTrailerRules(baseRules: String, hasTrailer: Boolean): String {
    if (!hasTrailer) return baseRules
    return if (baseRules.isNotEmpty()) "$baseRules, articulated, pondering, piledriver"
    else "articulated, pondering, piledriver"
}
