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
    val weight: String = "L",
    val perks: String? = null,
    var currentGear: Int = 1,
    val sponsor: String? = "Custom",
    var hazard: Int = 0,
    var chosenToTracker: Int = 0,
    var onFire: Boolean = false
){
    fun getWeaponsList(): MutableList<Weapon>{
        val weaponsList = mutableListOf<Weapon>()
        weapons.split(";").dropLast(1).forEach {
            val params = it.split(":")
            weaponsList.add(Weapon(
                name = params[0],
                cost = params[1].toInt(),
                buildSlots = params[2].toInt(),
                specialRules = params[3],
                ammo = params[4].toInt(),
                crewFired = params[5].toInt(),
                damage = params[6],
                range = params[7],
                mount = params[8]
            ))
        }
        return weaponsList
    }

    fun getUpgradesList(): MutableList<Upgrade>{
        val upgradesList = mutableListOf<Upgrade>()
        upgrades.split(";").dropLast(1).forEach{
            val params = it.split(":")
            upgradesList.add(Upgrade(
                name = params[0],
                cost = params[1].toInt(),
                buildSlots = params[2].toInt(),
                ammo = params[3].toInt(),
                specRules = params[4]
            ))
        }
        return upgradesList
    }

    fun getPerksList(): MutableList<Perk>{
        val perksList = mutableListOf<Perk>()
        perks?.split(";")?.dropLast(1)?.forEach{
            val params = it.split(":")
            perksList.add(Perk(
                name = params[0],
                perkClass = params[1],
                cost = params[2].toInt()
            ))
        }
        return perksList
    }

    fun getExportCarText(): String{
        val weaponsList: MutableList<String> = mutableListOf<String>()
        weapons.split(";").dropLast(1).forEach {
            weaponsList.add(it.split(":")[0])
        }
        val upgradesList: MutableList<String> = mutableListOf<String>()
        upgrades.split(";").dropLast(1).forEach {
            upgradesList.add(it.split(":")[0])
        }
        val perksList: MutableList<String> = mutableListOf<String>()
        perks?.split(";")?.dropLast(1)?.forEach {
            perksList.add(it.split(":")[0])
        }
        val text = """
            |$name 
            |Sponsor: ${"\t"}$sponsor
            |Car type:${"\t"}$type 
            |Cost: ${"\t"}${"\t"}$cost Cans 
            |${"\n"}
            |Weapons, upgrades and perks:
            ${weaponsList.joinToString("\n|w: \t", prefix = "|w: \t")}
            ${upgradesList.joinToString("\n|u: \t", prefix = "|u: \t")}
            ${perksList.joinToString("\n|p: \t", prefix = "|p: \t")}
        """.trimIndent().trimMargin()
        return text
    }
}

//fun savedCarFromString(savedCarString: String): SavedCar{
//
//}

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
                val perks = cursor.getString(cursor.getColumnIndex("perks"))
                val sponsor = cursor.getString(cursor.getColumnIndex("sponsor"))
                savedCarsMutableList.add(SavedCar(name, cost, type, weapons, id, upgrades, perks=perks, sponsor=sponsor))
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
        put("perks", car.perks)
        put("sponsor", car.sponsor)
    }
    db.insert("savedCars", null, values)
}

fun updateCar(car: SavedCar, db: SQLiteDatabase, id: Int){
    val values = ContentValues().apply{
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
        put("perks", car.perks)
        put("sponsor", car.sponsor)
    }
    db.update("savedCars", values, "id=?", arrayOf(id.toString()))
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
        val perks = cursor.getString(cursor.getColumnIndex("perks"))
        var sponsor = cursor.getString(cursor.getColumnIndex("sponsor"))
        if (sponsor==null){ sponsor="Custom" }
        readCar = SavedCar(name, cost, type, weapons, id, upgrades, hull, handling, maxGear, crew, specialRules, weight, perks, sponsor = sponsor)
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
    var readCars = mutableListOf<SavedCar>()
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
            val perks = cursor.getString(cursor.getColumnIndex("perks"))
            val sponsor = cursor.getString(cursor.getColumnIndex("sponsor"))
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
            weight,
            perks,
                sponsor = sponsor
            ))
            cursor.moveToNext()
        }
    }
    cursor.close()
    db.close()
    return readCars
}