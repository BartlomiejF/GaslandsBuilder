package com.example.data.models

data class GaslandsChassis(
    val id: String,
    val name: String,
    val cost: Int,
    val weight: String,
    val maxHull: Int,
    val handling: Int,
    val maxGear: Int,
    val crew: Int,
    val buildSlots: Int,
    val specialRules: String = ""
)

data class GaslandsSponsor(
    val id: String,
    val name: String,
    val perkClasses: List<String>,
    val description: String,
    val styleColorHex: String // visual identity accent
)

data class GaslandsWeapon(
    val id: String,
    val name: String,
    val cost: Int,
    val slots: Int,
    val range: String,
    val damage: String,
    val maxAmmo: Int = 0, // 0 means infinite
    val specialRules: String = ""
)

data class GaslandsUpgrade(
    val id: String,
    val name: String,
    val cost: Int,
    val slots: Int,
    val description: String
)

data class GaslandsPerk(
    val id: String,
    val name: String,
    val perkClass: String,
    val cost: Int
)

