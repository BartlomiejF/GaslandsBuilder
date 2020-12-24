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
    val upgrades: String,
    val hull: Int = 0,
    val handling: Int = 0,
    val maxGear: Int = 0,
    val crew: Int = 0,
    val specialRules: String? = null,
    val weight: String? = null,
    var currentGear: Int = 1
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
        db.close()
        return savedCarsMutableList
}

fun saveCar(car: SavedCar, db: SQLiteDatabase){
    val values = ContentValues().apply{
        put("name", car.name)
        put("cost", car.cost)
        put("type", car.type)
        put("weapons", car.weapons)
        put("upgrades", car.upgrades)
        put("handling", car.handling)
        put("hull", car.hull)
        put("crew", car.crew)
        put("maxGear", car.maxGear)
        put("specialRules", car.specialRules)
        put("weight", car.weight)
    }
    db.insert("savedCars", null, values)
}

fun deleteSavedCar(id: Int, context: Context){
    val db =DbHelper(
        context,
        "savedCarsDB",
        context.resources.getInteger(R.integer.savedCarsDBVersion)
    ).writableDatabase
    db.delete("savedCars", "id=?", arrayOf(id.toString()))
    db.close()
}

fun getSingleCar(context: Context, id: Int?): SavedCar {
    val db: SQLiteDatabase = DbHelper(
        context,
        "savedCarsDB",
        context.resources.getInteger(R.integer.savedCarsDBVersion)
    ).readableDatabase
    lateinit var readCar: SavedCar
    val cursor: Cursor = db.rawQuery("SELECT * FROM savedCars WHERE id=?", arrayOf(id.toString()))
    if (cursor.moveToFirst()) {
        val name = cursor.getString(cursor.getColumnIndex("name"))
        val cost = cursor.getInt(cursor.getColumnIndex("cost"))
        val type = cursor.getString(cursor.getColumnIndex("type"))
        val weapons = cursor.getString(cursor.getColumnIndex("weapons"))
        val id = cursor.getInt(cursor.getColumnIndex("id"))
        val upgrades = cursor.getString(cursor.getColumnIndex("upgrades"))
        val hull = cursor.getInt(cursor.getColumnIndex("hull"))
        val handling = cursor.getInt(cursor.getColumnIndex("handling"))
        val maxGear = cursor.getInt(cursor.getColumnIndex("maxGear"))
        val crew = cursor.getInt(cursor.getColumnIndex("crew"))
        val specialRules = cursor.getString(cursor.getColumnIndex("specialRules"))
        val weight = cursor.getString(cursor.getColumnIndex("weight"))
        readCar = SavedCar(name, cost, type, weapons, id, upgrades, hull, handling, maxGear, crew, specialRules, weight)
    }
    cursor.close()
    db.close()
    return readCar
}

fun getMultipleCarsOnId(context: Context, ids: String): MutableList<SavedCar> {
    val db: SQLiteDatabase = DbHelper(
        context,
        "savedCarsDB",
        context.resources.getInteger(R.integer.savedCarsDBVersion)
    ).readableDatabase
    val readCars = mutableListOf<SavedCar>()
    val cursor: Cursor = db.rawQuery("SELECT * FROM savedCars WHERE id IN ( $ids )", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val cost = cursor.getInt(cursor.getColumnIndex("cost"))
            val type = cursor.getString(cursor.getColumnIndex("type"))
            val weapons = cursor.getString(cursor.getColumnIndex("weapons"))
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val upgrades = cursor.getString(cursor.getColumnIndex("upgrades"))
            val hull = cursor.getInt(cursor.getColumnIndex("hull"))
            val handling = cursor.getInt(cursor.getColumnIndex("handling"))
            val maxGear = cursor.getInt(cursor.getColumnIndex("maxGear"))
            val crew = cursor.getInt(cursor.getColumnIndex("crew"))
            val specialRules = cursor.getString(cursor.getColumnIndex("specialRules"))
            val weight = cursor.getString(cursor.getColumnIndex("weight"))
            readCars.add(SavedCar(name,
                cost,
                type,
                weapons,
                id,
                upgrades,
                hull,
                handling,
                maxGear,
                crew,
                specialRules,
                weight))
            cursor.moveToNext()
        }
    }
    cursor.close()
    db.close()
    return readCars
}