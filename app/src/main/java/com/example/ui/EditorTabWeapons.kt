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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.GaslandsStaticData
import com.example.ui.theme.*

@Composable
fun EditorTabWeapons(
    viewModel: GaslandsViewModel,
    selectedWeapons: List<String>
) {
    val editUpgrades by viewModel.editUpgrades.collectAsStateWithLifecycle()
    val hasTrailer = editUpgrades.map { it.split(":").first().trim() }
        .any { it.startsWith("trailer", ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "2. MOUNT TACTICAL ARMAMENTS",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Weapons occupy slots and consume Cans. You may add multiple of the same weapons! Turret mount costs 3x Cans.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(GaslandsStaticData.weaponList) { weapon ->
            val fittedInstances = selectedWeapons.mapIndexedNotNull { index, item ->
                val parts = item.split(":")
                if (parts.first() == weapon.id) Pair(index, parts.getOrNull(1) ?: "Front") else null
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("weapon_option_${weapon.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (fittedInstances.isNotEmpty()) CarbonDarkElevated else CarbonDark
                ),
                border = BorderStroke(
                    1.dp,
                    if (fittedInstances.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Weapon header + add button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = weapon.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (fittedInstances.isNotEmpty()) MaterialTheme.colorScheme.primary else TextLight
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Slots: ${weapon.slots}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text("Cost: ${weapon.cost} Cans", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                Text("Dmg: ${weapon.damage}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                        Button(
                            onClick = { viewModel.addWeaponInstance(weapon.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_weapon_${weapon.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ADD", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    // Fitted instances
                    if (fittedInstances.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "FITTED INSTANCES (${fittedInstances.size})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            fittedInstances.forEachIndexed { num, (originalIndex, mount) ->
                                val isTurret = mount.equals("turret", ignoreCase = true)
                                val finalCost = if (isTurret) weapon.cost * 3 else weapon.cost
                                val isCrewFired = weapon.specialRules.contains("crew-fired", ignoreCase = true) || weapon.slots == 0

                                WeaponInstanceCard(
                                    num = num,
                                    mount = mount,
                                    finalCost = finalCost,
                                    isCrewFired = isCrewFired,
                                    hasTrailer = hasTrailer,
                                    weapon = weapon,
                                    originalIndex = originalIndex,
                                    onRemove = { viewModel.removeWeaponInstance(originalIndex) },
                                    onMountChange = { viewModel.updateWeaponInstanceMount(originalIndex, it) }
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
private fun WeaponInstanceCard(
    num: Int,
    mount: String,
    finalCost: Int,
    isCrewFired: Boolean,
    hasTrailer: Boolean,
    weapon: com.example.data.models.GaslandsWeapon,
    originalIndex: Int,
    onRemove: () -> Unit,
    onMountChange: (String) -> Unit
) {
    val hasTrailerPrefix = mount.startsWith("Trailer", ignoreCase = true)
    val baseDirection = if (hasTrailerPrefix) mount.substringAfter("Trailer").trim() else mount
    val isDropped = weapon.range.contains("dropped", ignoreCase = true)
    val validDirections = if (isDropped) listOf("Rear", "Side", "Turret") else listOf("Front", "Rear", "Side", "Turret")
    var currentDirection = baseDirection.replaceFirstChar { it.uppercase() }
    if (currentDirection !in validDirections) currentDirection = if (isDropped) "Rear" else "Front"

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
                    Text(
                        text = if (isCrewFired) "CREW-FIRED (No Mounting)" else "Mount: ${mount.uppercase()}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isCrewFired) MaterialTheme.colorScheme.tertiary else TextLight
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$finalCost Cans",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp).testTag("remove_weapon_${weapon.id}_$originalIndex")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = LaserRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (!isCrewFired) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Vehicle Mounting:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    validDirections.forEach { dir ->
                        val isCurrent = currentDirection.equals(dir, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val newMount = if (hasTrailerPrefix) "Trailer $dir" else dir
                                    onMountChange(newMount)
                                },
                            shape = RoundedCornerShape(4.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.secondary else CarbonDark,
                            border = BorderStroke(
                                1.dp,
                                if (isCurrent) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        ) {
                            Text(
                                text = dir.uppercase(),
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isCurrent) MaterialTheme.colorScheme.onSecondary else TextLight
                            )
                        }
                    }
                }

                if (hasTrailer) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mount on Trailer:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Surface(
                            modifier = Modifier.clickable {
                                val newMount = if (!hasTrailerPrefix) "Trailer $currentDirection" else currentDirection
                                onMountChange(newMount)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (hasTrailerPrefix) MaterialTheme.colorScheme.secondary else CarbonDark,
                            border = BorderStroke(
                                1.dp,
                                if (hasTrailerPrefix) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (hasTrailerPrefix) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (hasTrailerPrefix) MaterialTheme.colorScheme.onSecondary else TextMuted
                                )
                                Text(
                                    "TRAILER MOUNT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (hasTrailerPrefix) MaterialTheme.colorScheme.onSecondary else TextLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
