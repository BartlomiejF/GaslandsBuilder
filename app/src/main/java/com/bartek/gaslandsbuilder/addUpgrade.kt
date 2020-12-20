package com.bartek.gaslandsbuilder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.Upgrade
import com.bartek.gaslandsbuilder.data.getAllUpgradesNames
import kotlinx.android.synthetic.main.single_upgrade_row.view.*


class addUpgrade : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_upgrade)
        updateSumCost()

        val carUpgradesRecyclerView: RecyclerView = findViewById(R.id.upgradesView)
        carUpgradesRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = CarUpgradeAdapter(
                getAllUpgradesNames(application),
                ::addUpgrade,
                getPrefs().getInt("freeBuildSlots", 0)
            )
        }
    }
    fun addUpgrade(upgrade: Upgrade){
        val preferences = getPrefs()
        preferences.edit().apply {
            putInt(
                "sumWeaponsValue",
                preferences.getInt("sumWeaponsValue", 0) + upgrade.cost
            )
            putInt(
                "takenSlots",
                preferences.getInt("takenSlots", 0) + upgrade.buildSlots
            )
            apply()
        }
        when (upgrade.name) {
            "Extra Crewmember" -> {
                upgrade.onAdd = {
                    it.crew += 1
                }
            }
            "Armour Plating" -> {
                upgrade.onAdd = {
                    it.hull += 2
                }
            }
            "Tank Tracks" -> {
                upgrade.onAdd = {
                    it.maxGear -= 1
                    it.handling += 1
                }
            }
        }

        val intent = Intent()
        intent.putExtra("chosenUpgrade", upgrade)
        setResult(Activity.RESULT_OK, intent)
        finish()
        }

    fun getPrefs(): SharedPreferences {
        return this.getSharedPreferences(
            "singleCar",
            Context.MODE_PRIVATE
        )
    }

    fun updateSumCost(){
        val preferences = getPrefs()
        val sumCost: TextView = findViewById(R.id.sumWeaponCost)
        sumCost.text = preferences.getInt("sumCarVal", 0).toString()
    }

}

class CarUpgradeAdapter(val upgradesList: MutableList<Upgrade>,
                       val upgradeAdder:(Upgrade) -> Unit,
                       val freeSlots: Int
): RecyclerView.Adapter<CarUpgradeAdapter.ViewHolder>() {

    class ViewHolder(view: View, val upgradeAdder:(Upgrade) -> Unit, val freeSlots: Int): RecyclerView.ViewHolder(view) {
        fun bind(upgrade: Upgrade){
            itemView.upgradeName.text = upgrade.name
            itemView.slotsCost.text = "${kotlin.math.abs(upgrade.buildSlots)} slots"
            itemView.upgradeCost.text = "${upgrade.cost} cans"
            itemView.setOnClickListener{
                upgradeAdder(upgrade)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_upgrade_row, parent, false)
        return ViewHolder(view, upgradeAdder, freeSlots)
    }

    override fun getItemCount(): Int {
        return upgradesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(upgradesList[position])
    }

}