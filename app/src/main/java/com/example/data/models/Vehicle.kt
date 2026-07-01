package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

fun calculateMaxHull(chassisId: String, upgradesList: List<String>): Int {
    val chassis = GaslandsStaticData.chassisList.find { it.id == chassisId } ?: return 0
    val parsedUpgrades = upgradesList.map { it.split(":").first() }
    val armorCount = parsedUpgrades.count { it == "armour_plating" || it == "armor_plating" }
    var hull = chassis.maxHull + (armorCount * 2)
    
    // Warden prison car reduces hull by 2
    val prisonCarCount = parsedUpgrades.count { it == "prison_car" }
    hull -= (prisonCarCount * 2)
    
    // Verney microplate armour increases hull by 2
    val microplateCount = parsedUpgrades.count { it == "microplate_armour" }
    hull += (microplateCount * 2)
    
    return hull
}

fun calculateVehicleCans(chassisId: String, sponsorId: String, weaponsList: List<String>, upgradesList: List<String>): Int {
    val chassisCost = GaslandsStaticData.chassisList.find { it.id == chassisId }?.cost ?: 0
    
    val weaponsCost = weaponsList.sumOf { item ->
        val wId = item.split(":").first()
        val baseCost = GaslandsStaticData.weaponList.find { it.id == wId }?.cost ?: 0
        val mount = item.split(":").getOrNull(1)
        if (mount?.equals("turret", ignoreCase = true) == true) baseCost * 3 else baseCost
    }
    
    val upgradesCost = upgradesList.sumOf { item ->
        val uId = item.split(":").first()
        val baseCost = GaslandsStaticData.upgradeList.find { it.id == uId }?.cost 
            ?: GaslandsStaticData.perkList.find { it.id == uId }?.cost ?: 0
        
        // Sponsor rule applications:
        when {
            // Idris N2O Addict: Nitro upgrade costs half of the normal cost
            sponsorId == "idris" && (uId == "nitro_booster" || uId == "nitro") -> {
                baseCost / 2 // 6 / 2 = 3 Cans
            }
            // Scarlett Crew Quarters: Extra Crewmember cost is halved
            sponsorId == "scarlett" && uId == "extra_crewmember" -> {
                baseCost / 2 // 4 / 2 = 2 Cans
            }
            else -> baseCost
        }
    }
    
    val baseTotal = chassisCost + weaponsCost + upgradesCost
    
    // Warden Prison Car reduces cost by 4 (this is already factored into baseTotal since prison_car cost is -4).
    // We enforce the minimum cost limit of 5 Cans when a prison car is equipped.
    val prisonCarCount = upgradesList.map { it.split(":").first() }.count { it == "prison_car" }
    if (prisonCarCount > 0) {
        return baseTotal.coerceAtLeast(5)
    }
    
    return baseTotal
}

fun calculateSlotsUsed(sponsorId: String, weaponsList: List<String>, upgradesList: List<String>): Int {
    val weaponsSlots = weaponsList.sumOf { item ->
        val wId = item.split(":").first()
        GaslandsStaticData.weaponList.find { it.id == wId }?.slots ?: 0
    }
    val upgradesSlots = upgradesList.sumOf { item ->
        val uId = item.split(":").first()
        val baseSlots = GaslandsStaticData.upgradeList.find { it.id == uId }?.slots ?: 0
        
        // Slime Spiked Fist: Ram upgrade requires 0 slots
        if (sponsorId == "slime" && uId == "ram") {
            0
        } else {
            baseSlots
        }
    }
    return weaponsSlots + upgradesSlots
}

fun calculateSlotsTotal(chassisId: String, upgradesList: List<String>): Int {
    val chassis = GaslandsStaticData.chassisList.find { it.id == chassisId } ?: return 0
    var total = chassis.buildSlots
    val upgIds = upgradesList.map { it.split(":").first().trim() }
    if (upgIds.contains("trailer(middleweight)")) total += 1
    if (upgIds.contains("trailer(heavyweight)")) total += 3
    return total
}

fun calculateHandling(chassisId: String, upgradesList: List<String>): Int {
    val chassis = GaslandsStaticData.chassisList.find { it.id == chassisId } ?: return 0
    var handling = chassis.handling
    val parsedUpgrades = upgradesList.map { it.split(":").first().trim() }
    val tankTracksCount = parsedUpgrades.count { it == "tank_tracks" }
    handling += tankTracksCount
    return handling
}

fun calculateMaxGear(chassisId: String, upgradesList: List<String>): Int {
    val chassis = GaslandsStaticData.chassisList.find { it.id == chassisId } ?: return 0
    var maxGear = chassis.maxGear
    val parsedUpgrades = upgradesList.map { it.split(":").first().trim() }
    val tankTracksCount = parsedUpgrades.count { it == "tank_tracks" }
    maxGear -= tankTracksCount
    return maxGear.coerceAtLeast(1)
}

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sponsorId: String,
    val chassisId: String,
    val weaponsCsv: String = "",
    val upgradesCsv: String = "",
    val notes: String = "",
    
    // In-game state
    val currentGear: Int = 1,
    val currentHazards: Int = 0,
    val currentHull: Int = -1, // -1 represents initialized state, where it matches chassis max hull
    val ammoMapCsv: String = "", // "rockets:3,nitro:1"
    val isOnFire: Boolean = false,
    val spectatorVotes: Int = 0,
    val currentTurnGear: Int = 1,
    val activatedInCurrentGear: Boolean = false,
    val currentCrew: Int = -1 // -1 represents initialized state, where it matches maximum crew
) {
    // Helper to calculate total cost in Cans
    fun calculateTotalCans(): Int {
        val wList = if (weaponsCsv.isEmpty()) emptyList() else weaponsCsv.split(",")
        val uList = if (upgradesCsv.isEmpty()) emptyList() else upgradesCsv.split(",")
        return calculateVehicleCans(chassisId, sponsorId, wList, uList)
    }

    // Helper to calculate maximum hull including Armour Plating (+2 HP per instance)
    fun getMaxHull(): Int {
        val list = if (upgradesCsv.isEmpty()) emptyList() else upgradesCsv.split(",")
        return calculateMaxHull(chassisId, list)
    }

    // Helper to calculate total crew including Extra Crewmembers (+1 crew per instance)
    fun getCrew(): Int {
        val chassis = GaslandsStaticData.chassisList.find { it.id == chassisId } ?: return 0
        val upgradesList = if (upgradesCsv.isEmpty()) emptyList() else upgradesCsv.split(",")
        val extraCrewCount = upgradesList.map { it.split(":").first() }.count { it == "extra_crewmember" }
        return chassis.crew + extraCrewCount
    }

    fun getSlotsUsed(): Int {
        val wList = if (weaponsCsv.isEmpty()) emptyList() else weaponsCsv.split(",")
        val uList = if (upgradesCsv.isEmpty()) emptyList() else upgradesCsv.split(",")
        return calculateSlotsUsed(sponsorId, wList, uList)
    }

    fun getSlotsTotal(): Int {
        val uList = if (upgradesCsv.isEmpty()) emptyList() else upgradesCsv.split(",")
        return calculateSlotsTotal(chassisId, uList)
    }

    fun getHandling(): Int {
        val uList = if (upgradesCsv.isEmpty()) emptyList() else upgradesCsv.split(",")
        return calculateHandling(chassisId, uList)
    }

    fun getMaxGear(): Int {
        val uList = if (upgradesCsv.isEmpty()) emptyList() else upgradesCsv.split(",")
        return calculateMaxGear(chassisId, uList)
    }
}
