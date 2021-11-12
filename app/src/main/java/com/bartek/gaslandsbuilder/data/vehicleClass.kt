package com.bartek.gaslandsbuilder.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Parcelable
import com.bartek.gaslandsbuilder.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Vehicle(
    val name: String,
    val cost: Int,
    val buildSlots: Int,
    var hull: Int = 0,
    var handling: Int = 0,
    var maxGear: Int = 0,
    var crew: Int = 0,
    val specialRules: String? = null,
    val weight: String = "L"
): Parcelable

fun getAllVehicles(context: Context): MutableList<Vehicle>{
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
    ).readableDatabase
    val vehiclesMutableList = mutableListOf<Vehicle>()
    val cursor: Cursor = db.rawQuery("SELECT * FROM vehicles", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val cost = cursor.getString(cursor.getColumnIndex("cost")).toInt()
            val buildSlots = cursor.getString(cursor.getColumnIndex("buildSlots")).toInt()
            val hull = cursor.getInt(cursor.getColumnIndex("hull"))
            val handling = cursor.getInt(cursor.getColumnIndex("handling"))
            val maxGear = cursor.getInt(cursor.getColumnIndex("maxGear"))
            val crew = cursor.getInt(cursor.getColumnIndex("crew"))
            val specialRules = cursor.getString(cursor.getColumnIndex("specialRules"))
            val weight = cursor.getString(cursor.getColumnIndex("weight"))
            vehiclesMutableList.add(Vehicle(name, cost, buildSlots, hull, handling, maxGear,crew, specialRules, weight))
            cursor.moveToNext()
        }
    }
    cursor.close()
    db.close()
    return vehiclesMutableList
}

fun getVehicleOnName(context: Context, name: String): Vehicle {
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
    ).readableDatabase
    lateinit var readVehicle: Vehicle
    val cursor: Cursor = db.rawQuery("SELECT * FROM vehicles where name=?", arrayOf(name))
    if (cursor.moveToFirst()) {
        val name = cursor.getString(cursor.getColumnIndex("name"))
        val cost = cursor.getString(cursor.getColumnIndex("cost")).toInt()
        val buildSlots = cursor.getString(cursor.getColumnIndex("buildSlots")).toInt()
        val hull = cursor.getInt(cursor.getColumnIndex("hull"))
        val handling = cursor.getInt(cursor.getColumnIndex("handling"))
        val maxGear = cursor.getInt(cursor.getColumnIndex("maxGear"))
        val crew = cursor.getInt(cursor.getColumnIndex("crew"))
        val specialRules = cursor.getString(cursor.getColumnIndex("specialRules"))
        val weight = cursor.getString(cursor.getColumnIndex("weight"))
        readVehicle = Vehicle(name, cost, buildSlots, hull, handling, maxGear, crew, specialRules, weight)
    }
    cursor.close()
    db.close()
    return readVehicle
}

fun applyVehicleSpecialRules(weapon: Weapon, chosenVehicle: ChosenVehicle, context: Context){
    when (chosenVehicle.type!!.name){
        "Gyrocopter", "Helicopter" -> {
            if (weapon.range=="dropped") weapon.buildSlots = 0
        }
        else -> {
            if (weapon.range=="dropped") {
                weapon.buildSlots = getWeaponCostOnName(context, weapon.name)
            }
        }
    }
}
