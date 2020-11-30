package com.bartek.gaslandsbuilder.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.bartek.gaslandsbuilder.R

data class SavedCar(
    val name: String,
    val cost: Int,
    val type: String,
    val weapons: String,
    val id: Int? = null,
    val upgrades: String
)

fun getAllSavedCars(context: Context): MutableList<SavedCar> {
        val db: SQLiteDatabase = DbHelper(
            context,
            "savedCarsDB",
            context.resources.getInteger(R.integer.savedCarsDBVersion)
        ).readableDatabase
        val savedCarsMutableList = mutableListOf<SavedCar>()
        val cursor: Cursor = db.rawQuery("SELECT * FROM savedCars", null)
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val name = cursor.getString(cursor.getColumnIndex("name"))
                val cost = cursor.getString(cursor.getColumnIndex("cost")).toInt()
                val type = cursor.getString(cursor.getColumnIndex("type"))
                val weapons = cursor.getString(cursor.getColumnIndex("weapons"))
                val id = cursor.getString(cursor.getColumnIndex("id")).toInt()
                val upgrades = cursor.getString(cursor.getColumnIndex("upgrades"))
                savedCarsMutableList.add(SavedCar(name, cost, type, weapons, id, upgrades))
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
        put("upgrades", car.upgrades)
    }
    db.insert("savedCars", null, values)
}

fun deleteSavedCar(id: Int, db: SQLiteDatabase){
    db.delete("savedCars", "id=?", arrayOf(id.toString()))
}