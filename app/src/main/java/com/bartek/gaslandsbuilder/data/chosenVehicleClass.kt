package com.bartek.gaslandsbuilder.data

import java.nio.channels.NonReadableChannelException

data class ChosenVehicle(
    var type: Vehicle? = Vehicle("first", 0, 0),
    val chosenWeapons: MutableList<Weapon> = mutableListOf<Weapon>(),
    val chosenUpgrades: MutableList<Upgrade> = mutableListOf<Upgrade>(),
    val chosenPerks: MutableList<Perk> = mutableListOf<Perk>(),
    var cost: Int = 0,
    var sponsor: Sponsor? = Sponsor("first",null, null, null)
){
    fun calculateCost(): Int {
        var calculated = 0
        chosenWeapons.forEach{ calculated += it.cost }
        chosenUpgrades.forEach { calculated += it.cost }
        chosenPerks.forEach { calculated += it.cost }
        calculated += type?.cost ?: 0
        cost = calculated
        return calculated
    }

    fun calculateBuildSlots(): Int{
        var buildSlots = type?.buildSlots ?: 0
        chosenWeapons.forEach{ buildSlots -= it.buildSlots }
        chosenUpgrades.forEach { buildSlots -= it.buildSlots }

        return buildSlots
    }
}