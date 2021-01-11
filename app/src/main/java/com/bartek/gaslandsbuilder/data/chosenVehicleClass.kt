package com.bartek.gaslandsbuilder.data

data class ChosenVehicle(
    var type: Vehicle? = null,
    val chosenWeapons: MutableList<Weapon> = mutableListOf<Weapon>(),
    val chosenUpgrades: MutableList<Upgrade> = mutableListOf<Upgrade>(),
    val chosenPerks: MutableList<Perk> = mutableListOf<Perk>(),
    var cost: Int = 0
){
    fun calculateCost(): Int {
        chosenWeapons.forEach{ cost += it.cost }
        chosenUpgrades.forEach { cost += it.cost }
        chosenPerks.forEach { cost += it.cost }
        cost += type?.cost ?: 0

        return cost
    }

    fun calculateBuildSlots(): Int{
        var buildSlots = type?.buildSlots ?: 0
        chosenWeapons.forEach{ buildSlots += it.buildSlots }
        chosenUpgrades.forEach { buildSlots += it.buildSlots }

        return buildSlots
    }
}