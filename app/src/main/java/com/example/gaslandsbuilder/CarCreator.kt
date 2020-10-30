package com.example.gaslandsbuilder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gaslandsbuilder.data.*
import kotlinx.android.synthetic.main.car_spinner_row.view.*
import kotlinx.android.synthetic.main.chosen_weapons_row.view.*

class CarCreator : AppCompatActivity() {
    val weaponActivityRequestCode = 0
    val chosenWeapons: MutableList<ChosenWeapon> = mutableListOf<ChosenWeapon>()
    val chosenWeaponsAdapter = ChosenWeaponAdapter(chosenWeapons, ::removeWeapon)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_creator)
        updateSumCost()
        val preferences = getPrefs()
        val addWeaponButton: Button = findViewById(R.id.addWeaponButton)
        addWeaponButton.setOnClickListener{
            startActivityForResult(Intent(this, WeaponCreator::class.java), weaponActivityRequestCode)
        }

        val carTypeSpinner: Spinner = findViewById(R.id.carTypeSpinner)
        val adapter = CarTypeSpinnerAdapter(getAllVehicles(this))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == weaponActivityRequestCode){
            if (resultCode == Activity.RESULT_OK){
                chosenWeapons.add(data!!.getParcelableExtra("chosenWeapon")!!)
                chosenWeaponsAdapter.notifyDataSetChanged()
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

        val db: SQLiteDatabase = DbHelper(
            this,
            "savedCarsDB",
            SAVED_CARS_DB_VERSION
        ).writableDatabase
        val addCarButton: Button = findViewById(R.id.saveCarButton)
        addCarButton.setOnClickListener{
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
                    weapons = chosenWeapons.joinToString(separator = ";") { it -> "${it.mount} mounted ${it.name}" }
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

    fun removeWeapon(weapon: ChosenWeapon): Unit{
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

    fun updateSumCost(){
        val preferences = getPrefs()
        val sumCost: TextView = findViewById(R.id.sumCost)
        val sumSlots: TextView = findViewById(R.id.sumSlots)
        preferences.edit().apply {
            putInt(
                "sumCarVal",
                preferences.getInt("sumWeaponsValue", 0) + preferences.getInt("vehicleTypeCost", 0)
            )
            putInt(
                "freeBuildSlots",
                preferences.getInt("buildSlots", 0) - preferences.getInt("takenSlots", 0)
            )
            apply()
        }
        sumCost.text = "${ preferences.getInt("sumCarVal", 0) } cans"
        sumSlots.text = "${preferences.getInt("freeBuildSlots", 0)}/${preferences.getInt("buildSlots", 0)}"
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

class ChosenWeaponAdapter(val chosenWeapons: MutableList<ChosenWeapon>, val weaponRemover:(ChosenWeapon) -> Unit): RecyclerView.Adapter<ChosenWeaponAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(weapon: ChosenWeapon, weaponRemover:(ChosenWeapon) -> Unit){
            itemView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            itemView.chosenWeaponName.text = "${weapon.mount} mounted ${weapon.name}"
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