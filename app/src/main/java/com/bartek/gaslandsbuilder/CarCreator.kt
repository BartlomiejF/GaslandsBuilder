package com.bartek.gaslandsbuilder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.*
import kotlinx.android.synthetic.main.car_spinner_row.view.*
import kotlinx.android.synthetic.main.chosen_perk_row.view.*
import kotlinx.android.synthetic.main.chosen_upgrades_row.view.*
import kotlinx.android.synthetic.main.chosen_weapons_row.view.*

class CarCreator : AppCompatActivity() {
    val weaponActivityRequestCode = 0
    val upgradeActivityRequestCode = 1
    val perksActivityRequestCode = 2
    val chosenWeapons: MutableList<Weapon> = mutableListOf<Weapon>()
    val chosenUpgrades: MutableList<Upgrade> = mutableListOf<Upgrade>()
    val chosenPerks: MutableList<Perk> = mutableListOf()
    var weaponsUpgradesPerks: MutableList<Parcelable> = mutableListOf()
    var vehicles = mutableListOf<Vehicle>()
    lateinit var chosenVehicleType: Vehicle
    lateinit var weaponsUpgadesPerksAdapter: WeaponsUpgadesPerksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_creator)
        updateSumCost()
        vehicles = getAllVehicles(this)
        chosenVehicleType = vehicles[0]
        val preferences = getPrefs()
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
        val adapter = CarTypeSpinnerAdapter(vehicles)
        carTypeSpinner.adapter = adapter
        carTypeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                val carType = view.carType.text.toString()
                val carValue = view.carCans.text.toString().toInt()
                val freeSlots = view.buildSlots.text.toString().toInt()
                chosenVehicleType = parent.getItemAtPosition(position) as Vehicle
                preferences.edit().apply{
                    putInt("vehicleTypeCost", carValue)
                    putInt("buildSlots", freeSlots)
                    putString("carType", carType)
                    apply()
                }
                updateSumCost()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater = MenuInflater(this)
//        inflater.inflate(R.menu.menu_car_creator, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.getItemId()){
//            R.id.menuItemAbout -> startActivity(Intent(this, about::class.java))
//            R.id.menuItemWarnings -> startActivity(Intent(this, warnings::class.java))
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            weaponActivityRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    chosenWeapons.add(data!!.getParcelableExtra("chosenWeapon")!!)
                    notifier()
                }
            }
            upgradeActivityRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val chosenUpgrade = data!!.getParcelableExtra<Upgrade>("chosenUpgrade")!!
                    chosenUpgrades.add(chosenUpgrade)
                    notifier()
                }
            }
            perksActivityRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val chosenPerk = data!!.getParcelableExtra<Perk>("chosenPerk")!!
                    chosenPerks.add(chosenPerk)
                    notifier()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val preferences = getPrefs()
        updateSumCost()
        weaponsUpgradesPerks = listOf(chosenWeapons.toList(), chosenUpgrades.toList(), chosenPerks.toList()).flatten().toMutableList()
        weaponsUpgadesPerksAdapter = WeaponsUpgadesPerksAdapter(weaponsUpgradesPerks, ::removeWeaponUpgradePerk, chosenVehicleType)
        val weaponsUpgadesPerksRecyclerView: RecyclerView = findViewById(R.id.weaponsUpgradesPerksList)
        weaponsUpgadesPerksRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = weaponsUpgadesPerksAdapter
        }

        val addCarButton: Button = findViewById(R.id.saveCarButton)
        addCarButton.setOnClickListener{
            val db: SQLiteDatabase = DbHelper(
                this,
                "savedCarsDB",
                this.resources.getInteger(R.integer.savedCarsDBVersion)
            ).writableDatabase
            val carNameInput: String = findViewById<EditText>(R.id.carNameInput).text.toString()
            val carName: String = when (carNameInput){
                "" -> "Judgement Deliverer"
                else -> carNameInput
            }
            saveCar(
                SavedCar(
                    name = carName,
                    cost = preferences.getInt("sumCarVal", 0),
                    type = preferences.getString("carType", "Car")!!,
                    weapons = chosenWeapons.joinToString(separator = ";") {
                        var prefix = ""
                        var specRules = ""
                        if (it.mount!=null){
                            prefix = "${it.mount} mounted "
                            specRules = it.specialRules.toString()
                        } else {
                            specRules = "Crew Fired. ${specRules}"
                        }
                        return@joinToString "${prefix}${it.name}:${it.ammo}:${it.range}:${it.damage}:${specRules}"
                    },
                    upgrades = chosenUpgrades.joinToString(separator = ";") {
                        if (it.onAdd != null){ it.onAdd?.invoke(chosenVehicleType) }
                        "${it.name}:${it.cost}:${it.ammo}:${it.specRules}"
                    },
                    hull = chosenVehicleType.hull,
                    handling = chosenVehicleType.handling,
                    maxGear = chosenVehicleType.maxGear,
                    crew = chosenVehicleType.crew,
                    specialRules = chosenVehicleType.specialRules,
                    weight = chosenVehicleType.weight
                ),
                db
            )
            finish()
        }
    }

    fun getPrefs(): SharedPreferences{
        return this.getSharedPreferences(
            "singleCar",
            Context.MODE_PRIVATE
        )
    }

    fun removeWeaponUpgradePerk(removable: Any): Unit{
        fun removeWeaponUpgrade(removable: Any, prefs: SharedPreferences): Unit{
            when (removable){
                is Weapon -> {
                    prefs.edit().apply {
                    putInt(
                        "sumWeaponsValue",
                        prefs.getInt("sumWeaponsValue", 0) - removable.cost
                    )
                    putInt(
                        "takenSlots",
                        prefs.getInt("takenSlots", 0) - removable.buildSlots
                    )
                    apply()

                }
                    chosenWeapons.remove(removable)
                    weaponsUpgradesPerks.remove(removable)
                }
                is Upgrade -> {
                    prefs.edit().apply {
                    putInt(
                        "sumWeaponsValue",
                        prefs.getInt("sumWeaponsValue", 0) - removable.cost
                    )
                    putInt(
                        "takenSlots",
                        prefs.getInt("takenSlots", 0) - removable.buildSlots
                    )
                    apply()
                }
                    chosenUpgrades.remove(removable)
                    weaponsUpgradesPerks.remove(removable)
                }
            }
        }

        fun removePerk(removable: Perk, prefs: SharedPreferences): Unit{
            chosenPerks.remove(removable)
            weaponsUpgradesPerks.remove(removable)
            prefs.edit().apply {
                putInt(
                    "sumWeaponsValue",
                    prefs.getInt("sumWeaponsValue", 0) - removable.cost
                )
            }
        }

        val preferences = getPrefs()
        when (removable){
            is Weapon -> removeWeaponUpgrade(removable, preferences)
            is Upgrade -> removeWeaponUpgrade(removable, preferences)
            is Perk -> removePerk(removable, preferences)
        }
        updateSumCost()
        notifier()
    }

    fun notifier(){
//        weaponsUpgadesPerksAdapter.notifyDataSetChanged()
        weaponsUpgradesPerks = listOf(chosenWeapons.toList(), chosenUpgrades.toList(), chosenPerks.toList()).flatten().toMutableList()
        weaponsUpgadesPerksAdapter = WeaponsUpgadesPerksAdapter(weaponsUpgradesPerks, ::removeWeaponUpgradePerk, chosenVehicleType)
        val weaponsUpgadesPerksRecyclerView: RecyclerView = findViewById(R.id.weaponsUpgradesPerksList)
        weaponsUpgadesPerksRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = weaponsUpgadesPerksAdapter
        }
    }

    fun updateSumCost(){
        val preferences = getPrefs()
        val sumCost: TextView = findViewById(R.id.sumCost)
        val sumSlots: TextView = findViewById(R.id.sumSlots)
        val freeSlots = preferences.getInt("buildSlots", 0) - preferences.getInt("takenSlots",0)
        preferences.edit().apply {
            putInt(
                "sumCarVal",
                preferences.getInt("sumWeaponsValue", 0) + preferences.getInt("vehicleTypeCost", 0)
            )
            putInt(
                "freeBuildSlots",
                freeSlots
            )
            apply()
        }
        sumCost.text = "${ preferences.getInt("sumCarVal", 0) } cans"
        sumSlots.text = "$freeSlots/${preferences.getInt("buildSlots", 0)} slots"
        if (freeSlots < 0){
            sumSlots.setTextColor(Color.RED)
            Toast.makeText(
                applicationContext,
                "You exceeded build slots limit.",
                Toast.LENGTH_SHORT
            ).show()
        } else sumSlots.setTextColor(TextView(this).textColors)
    }
}

class CarTypeSpinnerAdapter(val carTypeList: MutableList<Vehicle>): BaseAdapter() {

    override fun getItem(position: Int): Any {
        return carTypeList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return carTypeList.size
    }

    override fun getView(position: Int, convertview: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.car_spinner_row, parent, false)
        view.carType.text = carTypeList[position].name
        view.carCans.text = carTypeList[position].cost.toString()
        view.buildSlots.text = carTypeList[position].buildSlots.toString()
        return view
    }
}

class WeaponsUpgadesPerksAdapter(val weaponsUpgradesPerks: MutableList<Parcelable>, val remover: (Any) -> Unit, val vehicle: Vehicle): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolderWeapon(view: View): RecyclerView.ViewHolder(view) {
        fun bind(weapon: Weapon, remover: (Any) -> Unit){
            itemView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            var prefix = ""
            if (weapon.mount!=null){ prefix = "${weapon.mount} mounted " }
            itemView.chosenWeaponName.text = "${prefix}${weapon.name}"
            itemView.removeChosenWeaponButton.setOnClickListener {
                remover(weapon)
            }
            itemView.requestLayout()
        }
    }

    class ViewHolderUpgrade(view: View): RecyclerView.ViewHolder(view){
        fun bind(upgrade: Upgrade, remover: (Any) -> Unit){
            itemView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            itemView.chosenUpgradesName.text = upgrade.name
            itemView.removeChosenUpgradesButton.setOnClickListener{
                remover(upgrade)
            }
            itemView.requestLayout()
        }
    }

    class ViewHolderPerk(view: View): RecyclerView.ViewHolder(view){
        fun bind(perk: Perk, remover: (Any) -> Unit){
            itemView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            itemView.chosenPerkName.text = perk.name
            itemView.removeChosenPerkButton.setOnClickListener{
                remover(perk)
            }
            itemView.requestLayout()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
        lateinit var holder: RecyclerView.ViewHolder
        when (weaponsUpgradesPerks[viewType]) {
            is Weapon -> holder = ViewHolderWeapon(view.inflate(R.layout.chosen_weapons_row, parent, false))
            is Upgrade -> holder = ViewHolderUpgrade(view.inflate(R.layout.chosen_upgrades_row, parent, false))
            is Perk -> holder = ViewHolderPerk(view.inflate(R.layout.chosen_perk_row, parent, false))
        }
        return holder
    }

    override fun getItemCount(): Int {
        return weaponsUpgradesPerks.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (weaponsUpgradesPerks[position]) {
            is Weapon -> (holder as ViewHolderWeapon).bind(weaponsUpgradesPerks[position] as Weapon, remover)
            is Upgrade -> (holder as ViewHolderUpgrade).bind(weaponsUpgradesPerks[position] as Upgrade, remover)
            is Perk -> (holder as ViewHolderPerk).bind(weaponsUpgradesPerks[position] as Perk, remover)
        }
    }
}

