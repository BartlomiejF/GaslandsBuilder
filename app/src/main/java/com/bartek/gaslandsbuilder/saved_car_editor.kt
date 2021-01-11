package com.bartek.gaslandsbuilder

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.bartek.gaslandsbuilder.data.*
import kotlinx.android.synthetic.main.car_spinner_row.view.*
import kotlinx.android.synthetic.main.single_weapons_row.*

class saved_car_editor : AppCompatActivity() {
    lateinit var chosenVehicle: ChosenVehicle
    val weaponActivityRequestCode = 0
    val upgradeActivityRequestCode = 1
    val perksActivityRequestCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_creator)
        val savedCarId: Int? = intent.extras?.getInt("id")
        val savedCar = getSingleCar(this, savedCarId!!)
        chosenVehicle = ChosenVehicle(
            getVehicleOnName(this, savedCar.name),
            chosenWeapons = savedCar.getWeaponsList(),
            chosenUpgrades = savedCar.getUpgradesList(),
            chosenPerks = savedCar.getPerksList(),
            cost = savedCar.cost
        )
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
        val adapter = CarTypeSpinnerAdapter(getAllVehicles(this))
        carTypeSpinner.adapter = adapter
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
    }

    override fun onResume(){
        super.onResume()

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