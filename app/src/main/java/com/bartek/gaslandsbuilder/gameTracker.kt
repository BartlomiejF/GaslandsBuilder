package com.bartek.gaslandsbuilder

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.SavedCar
import com.bartek.gaslandsbuilder.data.getMultipleCarsOnId
import kotlinx.android.synthetic.main.activity_view_car.view.*

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
            itemView.viewCarCost.text = car.cost.toString()
            itemView.maxGearText.text = "Gear"

            val weaponsAndUpgrades: List<List<String>> = listOf(
                car.weapons.split(";"),
                car.upgrades.split(";")
            )

            val weaponsAndUpgradesList: List<String> = if (weaponsAndUpgrades[0][0] == ""){
                weaponsAndUpgrades.flatten().drop(1)
            } else {
                weaponsAndUpgrades.flatten()
            }
            itemView.viewCarWeapons.text = weaponsAndUpgradesList.joinToString(separator="\n"){ it.split(":")[0] }

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
            .inflate(R.layout.activity_view_car, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cars.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cars[position], context)
    }
}