package com.bartek.gaslandsbuilder.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Parcelable
import com.bartek.gaslandsbuilder.R
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class Upgrade(
    val name: String,
    val cost: Int,
    val buildSlots: Int,
    val ammo: Int,
    val specRules: String,
    var onAdd: @RawValue ((Vehicle) -> Unit)? = null
): Parcelable{
    fun to_str(): String {
        return "$name:$cost:$buildSlots:$ammo:$specRules;"
    }
}


fun getAllUpgradesNames(context: Context): MutableList<Upgrade>{
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
    ).readableDatabase
    val upgradesMutableList = mutableListOf<Upgrade>()
    val cursor: Cursor = db.rawQuery("SELECT * FROM upgrades", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val cost = cursor.getString(cursor.getColumnIndex("cost")).toInt()
            val buildSlots = cursor.getString(cursor.getColumnIndex("buildSlots")).toInt()
            val ammo = cursor.getInt(cursor.getColumnIndex("ammo"))
            val specRules = cursor.getString(cursor.getColumnIndex("specRules"))
            upgradesMutableList.add(Upgrade(name, cost, buildSlots, ammo, specRules))
            cursor.moveToNext()
        }
    }
    cursor.close()
    return upgradesMutableList
}