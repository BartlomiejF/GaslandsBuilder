package com.bartek.gaslandsbuilder.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.bartek.gaslandsbuilder.R

data class Sponsor(
    val name: String,
    val perkClassI: String?,
    val perkClassII: String?,
    val sponsorPerks: MutableList<Perk>?
)

fun getAllSponsors(context: Context): MutableList<Sponsor>{
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
    ).readableDatabase
    val sponsorsMutableList = mutableListOf<Sponsor>()
    val cursor: Cursor = db.rawQuery("SELECT * FROM sponsors", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val perkClassI = cursor.getString(cursor.getColumnIndex("perkClassI"))
            val perkClassII = cursor.getString(cursor.getColumnIndex("perkClassII"))
            val sponsorPerksStr: String? = cursor.getString(cursor.getColumnIndex("sponsoredPerks"))
            val sponsorPerks = mutableListOf<Perk>()
            if (sponsorPerksStr != null) {
                sponsorPerksStr.split(".").dropLast(1).forEach {
                    if (it != "null") {
                        sponsorPerks.add(Perk(it.trim(), "Sponsored Perk", 0))
                    }
                }
            }
            sponsorsMutableList.add(Sponsor(name, perkClassI, perkClassII, sponsorPerks))
            cursor.moveToNext()
        }
    }
    cursor.close()
    db.close()
    return sponsorsMutableList
}

fun getSponsorOnName(context: Context, name: String): Sponsor {
    val db: SQLiteDatabase = DbHelper(
        context,
        "gaslandsWeapons",
        context.resources.getInteger(R.integer.dbVersion)
    ).readableDatabase
    lateinit var sponsor: Sponsor
    val cursor: Cursor = db.rawQuery("SELECT * FROM sponsors where name=?", arrayOf(name))
    if (cursor.moveToFirst()) {
        val name = cursor.getString(cursor.getColumnIndex("name"))
        val perkClassI = cursor.getString(cursor.getColumnIndex("perkClassI"))
        val perkClassII = cursor.getString(cursor.getColumnIndex("perkClassII"))
        val sponsorPerksStr: String? = cursor.getString(cursor.getColumnIndex("sponsoredPerks"))
        val sponsorPerks = mutableListOf<Perk>()
        if (sponsorPerksStr != null) {
            sponsorPerksStr.split(".").dropLast(1).forEach {
                if (it != "null") {
                    sponsorPerks.add(Perk(it.trim(), "Sponsored Perk", 0))
                }
            }
        }
        sponsor = Sponsor(name, perkClassI, perkClassII, sponsorPerks)
    }
    cursor.close()
    db.close()
    return sponsor
}