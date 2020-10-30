package com.example.gaslandsbuilder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gaslandsbuilder.data.ChosenWeapon
import com.example.gaslandsbuilder.data.Weapon
import com.example.gaslandsbuilder.data.getAllWeaponNames
import kotlinx.android.synthetic.main.single_weapons_row.view.*

class WeaponCreator : AppCompatActivity() {
    lateinit var mount: String
    val mountType = arrayOf("Front", "Back", "Side", "Turret")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weapon_creator)
        updateSumCost()

        val carWeaponsRecyclerView: RecyclerView = findViewById(R.id.weaponsView)
        carWeaponsRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = CarWeaponAdapter(
                getAllWeaponNames(application),
                ::addWeapon,
                getPrefs().getInt("freeBuildSlots", 0)
            )
        }

        val spinner: Spinner = findViewById(R.id.mountSpinner)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            this.mountType
        )

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                this@WeaponCreator.mount = mountType[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

    }

    fun addWeapon(weapon: Weapon){
        var cost = weapon.cost
        val preferences = getPrefs()
        if (preferences.getInt("freeBuildSlots", 0) - weapon.buildSlots < 0){
            val toast = Toast.makeText(
                applicationContext,
                "Not enough free build slots.",
                Toast.LENGTH_SHORT)
            toast.show()
        } else {
            val mount = this@WeaponCreator.mount
            if (mount == "Turret") {
                cost *= 3
            }
            preferences.edit().apply {
                putInt(
                    "sumWeaponsValue",
                    preferences.getInt("sumWeaponsValue", 0) + cost
                )
                putInt(
                    "takenSlots",
                    preferences.getInt("takenSlots", 0) + weapon.buildSlots
                )
                apply()
            }
            val chosenWeapon = ChosenWeapon(weapon.name, cost, weapon.buildSlots, mount)
            val intent = Intent()
            intent.putExtra("chosenWeapon", chosenWeapon)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    fun getPrefs(): SharedPreferences{
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

class CarWeaponAdapter(val weaponsList: MutableList<Weapon>,
                       val weaponAdder:(Weapon) -> Unit,
                       val freeSlots: Int
    ): RecyclerView.Adapter<CarWeaponAdapter.ViewHolder>() {

    class ViewHolder(view: View, val weaponAdder:(Weapon) -> Unit, val freeSlots: Int): RecyclerView.ViewHolder(view) {
        fun bind(weapon: Weapon){
            itemView.weaponName.text = weapon.name
            itemView.slotsCost.text = "${weapon.buildSlots} slots"
            itemView.weaponCost.text = "${weapon.cost} cans"
            if ((freeSlots - weapon.buildSlots)<= (-1)){
                itemView.weaponName.setTextColor(Color.RED)
                itemView.slotsCost.setTextColor(Color.RED)
                itemView.weaponCost.setTextColor(Color.RED)
            }
            itemView.setOnClickListener{
                weaponAdder(weapon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_weapons_row, parent, false)
        return ViewHolder(view, weaponAdder, freeSlots)
    }

    override fun getItemCount(): Int {
        return weaponsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(weaponsList[position])
    }

}

