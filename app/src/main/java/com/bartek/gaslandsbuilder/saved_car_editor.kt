package com.bartek.gaslandsbuilder

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.*

class saved_car_editor : AppCompatActivity() {
    lateinit var chosenVehicle: ChosenVehicle
    val weaponActivityRequestCode = 0
    val upgradeActivityRequestCode = 1
    val perksActivityRequestCode = 2
    var weaponsUpgradesPerks: MutableList<Parcelable> = mutableListOf()
    lateinit var weaponsUpgadesPerksAdapter: WeaponsUpgadesPerksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_creator)
        val savedCarId: Int? = intent.extras?.getInt("id")
        val savedCar = getSingleCar(this, savedCarId!!)
        chosenVehicle = ChosenVehicle(
            getVehicleOnName(this, savedCar.type),
            chosenWeapons = savedCar.getWeaponsList(),
            chosenUpgrades = savedCar.getUpgradesList(),
            chosenPerks = savedCar.getPerksList(),
            cost = 0
        )
        (findViewById<EditText>(R.id.carNameInput)).apply {
            setText(savedCar.name)
            isEnabled = false
        }
        val addWeaponButton: Button = findViewById(R.id.addWeaponButton)
        addWeaponButton.setOnClickListener{
            startActivityForResult(Intent(this, WeaponCreator::class.java), weaponActivityRequestCode)
        }
        val addUpgradeButton: Button = findViewById(R.id.addUpgradeButton)
        addUpgradeButton.setOnClickListener{
            startActivityForResult(Intent(this, addUpgrade::class.java), upgradeActivityRequestCode)
        }
        val addPerkButton: Button = findViewById(R.id.addPerkButton)
        addPerkButton.setOnClickListener {
            startActivityForResult(Intent(this, addPerk::class.java), perksActivityRequestCode)
        }

        val carTypeSpinner: Spinner = findViewById(R.id.carTypeSpinner)
        val allVehicles = getAllVehicles(this)
        val adapter = CarTypeSpinnerAdapter(allVehicles)
        carTypeSpinner.adapter = adapter
        carTypeSpinner.setSelection(allVehicles.indexOf(chosenVehicle.type))
        carTypeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                chosenVehicle.type = parent.getItemAtPosition(position) as Vehicle
                updateSumCost()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
        val saveChangesButton: Button = findViewById(R.id.saveCarButton)
        saveChangesButton.text = "Save Changes"
        saveChangesButton.setOnClickListener{
            val db: SQLiteDatabase = DbHelper(
                this,
                "savedCarsDB",
                this.resources.getInteger(R.integer.savedCarsDBVersion)
            ).writableDatabase
            updateCar(
                SavedCar(
                    name = savedCar.name,
                    cost = chosenVehicle.calculateCost(),
                    type = chosenVehicle.type!!.name,
                    weapons = chosenVehicle.chosenWeapons.joinToString("") {
                        it.to_str()
                    },
                    upgrades = chosenVehicle.chosenUpgrades.joinToString("") {
                        if (it.onAdd != null){ it.onAdd?.invoke(chosenVehicle.type!!) }
                        it.to_str()
                    },
                    hull = chosenVehicle.type!!.hull,
                    handling = chosenVehicle.type!!.handling,
                    maxGear = chosenVehicle.type!!.maxGear,
                    crew = chosenVehicle.type!!.crew,
                    specialRules = chosenVehicle.type!!.specialRules,
                    weight = chosenVehicle.type!!.weight,
                    perks = chosenVehicle.chosenPerks.joinToString("") { it.to_str() }
                ),
                db,
                savedCarId!!
            )
            finish()
        }
    }

    override fun onResume(){
        super.onResume()
        notifier()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            weaponActivityRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    chosenVehicle.chosenWeapons.add(data!!.getParcelableExtra("chosenWeapon")!!)
                    notifier()
                    updateSumCost()
                }
            }
            upgradeActivityRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val chosenUpgrade = data!!.getParcelableExtra<Upgrade>("chosenUpgrade")!!
                    chosenVehicle.chosenUpgrades.add(chosenUpgrade)
                    notifier()
                    updateSumCost()
                }
            }
            perksActivityRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val chosenPerk = data!!.getParcelableExtra<Perk>("chosenPerk")!!
                    chosenVehicle.chosenPerks.add(chosenPerk)
                    notifier()
                    updateSumCost()
                }
            }
        }
    }

    fun updateSumCost(){
        val sumCost: TextView = findViewById(R.id.sumCost)
        val sumSlots: TextView = findViewById(R.id.sumSlots)
        sumCost.text = "${ chosenVehicle.calculateCost() } cans"
        val freeSlots = chosenVehicle.calculateBuildSlots()
        sumSlots.text = "${freeSlots}/${chosenVehicle.type?.buildSlots} slots"
        if (freeSlots < 0){
            sumSlots.setTextColor(Color.RED)
            Toast.makeText(
                applicationContext,
                "You exceeded build slots limit.",
                Toast.LENGTH_SHORT
            ).show()
        } else sumSlots.setTextColor(TextView(this).textColors)
    }

    fun notifier(){
//        weaponsUpgadesPerksAdapter.notifyDataSetChanged()
        weaponsUpgradesPerks = listOf(
            chosenVehicle.chosenWeapons.toList(),
            chosenVehicle.chosenUpgrades.toList(),
            chosenVehicle.chosenPerks.toList()
        ).flatten().toMutableList()
        weaponsUpgadesPerksAdapter = WeaponsUpgadesPerksAdapter(weaponsUpgradesPerks, ::removeWeaponUpgradePerk)
        val weaponsUpgadesPerksRecyclerView: RecyclerView = findViewById(R.id.weaponsUpgradesPerksList)
        weaponsUpgadesPerksRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = weaponsUpgadesPerksAdapter
        }
    }

    fun removeWeaponUpgradePerk(removable: Any): Unit{
        fun removeWeaponUpgrade(removable: Any): Unit{
            when (removable){
                is Weapon -> {
                    chosenVehicle.chosenWeapons.remove(removable)
                    weaponsUpgradesPerks.remove(removable)
                }
                is Upgrade -> {
                    chosenVehicle.chosenUpgrades.remove(removable)
                    weaponsUpgradesPerks.remove(removable)
                }
            }
        }

        fun removePerk(removable: Perk): Unit{
            chosenVehicle.chosenPerks.remove(removable)
            weaponsUpgradesPerks.remove(removable)
        }

        when (removable){
            is Weapon -> removeWeaponUpgrade(removable)
            is Upgrade -> removeWeaponUpgrade(removable)
            is Perk -> removePerk(removable)
        }
        updateSumCost()
        notifier()
    }

}