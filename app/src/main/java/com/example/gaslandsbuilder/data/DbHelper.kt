package com.example.gaslandsbuilder.data

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class DbHelper(val context: Context, val dbName: String, val dbVersion: Int) : SQLiteOpenHelper(context,
    dbName, null, dbVersion
) {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        "databases.$dbName",
        Context.MODE_PRIVATE
    )

    init {
        installOrUpdateIfNecessary()
    }

    private fun installedDatabaseIsOutdated(): Boolean {
        return preferences.getInt(dbName, 0) < dbVersion
    }

    private fun writedbVersionInPreferences() {
        preferences.edit().apply {
            putInt(
                dbName,
                dbVersion
            )
            apply()
        }
    }

    @Synchronized
    private fun installOrUpdateIfNecessary() {
        if (installedDatabaseIsOutdated()) {
            context.deleteDatabase(dbName)
            installDatabaseFromAssets()
            writedbVersionInPreferences()
        }
    }

    private fun installDatabaseFromAssets() {
        val inputStream = context.assets.open("$ASSETS_PATH/$dbName.sqlite3")

        try {
            val outputDir = context.getDatabasePath(dbName).parentFile
            if(!outputDir.exists()){
                outputDir.mkdir()
            }
            val outputFile = File(context.getDatabasePath(dbName).path)
            if (!outputFile.exists()){
                outputFile.createNewFile()
            }
            val outputStream = FileOutputStream(outputFile)

            inputStream.copyTo(outputStream)
            inputStream.close()

            outputStream.flush()
            outputStream.close()
        } catch (exception: Throwable) {
            throw RuntimeException("The $dbName database couldn't be installed.", exception)
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Nothing to do
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Nothing to do
    }

    companion object {
        const val ASSETS_PATH = "databases"
    }
}