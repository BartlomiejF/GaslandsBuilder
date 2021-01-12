package com.bartek.gaslandsbuilder.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Parcelable
import com.bartek.gaslandsbuilder.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Perk(
    val name: String,
    val perkClass: String,
    val cost: Int
): Parcelable{
    fun to_str(): String{
        return "$name:$perkClass:$cost;"
    }
}

val PERK_CLASSES = listOf("Aggression", "Badass", "Built", "Daring", "Horror", "Military", "Reckless",
                      "Speed", "Technology", "Tuning", "Precision", "Pursuit")

fun getAllPerks(context: Context): MutableList<Perk>{
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
    ).readableDatabase
    val perksMutableList = mutableListOf<Perk>()
    val cursor: Cursor = db.rawQuery("SELECT * FROM perks", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val cost = cursor.getInt(cursor.getColumnIndex("cost"))
            val perkClass = cursor.getString(cursor.getColumnIndex("class"))
            perksMutableList.add(Perk(name, perkClass, cost))
            cursor.moveToNext()
        }
    }
    cursor.close()
    return perksMutableList
}

fun applyUpgradeSpecialRules(perk: Perk, vehicle: ChosenVehicle) {

}