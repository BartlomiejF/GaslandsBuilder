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
import com.bartek.gaslandsbuilder.data.SavedCar
import com.bartek.gaslandsbuilder.data.getMultipleCarsOnId
import kotlinx.android.synthetic.main.activity_view_car2.view.*
import kotlinx.android.synthetic.main.single_upgrade_row.view.*
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

            var weaponsList: List<String> = car.weapons.split(";")
            var upgradesList = car.upgrades.split(";")
            if (weaponsList[0] == ""){
                weaponsList = weaponsList.drop(1)
            }
            if (upgradesList[0] == ""){
                upgradesList = upgradesList.drop(1)
            }
            itemView.vehicleTypeText.text = car.type
            itemView.vehicleWeightText.text = car.weight

            val viewCarWeapons: LinearLayout = itemView.viewCarWeapons
            if (weaponsList.isNotEmpty()){
                itemView.viewCarWeaponsNoWeaponsText.visibility = View.GONE
                for (item in weaponsList){
                    val weaponsRow = LayoutInflater.from(context).inflate(R.layout.view_car_weapons_row, null)
                    val info = item.split(":")
                    weaponsRow.apply {
                        viewCarWeaponName.text = info[0]
                        val ammo = info[1].toInt()
                        if (ammo != 0){
                        repeat(ammo){
                            val ammoLinearLayout: LinearLayout = findViewById(R.id.ammoLayout)
                            val checkBox = CheckBox(context)
                            checkBox.text = ""
                            checkBox.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            ammoLinearLayout.addView(checkBox)
                        }
                        } else { ammoText.visibility = View.GONE }
                        rangeText.text = info[2]
                        if (info[3] != "null"){
                            weaponDamage.text = info[3]
                        } else {
                            weaponDamage.visibility = View.GONE
                        }
                        if (info[4] != "null"){
                            weaponSpecialRules.text = info[4]
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
                    val info = it.split(":")
                    upgradesRow.apply {
                        viewCarUpgradeName.text = info[0]
                        upgradeSpecialRules.text = info[3]
                        if (info[2] == "0") {
                            upgradeAmmoCheckBox.visibility = View.GONE
                            viewCarAmmoText.visibility = View.GONE
                        }
                    }
//                    val upgradeName = TextView(context)
//                    upgradeName.text = it.split(":")[0]
//                    val typeface = ResourcesCompat.getFont(context, R.font.bangers)
//                    upgradeName.typeface = typeface
                        viewCarWeapons.addView(upgradesRow)
                }
            }
//            itemView.viewCarWeapons.text = weaponsAndUpgradesList.joinToString(separator="\n"){ it.split(":")[0] }

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