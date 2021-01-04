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
    return vehiclesMutableList
}

