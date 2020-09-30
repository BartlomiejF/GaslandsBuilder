package com.example.gaslandsbuilder.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "SavedCars")
data class SavedCar(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val cost: Int
)

@Dao
interface SavedCarsDBDao{
    @Insert
    fun saveCar(car: SavedCar)

    @Query("SELECT name, cost FROM SavedCars")
    fun getAllSavedCars(): LiveData<List<SavedCar>>
}