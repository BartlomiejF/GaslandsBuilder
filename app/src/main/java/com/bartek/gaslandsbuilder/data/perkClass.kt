package com.bartek.gaslandsbuilder.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Parcelable
import com.bartek.gaslandsbuilder.R
import kotlinx.android.parcel.Parcelize

val prisonCarPerk: Perk = Perk("Prison Car", "*Sponsored Perk*", -4)
val microPlateArmourPerk = Perk("MicroPlate Armour", "*Sponsored Perk*", 6)

@Parcelize
data class Perk(
    val name: String,
    val perkClass: String,
    var cost: Int
): Parcelable{
    fun to_str(): String{
        return "$name:$perkClass:$cost;"
    }
}

val PERK_CLASSES = listOf("Aggression", "Badass", "Built", "Daring", "Horror", "Military", "Reckless",
                      "Speed", "Technology", "Tuning", "Precision", "Pursuit")

fun getAllPerks(context: Context): MutableList<Perk>{
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
    ).readableDatabase
    val perksMutableList = mutableListOf<Perk>()
    val cursor: Cursor = db.rawQuery("SELECT * FROM perks", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val cost = cursor.getInt(cursor.getColumnIndex("cost"))
            val perkClass = cursor.getString(cursor.getColumnIndex("class"))
            perksMutableList.add(Perk(name, perkClass, cost))
            cursor.moveToNext()
        }
    }
    cursor.close()
    db.close()
    return perksMutableList
}

fun applyPerkSpecialRules(perk: Perk, vehicle: ChosenVehicle, onSave: Boolean = false, onRemove: Boolean = false ) {
    when (perk.name) {
        "Well Stocked" -> {
            if (!onSave) {
                vehicle.chosenWeapons.forEach {
                    if (onRemove) {
                        if (it.ammo == 4) {
                            it.ammo = 3
                        }
                    } else {
                        if (it.ammo == 3) {
                            it.ammo = 4
                        }
                    }
                }
            }
        }
        "N2O Addict" -> {
            if (!onSave) {
                vehicle.chosenUpgrades.forEach {
                    if (it.name == "Nitro Booster") {
                        if (onRemove) {
                            it.cost = 6
                        } else {
                            it.cost = 3
                        }
                    }
                }
            }
        }
        "Spiked Fist" -> {
            if (!onSave) {
                vehicle.chosenUpgrades.forEach {
                    if (it.name == "Ram") {
                        if (onRemove) {
                            it.buildSlots = 1
                        } else {
                            it.buildSlots = 0
                        }
                    }
                }
            }
        }
        "Crew Quarters" -> {
            if (!onSave) {
                vehicle.chosenUpgrades.forEach {
                    if (it.name == "Extra Crewmember") {
                        if (onRemove) {
                            it.cost = 4
                        } else {
                            it.cost = 2
                        }
                    }
                }
            }
        }
        "Expertise" -> {
            if (onSave) {
                vehicle.type!!.handling += 1
            }
        }
        "Prison Car" -> {
            val vehicleCost = vehicle.type!!.cost
            if (vehicleCost < 9){
                perk.cost = 5 - vehicleCost
            } else {
                perk.cost = -4
            }
            if (onSave){
                vehicle.type!!.hull -= 2
            }
        }
        "MicroPlate Armour" -> {
            if (onSave){
                vehicle.type!!.hull +=2
            }
        }
    }
}