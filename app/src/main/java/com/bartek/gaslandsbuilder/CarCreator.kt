package com.bartek.gaslandsbuilder

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
import com.bartek.gaslandsbuilder.databinding.*

class CarCreator : AppCompatActivity() {
    val weaponActivityRequestCode = 0
    val upgradeActivityRequestCode = 1
    val perksActivityRequestCode = 2
    val chosenVehicle = ChosenVehicle()
    var weaponsUpgradesPerks: MutableList<Parcelable> = mutableListOf()
    lateinit var weaponsUpgadesPerksAdapter: WeaponsUpgadesPerksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_creator)
        updateSumCost()
        val vehicles = getAllVehicles(this)
        val sponsors = getAllSponsors(this)
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
        val adapter = CarTypeSpinnerAdapter(vehicles)
        chosenVehicle.type = vehicles[0]
        carTypeSpinner.adapter = adapter
        carTypeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                val vehicleType = parent.getItemAtPosition(position) as Vehicle
                chosenVehicle.type = vehicleType
                chosenVehicle.chosenPerks.forEach { perk -> applyPerkSpecialRules(perk, chosenVehicle) }
                chosenVehicle.chosenWeapons.forEach{ weapon -> applyVehicleSpecialRules(weapon, chosenVehicle, applicationContext) }
                updateSumCost()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        val sponsorsSpinner: Spinner = findViewById(R.id.sponsorsSpinner)
        val sponsorsAdapter = SponsorsSpinnerAdapter(sponsors)
        chosenVehicle.sponsor = sponsors.last()
        sponsorsSpinner.adapter = sponsorsAdapter
        sponsorsSpinner.setSelection(sponsors.indexOf(chosenVehicle.sponsor!!))
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
                chosenVehicle.chosenPerks.removeAll(listOf(microPlateArmourPerk, prisonCarPerk))
                chosenVehicle.sponsor = parent.getItemAtPosition(position) as Sponsor
                chosenVehicle.sponsor!!.sponsorPerks?.let {
                    chosenVehicle.chosenPerks.addAll(it)
                    chosenVehicle.chosenPerks.forEach { perk -> applyPerkSpecialRules(perk, chosenVehicle) }
                }
                notifier()
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
                if (resultCode == RESULT_OK) {
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
                if (resultCode == RESULT_OK) {
                    val chosenUpgrade = data!!.getParcelableExtra<Upgrade>("chosenUpgrade")!!
                    chosenVehicle.chosenUpgrades.add(chosenUpgrade)
                    chosenVehicle.chosenPerks.forEach { perk -> applyPerkSpecialRules(perk, chosenVehicle) }
                    notifier()
                    updateSumCost()
                }
            }
            perksActivityRequestCode -> {
                if (resultCode == RESULT_OK) {
                    val chosenPerk = data!!.getParcelableExtra<Perk>("chosenPerk")!!
                    chosenVehicle.chosenPerks.add(chosenPerk)
                    applyPerkSpecialRules(chosenPerk, chosenVehicle)
                    notifier()
                    updateSumCost()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateSumCost()
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
                db
            )
            if (carName.lowercase() == "ads destroyer"){
                val sharedPreferences = getSharedPreferences("ads_preferences", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("ads", false)
                editor.commit()
            }
            finish()
        }
    }

    fun getPrefs(): SharedPreferences{
        return this.getSharedPreferences(
            "singleCar",
            MODE_PRIVATE
        )
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

    fun notifier(){
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

        val inflater = LayoutInflater.from(parent?.context)
        val binding = CarSpinnerRowBinding.inflate(inflater, parent, false)
        binding.carType.text = carTypeList[position].name
        binding.carCans.text = carTypeList[position].cost.toString()
        binding.buildSlots.text = carTypeList[position].buildSlots.toString()
        return binding.root
    }
}

class SponsorsSpinnerAdapter(val sponsorsList: MutableList<Sponsor>): BaseAdapter() {

    override fun getItem(position: Int): Any {
        return sponsorsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return sponsorsList.size
    }

    override fun getView(position: Int, convertview: View?, parent: ViewGroup?): View {
//        val view = LayoutInflater.from(parent?.context)
//            .inflate(R.layout.sponsor_spinner_row, parent, false)
        val inflater = LayoutInflater.from(parent?.context)
        val binding = SponsorSpinnerRowBinding.inflate(inflater, parent, false)
        binding.sponsorName.text = sponsorsList[position].name
        binding.sponsorPerkClassI.text = "${sponsorsList[position].perkClassI}, ${sponsorsList[position].perkClassII}"
        return binding.root
    }
}

class WeaponsUpgadesPerksAdapter(val weaponsUpgradesPerks: MutableList<Parcelable>, val remover: (Any) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolderWeapon(view: ChosenWeaponsRowBinding): RecyclerView.ViewHolder(view.root) {
        val binding = view

        fun bind(weapon: Weapon, remover: (Any) -> Unit){
            itemView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            var prefix = ""
            if ((weapon.mount!= null).and((weapon.mount) !="null")){ prefix = "${weapon.mount} mounted " }
            binding.chosenWeaponName.text = "${prefix}${weapon.name}"
            binding.removeChosenWeaponButton.setOnClickListener {
                remover(weapon)
            }
            itemView.requestLayout()
        }
    }

    class ViewHolderUpgrade(view: ChosenUpgradesRowBinding): RecyclerView.ViewHolder(view.root){
        val binding = view

        fun bind(upgrade: Upgrade, remover: (Any) -> Unit){
            itemView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            binding.chosenUpgradesName.text = upgrade.name
            binding.removeChosenUpgradesButton.setOnClickListener{
                remover(upgrade)
            }
            itemView.requestLayout()
        }
    }

    class ViewHolderPerk(view: ChosenPerkRowBinding): RecyclerView.ViewHolder(view.root){
        val binding = view

        fun bind(perk: Perk, remover: (Any) -> Unit){
            itemView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            binding.chosenPerkName.text = perk.name
            if ((perk.perkClass=="Sponsored Perk").and(perk.cost==0)){
                binding.removeChosenPerkButton.visibility = View.GONE
            }
            binding.removeChosenPerkButton.setOnClickListener{
                remover(perk)
            }
            itemView.requestLayout()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        val view = LayoutInflater.from(parent.context)
        lateinit var holder: RecyclerView.ViewHolder
        val inflater = LayoutInflater.from(parent?.context)
        when (weaponsUpgradesPerks[viewType]) {
            is Weapon -> holder = ViewHolderWeapon(ChosenWeaponsRowBinding.inflate(inflater, parent, false))
            is Upgrade -> holder = ViewHolderUpgrade(ChosenUpgradesRowBinding.inflate(inflater, parent, false))
            is Perk -> holder = ViewHolderPerk(ChosenPerkRowBinding.inflate(inflater, parent, false))
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

