package com.bartek.gaslandsbuilder

import android.app.Activity
import android.content.Intent
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
        val sumCost: Int = intent.extras!!.getInt("cost")
        findViewById<TextView>(R.id.addUpgradeSumCarCost).text = sumCost.toString()
        val carUpgradesRecyclerView: RecyclerView = findViewById(R.id.upgradesView)
        carUpgradesRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = CarUpgradeAdapter(
                getAllUpgradesNames(application),
                ::addUpgrade
            )
        }
    }
    fun addUpgrade(upgrade: Upgrade){
        val intent = Intent()
        intent.putExtra("chosenUpgrade", upgrade)
        setResult(Activity.RESULT_OK, intent)
        finish()
        }
}

class CarUpgradeAdapter(val upgradesList: MutableList<Upgrade>,
                       val upgradeAdder:(Upgrade) -> Unit
): RecyclerView.Adapter<CarUpgradeAdapter.ViewHolder>() {

    class ViewHolder(view: View, val upgradeAdder:(Upgrade) -> Unit): RecyclerView.ViewHolder(view) {
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
        return ViewHolder(view, upgradeAdder)
    }

    override fun getItemCount(): Int {
        return upgradesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(upgradesList[position])
    }

}