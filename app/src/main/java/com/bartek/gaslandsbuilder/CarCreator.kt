package com.bartek.gaslandsbuilder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.*
import kotlinx.android.synthetic.main.car_spinner_row.view.*
import kotlinx.android.synthetic.main.chosen_upgrades_row.view.*
import kotlinx.android.synthetic.main.chosen_weapons_row.view.*

class CarCreator : AppCompatActivity() {
    val weaponActivityRequestCode = 0
    val upgradeActivityRequestCode = 1
    val chosenWeapons: MutableList<Weapon> = mutableListOf<Weapon>()
    val chosenWeaponsAdapter = ChosenWeaponAdapter(chosenWeapons, ::removeWeapon)
    val chosenUpgrades: MutableList<Upgrade> = mutableListOf<Upgrade>()
    var vehicles = mutableListOf<Vehicle>()
    lateinit var chosenVehicleType: Vehicle
    lateinit var chosenUpgradesAdapter: ChosenUpgradesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_creator)
        updateSumCost()
        vehicles = getAllVehicles(this)
        chosenVehicleType = vehicles[0]
        chosenUpgradesAdapter = ChosenUpgradesAdapter(chosenUpgrades, ::removeUpgrade, chosenVehicleType)
        val preferences = getPrefs()
        val addWeaponButton: Button = findViewById(R.id.addWeaponButton)
        addWeaponButton.setOnClickListener{
            startActivityForResult(Intent(this, WeaponCreator::class.java), weaponActivityRequestCode)
        }
        val addUpgradeButton: Button = findViewById(R.id.addUpgradeButton)
        addUpgradeButton.setOnClickListener{
            startActivityForResult(Intent(this, addUpgrade::class.java), upgradeActivityRequestCode)
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
                    chosenWeaponsAdapter.notifyDataSetChanged()
                }
            }
            upgradeActivityRequestCode -> {
                if (resultCode == Activity.RESULT_OK){
                    val chosenUpgrade = data!!.getParcelableExtra<Upgrade>("chosenUpgrade")!!
                    chosenUpgrades.add(chosenUpgrade)
                    chosenUpgradesAdapter.notifyDataSetChanged()

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val preferences = getPrefs()
        updateSumCost()
        val chosenWeaponsRecyclerView: RecyclerView = findViewById(R.id.chosenWeaponsList)
        chosenWeaponsRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = chosenWeaponsAdapter
        }
        val chosenUpgradesRecyclerView: RecyclerView = findViewById(R.id.chosenUpgradesList)
        chosenUpgradesRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = chosenUpgradesAdapter
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
                        "${it.name}:${it.cost}" },
                    hull = chosenVehicleType.hull,
                    handling = chosenVehicleType.handling,
                    maxGear = chosenVehicleType.maxGear,
                    crew = chosenVehicleType.crew,
                    specialRules = chosenVehicleType.specialRules
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

    fun removeWeapon(weapon: Weapon): Unit{
        val preferences = getPrefs()
        preferences.edit().apply {
            putInt(
                "sumWeaponsValue",
                preferences.getInt("sumWeaponsValue", 0) - weapon.cost
            )
            putInt(
                "takenSlots",
                preferences.getInt("takenSlots", 0) - weapon.buildSlots
            )
            apply()
        }
        updateSumCost()
        chosenWeapons.remove(weapon)
        chosenWeaponsAdapter.notifyDataSetChanged()
    }

    fun removeUpgrade(upgrade: Upgrade): Unit{
        val preferences = getPrefs()
        preferences.edit().apply {
            putInt(
                "sumWeaponsValue",
                preferences.getInt("sumWeaponsValue", 0) - upgrade.cost
            )
            putInt(
                "takenSlots",
                preferences.getInt("takenSlots", 0) - upgrade.buildSlots
            )
            apply()
        }
        updateSumCost()
        chosenUpgrades.remove(upgrade)
        chosenUpgradesAdapter.notifyDataSetChanged()
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

class ChosenWeaponAdapter(val chosenWeapons: MutableList<Weapon>, val weaponRemover:(Weapon) -> Unit): RecyclerView.Adapter<ChosenWeaponAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(weapon: Weapon, weaponRemover:(Weapon) -> Unit){
            itemView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            var prefix = ""
            if (weapon.mount!=null){ prefix = "${weapon.mount} mounted " }
            itemView.chosenWeaponName.text = "${prefix}${weapon.name}"
            itemView.removeChosenWeaponButton.setOnClickListener{
                weaponRemover(weapon)
            }
            itemView.requestLayout()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chosen_weapons_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chosenWeapons.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chosenWeapons[position], weaponRemover)
    }
}

class ChosenUpgradesAdapter(val chosenUpgrades: MutableList<Upgrade>, val upgradeRemover:(Upgrade) -> Unit, val vehicle:Vehicle): RecyclerView.Adapter<ChosenUpgradesAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(upgrade: Upgrade, upgradeRemover:(Upgrade) -> Unit, vehicle: Vehicle){
            itemView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            itemView.chosenUpgradesName.text = upgrade.name
            itemView.removeChosenUpgradesButton.setOnClickListener{
                upgradeRemover(upgrade)
            }
            itemView.requestLayout()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chosen_upgrades_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chosenUpgrades.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chosenUpgrades[position], upgradeRemover, vehicle)
    }
}