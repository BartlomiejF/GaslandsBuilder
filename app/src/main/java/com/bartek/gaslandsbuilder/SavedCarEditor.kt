package com.bartek.gaslandsbuilder

import com.bartek.gaslandsbuilder.*
import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.*

class SavedCarEditor : AppCompatActivity() {
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
            cost = 0,
            sponsor = savedCar.sponsor?.let { getSponsorOnName(this, it) }
        )
        chosenVehicle.chosenPerks.forEach { applyPerkSpecialRules(it, chosenVehicle) }
        (findViewById<EditText>(R.id.carNameInput)).apply {
            setText(savedCar.name)
            isEnabled = false
        }
        val addWeaponButton: Button = findViewById(R.id.addWeaponButton)
        addWeaponButton.setOnClickListener{
            val intent = Intent(this, WeaponCreator::class.java)
            intent.putExtra("cost", chosenVehicle.calculateCost())
            startActivityForResult(intent, weaponActivityRequestCode)
        }
        val addUpgradeButton: Button = findViewById(R.id.addUpgradeButton)
        addUpgradeButton.setOnClickListener{
            val intent = Intent(this, addUpgrade::class.java)
            intent.putExtra("cost", chosenVehicle.calculateCost())
            startActivityForResult(intent , upgradeActivityRequestCode)
        }
        val addPerkButton: Button = findViewById(R.id.addPerkButton)
        addPerkButton.setOnClickListener {
            val intent = Intent(this, addPerk::class.java)
            intent.putExtra("cost", chosenVehicle.calculateCost())
            intent.putExtra("sponsor", chosenVehicle.sponsor!!.name)
            startActivityForResult(intent, perksActivityRequestCode)
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
                chosenVehicle.chosenPerks.forEach { perk -> applyPerkSpecialRules(perk, chosenVehicle) }
                chosenVehicle.chosenWeapons.forEach{ weapon -> applyVehicleSpecialRules(chosenVehicle, applicationContext) }
                updateSumCost()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        val sponsors = getAllSponsors(this)
        val sponsorsSpinner: Spinner = findViewById(R.id.sponsorsSpinner)
        val sponsorsAdapter = SponsorsSpinnerAdapter(sponsors)
        sponsorsSpinner.adapter = sponsorsAdapter
        val currentSponsor = if (chosenVehicle.sponsor != null) {chosenVehicle.sponsor} else {
            chosenVehicle.sponsor = sponsors.last()
            chosenVehicle.sponsor
        }
        sponsorsSpinner.setSelection(sponsors.indexOf(currentSponsor))
        sponsorsSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                chosenVehicle.sponsor!!.sponsorPerks?.let {
                    chosenVehicle.chosenPerks.forEach { perk -> applyPerkSpecialRules(perk, chosenVehicle, onRemove = true) }
                    chosenVehicle.chosenPerks.removeAll(it)
                }
                chosenVehicle.sponsor = parent.getItemAtPosition(position) as Sponsor

                chosenVehicle.sponsor!!.sponsorPerks?.let {
                    chosenVehicle.chosenPerks.addAll(it)
                    chosenVehicle.chosenPerks.forEach { perk -> applyPerkSpecialRules(perk, chosenVehicle) }
                }
                val newSponsor = chosenVehicle.sponsor!!.name
                when (newSponsor) {
                    "The Warden" -> {
                        if (microPlateArmourPerk in chosenVehicle.chosenPerks) {
                            chosenVehicle.chosenPerks.removeAll(listOf(microPlateArmourPerk))
                        }
                    }
                    "Verney" -> {
                        if (prisonCarPerk in chosenVehicle.chosenPerks) {
                            chosenVehicle.chosenPerks.removeAll(listOf(prisonCarPerk))
                        }
                    }
                    else -> chosenVehicle.chosenPerks.removeAll(listOf(microPlateArmourPerk, prisonCarPerk))
                }
                notifier()
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
                    type = chosenVehicle.type!!.name,
                    weapons = chosenVehicle.chosenWeapons.joinToString("") {
                        it.to_str()
                    },
                    upgrades = chosenVehicle.chosenUpgrades.joinToString("") {
                        applyUpgradeSpecialRules(it, chosenVehicle)
                        it.to_str()
                    },
                    perks = chosenVehicle.chosenPerks.joinToString("") {
                        applyPerkSpecialRules(it, chosenVehicle, onSave = true)
                        it.to_str()
                    },
                    cost = chosenVehicle.calculateCost(),
                    hull = chosenVehicle.type!!.hull,
                    handling = chosenVehicle.type!!.handling,
                    maxGear = chosenVehicle.type!!.maxGear,
                    crew = chosenVehicle.type!!.crew,
                    specialRules = chosenVehicle.type!!.specialRules,
                    weight = chosenVehicle.type!!.weight,
                    sponsor = chosenVehicle.sponsor!!.name
                ),
                db,
                savedCarId!!
            )
            finish()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater = MenuInflater(this)
//        inflater.inflate(R.menu.dark_mode_menu, menu)
//        menu!!.findItem(R.id.darkMode)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.getItemId()) {
//            R.id.darkMode -> {
//                val nightModePrefs = getSharedPreferences("night_mode", MODE_PRIVATE)
//                if (nightModePrefs.getBoolean("night_mode", false)) {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                    nightModePrefs.edit().putBoolean("night_mode", false).commit()
//                } else {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                    nightModePrefs.edit().putBoolean("night_mode", true).commit()
//                }
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onResume(){
        super.onResume()
        notifier()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            weaponActivityRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val weaponToAdd: Weapon = data!!.getParcelableExtra("chosenWeapon")!!
                    if ("Gyrocopter Helicopter".contains(chosenVehicle.type!!.name)){
                        if (weaponToAdd.range=="dropped") weaponToAdd.buildSlots = 0
                    }
                    chosenVehicle.chosenWeapons.add(weaponToAdd)
                    chosenVehicle.chosenPerks.forEach { perk -> applyPerkSpecialRules(perk, chosenVehicle) }
                    notifier()
                    updateSumCost()
                }
            }
            upgradeActivityRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val chosenUpgrade = data!!.getParcelableExtra<Upgrade>("chosenUpgrade")!!
                    chosenVehicle.chosenUpgrades.add(chosenUpgrade)
                    chosenVehicle.chosenPerks.forEach { perk -> applyPerkSpecialRules(perk, chosenVehicle) }
                    notifier()
                    updateSumCost()
                }
            }
            perksActivityRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val chosenPerk = data!!.getParcelableExtra<Perk>("chosenPerk")!!
                    chosenVehicle.chosenPerks.add(chosenPerk)
                    applyPerkSpecialRules(chosenPerk, chosenVehicle)
                    chosenVehicle.chosenPerks.forEach { perk -> applyPerkSpecialRules(perk, chosenVehicle) }
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
        chosenVehicle.chosenPerks.forEach { perk -> applyPerkSpecialRules(perk, chosenVehicle) }
        updateSumCost()
        notifier()
    }

}