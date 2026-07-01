package com.example.data.repository

import com.example.data.local.VehicleDao
import com.example.data.models.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {
    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    fun getVehicleById(id: Int): Flow<Vehicle?> = vehicleDao.getVehicleById(id)

    suspend fun getVehicleDirect(id: Int): Vehicle? = vehicleDao.getVehicleByIdDirect(id)

    suspend fun insert(vehicle: Vehicle): Long = vehicleDao.insertVehicle(vehicle)

    suspend fun update(vehicle: Vehicle) = vehicleDao.updateVehicle(vehicle)

    suspend fun delete(vehicle: Vehicle) = vehicleDao.deleteVehicle(vehicle)

    suspend fun deleteById(id: Int) = vehicleDao.deleteVehicleById(id)
}
