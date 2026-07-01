@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.GaslandsStaticData
import com.example.ui.theme.*

@Composable
fun EditorTabUpgradesSelect(
    viewModel: GaslandsViewModel,
    selectedUpgrades: List<String>
) {
    val currentChassis = viewModel.selectedChassis.collectAsStateWithLifecycle().value
    val basicCrew = currentChassis.crew
    val isLightweight = currentChassis.weight.equals("Lightweight", ignoreCase = true)
    val currentSponsorId = viewModel.editSponsorId.collectAsStateWithLifecycle().value

    val filteredUpgrades = remember {
        GaslandsStaticData.upgradeList
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "REINFORCE ENGINE UPGRADES",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(filteredUpgrades) { upgrade ->
            val isMultiBuy = upgrade.id == "ram" || upgrade.id == "exploding_ram" ||
                    upgrade.id == "armour_plating" || upgrade.id == "extra_crewmember"
            val upgradeIndices = selectedUpgrades.mapIndexedNotNull { index, item ->
                if (item.split(":").first() == upgrade.id) index else null
            }
            val currentCount = upgradeIndices.size

            val displaySlots = if (currentSponsorId == "slime" && upgrade.id == "ram") 0 else upgrade.slots
            val displayCost = when {
                currentSponsorId == "idris" && (upgrade.id == "nitro_booster" || upgrade.id == "nitro") -> upgrade.cost / 2
                currentSponsorId == "scarlett" && upgrade.id == "extra_crewmember" -> upgrade.cost / 2
                else -> upgrade.cost
            }

            if (isMultiBuy) {
                val isAddDisabled =
                    (upgrade.id == "extra_crewmember" && currentCount >= basicCrew) ||
                            (upgrade.id == "exploding_ram" && isLightweight)
                val disableMessage = when {
                    upgrade.id == "extra_crewmember" && currentCount >= basicCrew ->
                        "Max extra crew reached ($basicCrew) - equal to basic crew"
                    upgrade.id == "exploding_ram" && isLightweight ->
                        "Lightweight vehicles cannot mount exploding ram"
                    else -> null
                }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("upgrade_option_${upgrade.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentCount > 0) CarbonDarkElevated else CarbonDark
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (currentCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    upgrade.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (currentCount > 0) MaterialTheme.colorScheme.primary else TextLight
                                )
                                Text(upgrade.description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                if (displaySlots > 0) Text("Requires $displaySlots slot(s)", fontSize = 11.sp, color = LaserRed)
                                if (disableMessage != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(disableMessage, fontSize = 11.sp, color = LaserRed, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$displayCost Cans",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { viewModel.addUpgradeInstance(upgrade.id) },
                                    enabled = !isAddDisabled,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("add_upgrade_${upgrade.id}")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ADD ($currentCount)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        if (currentCount > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "PURCHASED INSTANCES ($currentCount)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextMuted
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                upgradeIndices.forEachIndexed { i, originalIndex ->
                                    val itemVal = selectedUpgrades[originalIndex]
                                    val facing = itemVal.split(":").getOrNull(1) ?: "Front"
                                    UpgradeInstanceCard(
                                        num = i,
                                        upgrade = upgrade,
                                        facing = facing,
                                        originalIndex = originalIndex,
                                        onRemove = { viewModel.removeUpgradeInstanceAt(originalIndex) },
                                        onFacingChange = { viewModel.updateUpgradeInstanceFacing(originalIndex, it) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                val isSelected = currentCount > 0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleUpgradeSelection(upgrade.id) }
                        .testTag("upgrade_option_${upgrade.id}"),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) CarbonDarkElevated else CarbonDark),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isSelected, onCheckedChange = { viewModel.toggleUpgradeSelection(upgrade.id) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    upgrade.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else TextLight
                                )
                                Text(
                                    "$displayCost Cans",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(upgrade.description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            if (displaySlots > 0) Text("Requires $displaySlots slot(s)", fontSize = 11.sp, color = LaserRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeInstanceCard(
    num: Int,
    upgrade: com.example.data.models.GaslandsUpgrade,
    facing: String,
    originalIndex: Int,
    onRemove: () -> Unit,
    onFacingChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "#${num + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val labelText = if (upgrade.id == "ram" || upgrade.id == "exploding_ram") "Facing: ${facing.uppercase()}" else "Equipped"
                    Text(labelText, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextLight)
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp).testTag("remove_upgrade_${upgrade.id}_$originalIndex")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = LaserRed, modifier = Modifier.size(16.dp))
                }
            }

            if (upgrade.id == "ram" || upgrade.id == "exploding_ram") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Front", "Rear", "Side").forEach { f ->
                        val isSel = facing.equals(f, ignoreCase = true)
                        Surface(
                            modifier = Modifier.weight(1f).clickable { onFacingChange(f) },
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSel) MaterialTheme.colorScheme.secondary else CarbonDark,
                            border = BorderStroke(
                                1.dp,
                                if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        ) {
                            Text(
                                text = f.uppercase(),
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSel) MaterialTheme.colorScheme.onSecondary else TextLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditorTabPerksSelect(
    viewModel: GaslandsViewModel,
    editSponsorId: String,
    selectedUpgrades: List<String>,
    onToggleUpgrade: (String) -> Unit
) {
    val groupedPerks = remember { GaslandsStaticData.perkList.groupBy { it.perkClass } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "EQUIP SPONSOR & CUSTOM PERKS",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            val curSponsor = GaslandsStaticData.sponsorList.find { it.id == editSponsorId } ?: GaslandsStaticData.sponsorList[0]
            val permittedStr = curSponsor.perkClasses.filter { it.isNotEmpty() }.joinToString(", ")
            Text(
                text = "Permitted Classes for ${curSponsor.name}: $permittedStr",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        groupedPerks.forEach { (perkClass, perks) ->
            item {
                val curSponsor = GaslandsStaticData.sponsorList.find { it.id == editSponsorId } ?: GaslandsStaticData.sponsorList[0]
                val isPermitted = curSponsor.perkClasses.any { it.equals(perkClass, ignoreCase = true) }
                Surface(
                    color = if (isPermitted) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else CarbonDarkElevated,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = perkClass.uppercase() + " CLASS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isPermitted) MaterialTheme.colorScheme.primary else TextLight
                        )
                        if (isPermitted) {
                            Text(
                                text = "RECOMMENDED CLASS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            items(perks) { perk ->
                val isSelected = selectedUpgrades.contains(perk.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleUpgrade(perk.id) }
                        .testTag("perk_option_${perk.id}"),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) CarbonDarkElevated else CarbonDark),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isSelected, onCheckedChange = { onToggleUpgrade(perk.id) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    perk.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else TextLight
                                )
                                Text(
                                    "${perk.cost} Cans",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
