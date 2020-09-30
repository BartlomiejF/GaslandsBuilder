package com.example.gaslandsbuilder.data

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class DbHelper(val context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        "${context.packageName}.database_versions",
        Context.MODE_PRIVATE
    )

    init {
        installOrUpdateIfNecessary()
    }

    private fun installedDatabaseIsOutdated(): Boolean {
        return preferences.getInt(DATABASE_NAME, 0) < DATABASE_VERSION
    }

    private fun writeDatabaseVersionInPreferences() {
        preferences.edit().apply {
            putInt(
                DATABASE_NAME,
                DATABASE_VERSION
            )
            apply()
        }
    }

    @Synchronized
    private fun installOrUpdateIfNecessary() {
        if (installedDatabaseIsOutdated()) {
            context.deleteDatabase(DATABASE_NAME)
            installDatabaseFromAssets()
            writeDatabaseVersionInPreferences()
        }
    }

    private fun installDatabaseFromAssets() {
        val inputStream = context.assets.open("$ASSETS_PATH/$DATABASE_NAME.sqlite3")

        try {
            val outputFile = File(context.getDatabasePath(DATABASE_NAME).path)
            val outputStream = FileOutputStream(outputFile)

            inputStream.copyTo(outputStream)
            inputStream.close()

            outputStream.flush()
            outputStream.close()
        } catch (exception: Throwable) {
            throw RuntimeException("The $DATABASE_NAME database couldn't be installed.", exception)
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Nothing to do
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Nothing to do
    }

    companion object {
        const val DATABASE_NAME = "gaslandsWeapons"
        const val ASSETS_PATH = "databases"
        const val DATABASE_VERSION = 4
    }
}