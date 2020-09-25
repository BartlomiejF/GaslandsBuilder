package com.example.gaslandsbuilder.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

data class Vehicle(
    val name: String,
    val cost: Int,
    val buildSlots: Int

)

fun getAllVehicles(context: Context): MutableList<Vehicle>{
    val db: SQLiteDatabase = DbHelper(
        context
    ).readableDatabase
    val vehiclesMutableList = mutableListOf<Vehicle>()
//    val cursor: Cursor = db.rawQuery("SELECT name, cost FROM vehicles", null)
    val cursor: Cursor = db.rawQuery("SELECT name, cost, buildSlots FROM vehicles", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val cost = cursor.getString(cursor.getColumnIndex("cost")).toInt()
            val buildSlots = cursor.getString(cursor.getColumnIndex("buildSlots")).toInt()
//            vehiclesMutableList.add(Weapon(name, cost))
            vehiclesMutableList.add(Vehicle(name, cost, buildSlots))
            cursor.moveToNext()
        }
    }
    cursor.close()
    return vehiclesMutableList
}