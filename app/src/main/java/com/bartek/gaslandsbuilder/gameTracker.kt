package com.bartek.gaslandsbuilder

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.*
import kotlinx.android.synthetic.main.activity_view_car2.view.*
import kotlinx.android.synthetic.main.single_upgrade_row.view.*
import kotlinx.android.synthetic.main.view_car_perks_row.view.*
import kotlinx.android.synthetic.main.view_car_upgrades_row.view.*
import kotlinx.android.synthetic.main.view_car_weapons_row.view.*

class gameTracker : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tracker)
        val ids = intent.extras?.getString("ids")
        val cars = getMultipleCarsOnId(this, ids!!)
        val gameTrackerAdapter = GameTrackerAdapter(cars, this)
        val gameTrackerRecyclerView: RecyclerView = findViewById(R.id.gameTrackerRecyclerView)
        gameTrackerRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = gameTrackerAdapter
        }
    }

    override fun onBackPressed() {
        val alertDialog: AlertDialog? = this?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("Do you really want to finish the tracker?")
                setTitle("Back button pressed")
                setPositiveButton("Yes",
                    DialogInterface.OnClickListener { dialog, id ->
                        super.onBackPressed()
                        dialog.cancel()
                    })
                setNegativeButton("No",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            }
            builder.create()
        }
        alertDialog?.show()
    }
}

class GameTrackerAdapter(val cars: MutableList<SavedCar>, val context: Context): RecyclerView.Adapter<GameTrackerAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        fun bind(car: SavedCar, context: Context) {
            itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            itemView.viewCarName.text = car.name
            itemView.viewCarCost.text = "Cans: ${car.cost}"
            itemView.maxGearText.text = "Gear"

            val weaponsList: List<Weapon> = car.weapons
            val upgradesList: List<Upgrade> = car.upgrades
            val perksList: List<Perk> =
            itemView.vehicleTypeText.text = car.type
            itemView.vehicleWeightText.text = car.weight

            val viewCarWeapons: LinearLayout = itemView.viewCarWeapons
            if (weaponsList.isNotEmpty()){
                itemView.viewCarWeaponsNoWeaponsText.visibility = View.GONE
                for (item in weaponsList){
                    val weaponsRow = LayoutInflater.from(context).inflate(R.layout.view_car_weapons_row, null)
                    weaponsRow.apply {
                        viewCarWeaponName.text = item.name
                        val ammo = item.ammo
                        if (ammo != 0){
                        repeat(ammo){
                            val ammoLinearLayout: LinearLayout = findViewById(R.id.ammoLayout)
                            val checkBox = CheckBox(context)
                            checkBox.text = ""
                            checkBox.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            ammoLinearLayout.addView(checkBox)
                        }
                        } else { ammoText.visibility = View.GONE }
                        rangeText.text = item.range
                        if (item.damage != "null"){
                            weaponDamage.text = item.damage
                        } else {
                            weaponDamage.visibility = View.GONE
                        }
                        if (item.specialRules != "null"){
                            weaponSpecialRules.text = item.specialRules
                        } else {
                            weaponSpecialRules.visibility = View.GONE
                        }
                    }
                    viewCarWeapons.addView(weaponsRow)
                }
            }
            if (upgradesList.isNotEmpty()){
                upgradesList.forEach{
                    val upgradesRow = LayoutInflater.from(context).inflate(R.layout.view_car_upgrades_row, null)
                    upgradesRow.apply {
                        viewCarUpgradeName.text = it.name
                        upgradeSpecialRules.text = it.specRules
                        if (it.ammo == 0) {
                            upgradeAmmoCheckBox.visibility = View.GONE
                            viewCarAmmoText.visibility = View.GONE
                        }
                    }
                    viewCarWeapons.addView(upgradesRow)
                }
            }

            if (perksList.isNotEmpty()){
                perksList.forEach {
                    val perksRow = LayoutInflater.from(context).inflate(R.layout.view_car_perks_row, null)
                    perksRow.apply {
                        viewCarPerkName.text = it.name
                        viewCarPerkClass.text = it.perkClass
                    }
                    viewCarWeapons.addView(perksRow)
                }
            }

            itemView.maxGearValue.text = car.currentGear.toString()

            itemView.handlingValue.text = car.handling.toString()

            itemView.crewValue.text = car.crew.toString()

            repeat(car.hull){
                val checkBox = CheckBox(context)
                checkBox.text = ""
                checkBox.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                if (it>17){
                    itemView.hullTableRowLayout3.addView(checkBox)
                } else if (it.rem(other = 2)==0) {
                    itemView.hullTableRowLayout1.addView(checkBox)
                } else { itemView.hullTableRowLayout2.addView(checkBox) }
            }
            itemView.gearUpDownLayout.visibility = View.VISIBLE
            itemView.button.visibility = View.GONE
            itemView.gearUp.setOnClickListener {
                if (car.currentGear<car.maxGear){
                    car.currentGear+=1
                    itemView.maxGearValue.text = car.currentGear.toString()
                }
            }
            itemView.gearDown.setOnClickListener {
                if (car.currentGear>1) {
                    car.currentGear -= 1
                    itemView.maxGearValue.text = car.currentGear.toString()

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_view_car2, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cars.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cars[position], context)
    }
}