package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.models.GaslandsStaticData
import com.example.data.models.Vehicle
import com.example.data.repository.VehicleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AppScreen {
    object ListScreen : AppScreen
    object BuildEditorScreen : AppScreen
    data class DashboardScreen(val vehicleIds: List<Int>) : AppScreen
}

class GaslandsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VehicleRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VehicleRepository(database.vehicleDao())
    }

    // List of saved vehicles
    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current screen
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.ListScreen)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // --- EDITOR STATE ---
    var editingVehicleId = MutableStateFlow<Int?>(null)
        private set
    val editName = MutableStateFlow("")
    val editSponsorId = MutableStateFlow("unaligned")
    val editChassisId = MutableStateFlow("car")
    val editWeapons = MutableStateFlow<List<String>>(emptyList())
    val editUpgrades = MutableStateFlow<List<String>>(emptyList())
    val editNotes = MutableStateFlow("")

    val selectedChassis = editChassisId.map { id ->
        GaslandsStaticData.chassisList.find { it.id == id } ?: GaslandsStaticData.chassisList[2]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GaslandsStaticData.chassisList[2])

    val selectedSponsor = editSponsorId.map { id ->
        GaslandsStaticData.sponsorList.find { it.id == id } ?: GaslandsStaticData.sponsorList[0]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GaslandsStaticData.sponsorList[0])

    val totalCans = combine(editChassisId, editSponsorId, editWeapons, editUpgrades) { chassisId, sponsorId, weps, upgs ->
        com.example.data.models.calculateVehicleCans(chassisId, sponsorId, weps, upgs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val slotsTotal = combine(selectedChassis, editUpgrades) { chassis, upgs ->
        com.example.data.models.calculateSlotsTotal(chassis.id, upgs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val slotsUsed = combine(editSponsorId, editWeapons, editUpgrades) { sponsorId, weps, upgs ->
        com.example.data.models.calculateSlotsUsed(sponsorId, weps, upgs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun startNewBuild() {
        editingVehicleId.value = null
        editName.value = ""
        editSponsorId.value = "unaligned"
        editChassisId.value = "car"
        editWeapons.value = emptyList()
        editUpgrades.value = emptyList()
        editNotes.value = ""
        _currentScreen.value = AppScreen.BuildEditorScreen
    }

    fun startEditBuild(vehicle: Vehicle) {
        editingVehicleId.value = vehicle.id
        editName.value = vehicle.name
        editSponsorId.value = vehicle.sponsorId
        editChassisId.value = vehicle.chassisId
        editWeapons.value = if (vehicle.weaponsCsv.isEmpty()) emptyList() else vehicle.weaponsCsv.split(",")
        editUpgrades.value = if (vehicle.upgradesCsv.isEmpty()) emptyList() else vehicle.upgradesCsv.split(",")
        editNotes.value = vehicle.notes
        _currentScreen.value = AppScreen.BuildEditorScreen
    }

    fun addWeaponInstance(weaponId: String) {
        val currentList = editWeapons.value.toMutableList()
        val weaponObj = GaslandsStaticData.weaponList.find { it.id == weaponId }
        val isCrewFired = weaponObj?.let { it.specialRules.contains("crew-fired", ignoreCase = true) || it.slots == 0 } ?: false
        val isDropped = weaponObj?.let { it.range.contains("dropped", ignoreCase = true) } ?: false
        when {
            isCrewFired -> currentList.add("$weaponId:none")
            isDropped -> currentList.add("$weaponId:Rear")
            else -> currentList.add("$weaponId:Front")
        }
        editWeapons.value = currentList
    }

    fun removeWeaponInstance(index: Int) {
        val currentList = editWeapons.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            editWeapons.value = currentList
        }
    }

    fun updateWeaponInstanceMount(index: Int, newMount: String) {
        val currentList = editWeapons.value.toMutableList()
        if (index in currentList.indices) {
            val weaponId = currentList[index].split(":").first()
            currentList[index] = "$weaponId:$newMount"
            editWeapons.value = currentList
        }
    }

    fun addUpgradeInstance(upgradeId: String) {
        val currentList = editUpgrades.value.toMutableList()
        if (upgradeId == "ram" || upgradeId == "exploding_ram") {
            currentList.add("$upgradeId:Front")
        } else {
            currentList.add(upgradeId)
        }
        editUpgrades.value = currentList
    }

    fun removeUpgradeInstanceAt(index: Int) {
        val currentList = editUpgrades.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            editUpgrades.value = currentList
        }
    }

    fun updateUpgradeInstanceFacing(index: Int, newFacing: String) {
        val currentList = editUpgrades.value.toMutableList()
        if (index in currentList.indices) {
            val uId = currentList[index].split(":").first()
            currentList[index] = "$uId:$newFacing"
            editUpgrades.value = currentList
        }
    }

    fun toggleUpgradeSelection(upgradeId: String) {
        val currentList = editUpgrades.value.toMutableList()
        val uIdClean = upgradeId.split(":").first()
        val isMultiBuy = uIdClean == "ram" || uIdClean == "exploding_ram" ||
                uIdClean == "armour_plating" || uIdClean == "extra_crewmember"
        if (isMultiBuy) {
            val existingIndex = currentList.indexOfFirst { it.split(":").first() == uIdClean }
            if (existingIndex != -1) {
                currentList.removeAt(existingIndex)
            } else {
                if (uIdClean == "ram" || uIdClean == "exploding_ram") {
                    currentList.add("$uIdClean:Front")
                } else {
                    currentList.add(uIdClean)
                }
            }
        } else {
            if (currentList.contains(upgradeId)) currentList.remove(upgradeId)
            else currentList.add(upgradeId)
        }
        editUpgrades.value = currentList
    }

    fun saveVehicle() {
        viewModelScope.launch {
            val name = editName.value.ifBlank { "Custom ${editChassisId.value.replaceFirstChar { it.uppercase() }}" }
            val chassis = selectedChassis.value

            val resolvedMaxHull = com.example.data.models.calculateMaxHull(editChassisId.value, editUpgrades.value)

            val weaponsCsv = editWeapons.value.joinToString(",")
            val upgradesCsv = editUpgrades.value.joinToString(",")

            val ammoList = mutableListOf<String>()
            editWeapons.value.forEach { item ->
                val wId = item.split(":").first()
                val weaponObj = GaslandsStaticData.weaponList.find { it.id == wId }
                if (weaponObj != null && weaponObj.maxAmmo > 0) {
                    val maxAmmo = if (editSponsorId.value == "rutherford" && weaponObj.maxAmmo == 3) 4 else weaponObj.maxAmmo
                    ammoList.add("$wId:$maxAmmo")
                }
            }
            if (editUpgrades.value.any { val u = it.split(":").first().trim(); u == "nitro" || u == "nitro_booster" }) {
                ammoList.add("nitro:1")
            }
            val ammoMapCsv = ammoList.joinToString(",")

            val vehicleId = editingVehicleId.value
            if (vehicleId != null) {
                val existing = repository.getVehicleDirect(vehicleId)
                if (existing != null) {
                    val updatedHull = if (existing.currentHull == -1 || existing.chassisId != editChassisId.value) {
                        resolvedMaxHull
                    } else {
                        existing.currentHull
                    }
                    val updated = Vehicle(
                        id = vehicleId,
                        name = name,
                        sponsorId = editSponsorId.value,
                        chassisId = editChassisId.value,
                        weaponsCsv = weaponsCsv,
                        upgradesCsv = upgradesCsv,
                        notes = editNotes.value,
                        currentGear = existing.currentGear,
                        currentHazards = existing.currentHazards,
                        currentHull = updatedHull,
                        ammoMapCsv = if (existing.chassisId != editChassisId.value) ammoMapCsv else existing.ammoMapCsv,
                        isOnFire = existing.isOnFire,
                        spectatorVotes = existing.spectatorVotes,
                        currentTurnGear = existing.currentTurnGear,
                        activatedInCurrentGear = existing.activatedInCurrentGear,
                        currentCrew = if (existing.chassisId != editChassisId.value) -1 else existing.currentCrew
                    )
                    repository.update(updated)
                }
            } else {
                val newVehicle = Vehicle(
                    name = name,
                    sponsorId = editSponsorId.value,
                    chassisId = editChassisId.value,
                    weaponsCsv = weaponsCsv,
                    upgradesCsv = upgradesCsv,
                    notes = editNotes.value,
                    currentGear = 1,
                    currentHazards = 0,
                    currentHull = resolvedMaxHull,
                    ammoMapCsv = ammoMapCsv,
                    isOnFire = false
                )
                repository.insert(newVehicle)
            }
            _currentScreen.value = AppScreen.ListScreen
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch { repository.delete(vehicle) }
    }

    // --- PLAY/DASHBOARD CONTROLLER STATE ---
    private val _activeVehicleIds = MutableStateFlow<List<Int>>(emptyList())
    val activeVehicleIds = _activeVehicleIds.asStateFlow()

    val activeVehiclesFlow: StateFlow<List<Vehicle>> = combine(_activeVehicleIds, vehicles) { ids, list ->
        list.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeVehicleFlow: StateFlow<Vehicle?> = activeVehiclesFlow.map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loadActiveVehicle(id: Int) { _activeVehicleIds.value = listOf(id) }

    fun loadActiveVehicles(ids: List<Int>) { _activeVehicleIds.value = ids }

    fun updateGear(vehicle: Vehicle, newGear: Int) {
        val maxGearSupported = vehicle.getMaxGear()
        val bondedGear = newGear.coerceIn(1, maxGearSupported)
        viewModelScope.launch { repository.update(vehicle.copy(currentGear = bondedGear)) }
    }

    fun updateHazards(vehicle: Vehicle, newHazards: Int) {
        val bondedHazards = newHazards.coerceIn(0, 6)
        val shouldExtinguish = bondedHazards == 0
        viewModelScope.launch {
            repository.update(
                vehicle.copy(
                    currentHazards = bondedHazards,
                    isOnFire = if (shouldExtinguish) false else vehicle.isOnFire
                )
            )
        }
    }

    fun resetHazards(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.update(vehicle.copy(currentHazards = 0, isOnFire = false))
        }
    }

    fun updateHull(vehicle: Vehicle, newHull: Int) {
        val chassis = GaslandsStaticData.chassisList.find { it.id == vehicle.chassisId } ?: return
        val armorCounts = vehicle.upgradesCsv.split(",").map { it.split(":").first() }
            .count { it == "armour_plating" || it == "armor_plating" }
        val maxHullWithArmor = chassis.maxHull + (armorCounts * 2)
        val bondedHull = newHull.coerceIn(0, maxHullWithArmor)
        viewModelScope.launch { repository.update(vehicle.copy(currentHull = bondedHull)) }
    }

    fun toggleUseAmmo(vehicle: Vehicle, itemId: String) {
        val items = vehicle.ammoMapCsv.split(",").toMutableList()
        val index = items.indexOfFirst { it.startsWith("$itemId:") }
        if (index != -1) {
            val amount = items[index].split(":")[1].toInt()
            if (amount > 0) {
                items[index] = "$itemId:${amount - 1}"
                viewModelScope.launch {
                    repository.update(vehicle.copy(ammoMapCsv = items.joinToString(",")))
                }
            }
        }
    }

    fun reloadAmmo(vehicle: Vehicle, itemId: String) {
        val weaponObj = GaslandsStaticData.weaponList.find { it.id == itemId }
        val baseMax = if (itemId == "nitro") 1 else (weaponObj?.maxAmmo ?: 0)
        val maxAmmoVal = if (itemId != "nitro" && vehicle.sponsorId == "rutherford" && baseMax == 3) 4 else baseMax
        if (maxAmmoVal > 0) {
            val items = vehicle.ammoMapCsv.split(",").toMutableList()
            val index = items.indexOfFirst { it.startsWith("$itemId:") }
            if (index != -1) items[index] = "$itemId:$maxAmmoVal"
            else items.add("$itemId:$maxAmmoVal")
            viewModelScope.launch {
                repository.update(vehicle.copy(ammoMapCsv = items.joinToString(",")))
            }
        }
    }

    fun updateFireState(vehicle: Vehicle, onFire: Boolean) {
        viewModelScope.launch { repository.update(vehicle.copy(isOnFire = onFire)) }
    }

    fun updateSpectatorVotes(newVotes: Int) {
        val bondedVotes = newVotes.coerceAtLeast(0)
        viewModelScope.launch {
            activeVehiclesFlow.value.forEach { vehicle ->
                repository.update(vehicle.copy(spectatorVotes = bondedVotes))
            }
        }
    }

    fun updateCurrentTurnGear(newTurnGear: Int) {
        val bondedTurnGear = newTurnGear.coerceIn(1, 6)
        viewModelScope.launch {
            activeVehiclesFlow.value.forEach { vehicle ->
                val changed = bondedTurnGear != vehicle.currentTurnGear
                repository.update(
                    vehicle.copy(
                        currentTurnGear = bondedTurnGear,
                        activatedInCurrentGear = if (changed) false else vehicle.activatedInCurrentGear
                    )
                )
            }
        }
    }

    fun updateActivatedInCurrentGear(vehicle: Vehicle, activated: Boolean) {
        viewModelScope.launch { repository.update(vehicle.copy(activatedInCurrentGear = activated)) }
    }

    fun updateCrew(vehicle: Vehicle, newCrew: Int) {
        val maxCrew = vehicle.getCrew()
        val bondedCrew = newCrew.coerceIn(0, maxCrew)
        viewModelScope.launch { repository.update(vehicle.copy(currentCrew = bondedCrew)) }
    }

    fun resetActiveVehiclesAndExit(onBack: () -> Unit) {
        viewModelScope.launch {
            activeVehiclesFlow.value.forEach { vehicle ->
                val maxHull = vehicle.getMaxHull()

                val ammoList = mutableListOf<String>()
                val weaponsList = if (vehicle.weaponsCsv.isEmpty()) emptyList() else vehicle.weaponsCsv.split(",")
                weaponsList.forEach { item ->
                    val wId = item.split(":").first().trim()
                    if (wId.isNotEmpty()) {
                        val weaponObj = GaslandsStaticData.weaponList.find { it.id == wId }
                        if (weaponObj != null && weaponObj.maxAmmo > 0) {
                            val actualMax = if (vehicle.sponsorId == "rutherford" && weaponObj.maxAmmo == 3) 4 else weaponObj.maxAmmo
                            ammoList.add("$wId:$actualMax")
                        }
                    }
                }
                val upgradesList = if (vehicle.upgradesCsv.isEmpty()) emptyList() else vehicle.upgradesCsv.split(",")
                if (upgradesList.any { val u = it.split(":").first().trim(); u == "nitro" || u == "nitro_booster" }) {
                    ammoList.add("nitro:1")
                }

                repository.update(
                    vehicle.copy(
                        currentGear = 1,
                        currentHazards = 0,
                        currentHull = maxHull,
                        ammoMapCsv = ammoList.joinToString(","),
                        isOnFire = false,
                        spectatorVotes = 0,
                        currentTurnGear = 1,
                        activatedInCurrentGear = false,
                        currentCrew = -1
                    )
                )
            }
            onBack()
        }
    }
}
