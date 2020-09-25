package com.example.gaslandsbuilder.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class Weapon(
    val name: String,
    val cost: Int,
    val buildSlots: Int
){
    fun addWeapon(): Int{
        return cost
    }

    fun removeWeapon(): Int{
        return cost*(-1)
    }
}

data class ChosenWeapon(
    val name: String,
    val cost: Int,
    val buildSlots: Int,
    val mount: String
)


fun getAllWeaponNames(context: Context): MutableList<Weapon>{
    val db: SQLiteDatabase = DbHelper(
        context
    ).readableDatabase
    val weaponsMutableList = mutableListOf<Weapon>()
    val cursor: Cursor = db.rawQuery("SELECT name, cost, buildSlots FROM weapons", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val cost = cursor.getString(cursor.getColumnIndex("cost")).toInt()
            val buildSlots = cursor.getString(cursor.getColumnIndex("buildSlots")).toInt()
            weaponsMutableList.add(Weapon(name, cost, buildSlots))
            cursor.moveToNext()
        }
    }
    cursor.close()
    return weaponsMutableList
}