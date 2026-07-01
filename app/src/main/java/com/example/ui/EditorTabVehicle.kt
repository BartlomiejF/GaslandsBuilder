@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.GaslandsStaticData
import com.example.ui.theme.*

@Composable
fun EditorTabVehicleType(
    editChassisId: String,
    onChassisChange: (String) -> Unit,
    editUpgrades: List<String>
) {
    val currentChassis = remember(editChassisId) {
        GaslandsStaticData.chassisList.find { it.id == editChassisId } ?: GaslandsStaticData.chassisList[2]
    }
    val armorCount = editUpgrades.map { it.split(":").first() }.count { it == "armour_plating" || it == "armor_plating" }
    val extraCrew = editUpgrades.map { it.split(":").first() }.count { it == "extra_crewmember" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selected chassis info card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonDarkElevated),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("selected_chassis_info_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACTIVE CHASSIS SPECIFICATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentChassis.name.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))
                    ChassisStatsRow(
                        weight = currentChassis.weight,
                        maxHull = com.example.data.models.calculateMaxHull(currentChassis.id, editUpgrades),
                        maxGear = com.example.data.models.calculateMaxGear(currentChassis.id, editUpgrades),
                        handling = com.example.data.models.calculateHandling(currentChassis.id, editUpgrades),
                        slotsTotal = com.example.data.models.calculateSlotsTotal(currentChassis.id, editUpgrades),
                        crew = currentChassis.crew + extraCrew
                    )
                    val hasTrailer = editUpgrades.map { it.split(":").first().trim() }
                        .any { it.startsWith("trailer", ignoreCase = true) }
                    val displayRules = buildTrailerRules(currentChassis.specialRules, hasTrailer)
                    if (displayRules.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("SPECIAL CHASSIS RULES", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            displayRules,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "CHOOSE VEHICLE CLASS",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(GaslandsStaticData.chassisList) { chassis ->
            val isSelected = chassis.id == editChassisId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChassisChange(chassis.id) }
                    .testTag("chassis_option_${chassis.id}"),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) CarbonDarkElevated else CarbonDark),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isSelected, onClick = { onChassisChange(chassis.id) })
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                chassis.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else TextLight
                            )
                        }
                        Text(
                            "${chassis.cost} Cans",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.padding(start = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Hull: ${chassis.maxHull}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("Max Gear: ${chassis.maxGear}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("Handling: ${chassis.handling}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("Slots: ${chassis.buildSlots}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun EditorTabSponsorSelect(
    editSponsorId: String,
    onSponsorChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "UNDERWRITE COLLISION TEAM SPONSOR",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(GaslandsStaticData.sponsorList) { sponsor ->
            val isSelected = sponsor.id == editSponsorId
            val sColorHex = if (sponsor.styleColorHex.startsWith("#")) sponsor.styleColorHex else "#90A4AE"
            val parsedColor = try {
                androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(sColorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSponsorChange(sponsor.id) }
                    .testTag("sponsor_option_${sponsor.id}"),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) CarbonDarkElevated else CarbonDark),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) parsedColor else Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isSelected, onClick = { onSponsorChange(sponsor.id) })
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sponsor.name.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (isSelected) parsedColor else TextLight
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = parsedColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PERKS: " + sponsor.perkClasses.joinToString(", ").uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = parsedColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sponsor.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) TextLight else TextMuted
                    )
                }
            }
        }
    }
}
