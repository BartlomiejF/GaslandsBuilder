@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.GaslandsStaticData
import com.example.data.models.Vehicle
import com.example.ui.theme.*

@Composable
fun ArmamentTab(
    vehicle: Vehicle,
    onUseAmmo: (String) -> Unit,
    onReloadAmmo: (String) -> Unit
) {
    val baseWeapons = if (vehicle.weaponsCsv.isEmpty()) emptyList() else vehicle.weaponsCsv.split(",")
    val weaponsList = if (baseWeapons.any { it.startsWith("handgun", ignoreCase = true) }) baseWeapons
    else baseWeapons + "handgun:Crew"
    val upgradesList = if (vehicle.upgradesCsv.isEmpty()) emptyList() else vehicle.upgradesCsv.split(",")

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(
                "EQUIPPED ARSENAL & AMMO",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (weaponsList.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CarbonDark)) {
                    Text("No weapons equipped on build chassis.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
        } else {
            items(weaponsList) { weaponEntry ->
                val parts = weaponEntry.split(":")
                val weaponId = parts[0]
                val mount = parts.getOrNull(1)
                val weapon = GaslandsStaticData.weaponList.find { it.id == weaponId }
                if (weapon != null) {
                    val maxAmmo = if (vehicle.sponsorId == "rutherford" && weapon.maxAmmo == 3) 4 else weapon.maxAmmo
                    val ammoPartStr = vehicle.ammoMapCsv.split(",").find { it.startsWith("$weaponId:") }
                    val currentAmmo = ammoPartStr?.split(":")?.getOrNull(1)?.toIntOrNull() ?: maxAmmo
                    val isCrewFired = weapon.specialRules.contains("crew-fired", ignoreCase = true) || weapon.slots == 0

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("hud_weapon_card_$weaponId"),
                        colors = CardDefaults.cardColors(containerColor = CarbonDark)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            weapon.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (mount != null || isCrewFired) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val mountColor = if (isCrewFired) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = mountColor.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, mountColor.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = if (isCrewFired) "CREW-FIRED" else mount!!.uppercase(),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = mountColor
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        "Range: ${weapon.range}  |  Dmg: ${weapon.damage}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                                if (maxAmmo > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "AMMO: $currentAmmo / $maxAmmo",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (currentAmmo == 0) LaserRed else MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { onReloadAmmo(weaponId) },
                                            modifier = Modifier.size(28.dp).testTag("reload_ammo_$weaponId")
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                } else {
                                    Text("∞ AMMO", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                            }
                            if (weapon.specialRules.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Rule: " + weapon.specialRules, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                            if (maxAmmo > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onUseAmmo(weaponId) },
                                    enabled = currentAmmo > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth().testTag("fire_weapon_$weaponId")
                                ) { Text("FIRE WEAPON") }
                            }
                        }
                    }
                }
            }
        }

        val hasNitro = upgradesList.contains("nitro")
        if (hasNitro) {
            item {
                val ammoPartStr = vehicle.ammoMapCsv.split(",").find { it.startsWith("nitro:") }
                val currentNitro = ammoPartStr?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 1

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("hud_upgrade_card_nitro"),
                    colors = CardDefaults.cardColors(containerColor = CarbonDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Nitro Booster",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "BOOSTS: $currentNitro / 1",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (currentNitro == 0) LaserRed else MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onReloadAmmo("nitro") },
                                    modifier = Modifier.size(28.dp).testTag("reload_nitro")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onUseAmmo("nitro") },
                            enabled = currentNitro > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().testTag("fire_nitro")
                        ) { Text("ENGAGE NITRO BOOST (-1 BOOST)") }
                    }
                }
            }
        }
    }
}
