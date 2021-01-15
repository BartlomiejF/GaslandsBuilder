package com.bartek.gaslandsbuilder.data

data class ChosenVehicle(
    var type: Vehicle? = null,
    val chosenWeapons: MutableList<Weapon> = mutableListOf<Weapon>(),
    val chosenUpgrades: MutableList<Upgrade> = mutableListOf<Upgrade>(),
    val chosenPerks: MutableList<Perk> = mutableListOf<Perk>(),
    var cost: Int = 0
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