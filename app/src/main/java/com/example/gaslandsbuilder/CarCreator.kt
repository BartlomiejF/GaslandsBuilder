package com.example.gaslandsbuilder

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gaslandsbuilder.data.Vehicle
import com.example.gaslandsbuilder.data.getAllVehicles
import kotlinx.android.synthetic.main.car_spinner_row.view.*

class CarCreator : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_creator)
        updateSumCost()
        val preferences = getPrefs()
        val addWeaponButton: Button = findViewById(R.id.addWeaponButton)
        addWeaponButton.setOnClickListener{
            startActivity(Intent(this, WeaponCreator::class.java))
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
                val carValue = view.carCans.text.toString().toInt()
                val freeSlots = view.buildSlots.text.toString().toInt()
                preferences.edit().apply{
                    putInt("vehicleTypeCost", carValue)
                    putInt("freeBuildSlots", freeSlots)
                    apply()
                }
                updateSumCost()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }

        }
    }

    override fun onResume() {
        super.onResume()
        val preferences = getPrefs()
        updateSumCost()
    }

    fun getPrefs(): SharedPreferences{
        return this.getSharedPreferences(
            "singleCar",
            Context.MODE_PRIVATE
        )
    }

    fun updateSumCost(){
        val preferences = getPrefs()
        val sumCost: TextView = findViewById(R.id.sumCost)
        preferences.edit().apply {
            putInt(
                "sumCarVal",
                preferences.getInt("sumWeaponsValue", 0) + preferences.getInt("vehicleTypeCost", 0)
            )
            apply()
        }
        sumCost.text = preferences.getInt("sumCarVal", 0).toString()
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