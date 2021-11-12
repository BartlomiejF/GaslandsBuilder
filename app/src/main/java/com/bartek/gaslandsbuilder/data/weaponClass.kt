package com.bartek.gaslandsbuilder.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Parcelable
import com.bartek.gaslandsbuilder.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Weapon(
    val name: String,
    val cost: Int,
    var buildSlots: Int,
    val specialRules: String? = "No special rules.",
    var ammo: Int = 0,
    val crewFired: Int = 0,
    val damage: String? = "1d6",
    val range: String? = "long",
    val mount: String? = "front"
): Parcelable {
    fun to_str(): String {
        return "$name:$cost:$buildSlots:$specialRules:$ammo:$crewFired:$damage:$range:$mount;"
    }
}

//@Parcelize
//data class ChosenWeapon(
//    val name: String,
//    val cost: Int,
//    val buildSlots: Int,
//    val mount: String
//): Parcelable


fun getAllWeaponNames(context: Context): MutableList<Weapon>{
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
    ).readableDatabase
    val weaponsMutableList = mutableListOf<Weapon>()
    val cursor: Cursor = db.rawQuery("SELECT * FROM weapons", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val cost = cursor.getString(cursor.getColumnIndex("cost")).toInt()
            val buildSlots = cursor.getString(cursor.getColumnIndex("buildSlots")).toInt()
            val ammo = cursor.getInt(cursor.getColumnIndex("ammo"))
            val crewFired = cursor.getInt(cursor.getColumnIndex("crewFired"))
            val damage = cursor.getString(cursor.getColumnIndex("damage"))
            val specialRules = cursor.getString(cursor.getColumnIndex("specialRules"))
            val range = cursor.getString(cursor.getColumnIndex("range"))
            weaponsMutableList.add(Weapon(name, cost, buildSlots, specialRules, ammo, crewFired, damage, range))
            cursor.moveToNext()
        }
    }
    cursor.close()
    return weaponsMutableList
}

fun getWeaponCostOnName(context: Context, name: String): Int {
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
    ).readableDatabase
    var buildSlots = 0
    val cursor: Cursor = db.rawQuery("SELECT buildSlots FROM weapons where name=?", arrayOf(name))
    if (cursor.moveToFirst()) {
        buildSlots = cursor.getString(cursor.getColumnIndex("buildSlots")).toInt()
    }
    cursor.close()
    db.close()
    return buildSlots
}