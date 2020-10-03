package com.example.gaslandsbuilder.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

val SAVED_CARS_DB_VERSION = 3

data class SavedCar(
    val name: String,
    val cost: Int,
    val type: String,
    val weapons: String
)


fun getAllSavedCars(context: Context): MutableList<SavedCar> {
        val db: SQLiteDatabase = DbHelper(
            context,
            "savedCarsDB",
            SAVED_CARS_DB_VERSION
        ).readableDatabase
        val savedCarsMutableList = mutableListOf<SavedCar>()
        val cursor: Cursor = db.rawQuery("SELECT name, cost, type, weapons FROM savedCars", null)
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val name = cursor.getString(cursor.getColumnIndex("name"))
                val cost = cursor.getString(cursor.getColumnIndex("cost")).toInt()
                val type = cursor.getString(cursor.getColumnIndex("type"))
                val weapons = cursor.getString(cursor.getColumnIndex("weapons"))
                savedCarsMutableList.add(SavedCar(name, cost, type, weapons))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return savedCarsMutableList
}

fun saveCar(car: SavedCar, db: SQLiteDatabase){
    val values = ContentValues().apply{
        put("name", car.name)
        put("cost", car.cost)
        put("type", car.type)
        put("weapons", car.weapons)
    }
    db.insert("savedCars", null, values)
}