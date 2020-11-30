package com.bartek.gaslandsbuilder.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Parcelable
import com.bartek.gaslandsbuilder.R
import kotlinx.android.parcel.Parcelize

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

@Parcelize
data class ChosenWeapon(
    val name: String,
    val cost: Int,
    val buildSlots: Int,
    val mount: String
): Parcelable


fun getAllWeaponNames(context: Context): MutableList<Weapon>{
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
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