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
    val buildSlots: Int
): Parcelable

fun getAllVehicles(context: Context): MutableList<Vehicle>{
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
    ).readableDatabase
    val vehiclesMutableList = mutableListOf<Vehicle>()
    val cursor: Cursor = db.rawQuery("SELECT name, cost, buildSlots FROM vehicles", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val cost = cursor.getString(cursor.getColumnIndex("cost")).toInt()
            val buildSlots = cursor.getString(cursor.getColumnIndex("buildSlots")).toInt()
            vehiclesMutableList.add(Vehicle(name, cost, buildSlots))
            cursor.moveToNext()
        }
    }
    cursor.close()
    return vehiclesMutableList
}

