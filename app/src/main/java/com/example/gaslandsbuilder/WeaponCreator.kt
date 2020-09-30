package com.example.gaslandsbuilder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
    val chosenWeapons: MutableList<ChosenWeapon> = mutableListOf<ChosenWeapon>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weapon_creator)
        val preferences = getPrefs()
        updateSumCost()

        val carWeaponsRecyclerView: RecyclerView = findViewById(R.id.weaponsView)
        carWeaponsRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = CarWeaponAdapter(getAllWeaponNames(application), ::addWeapon)
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
                Toast.makeText(this@WeaponCreator, mountType[position], Toast.LENGTH_SHORT).show()
                this@WeaponCreator.mount = mountType[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

    }

    fun addWeapon(weapon: Weapon){
        var cost = weapon.cost
        val preferences = getPrefs()
        val mount = this@WeaponCreator.mount
        when (mount){
            "Turret"-> cost *= 3
            else -> cost = cost
        }
        preferences.edit().apply {
            putInt(
                "sumWeaponsValue",
                preferences.getInt("sumWeaponsValue", 0) + cost
            )
            apply()
        }
        val chosenWeapon = ChosenWeapon(weapon.name, cost, weapon.buildSlots, mount)
        val intent = Intent()
        intent.putExtra("chosenWeapon", chosenWeapon)
        setResult(Activity.RESULT_OK, intent)
        finish()
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
        sumCost.text = preferences.getInt("sumWeaponsValue", 0).toString()
    }

}

class CarWeaponAdapter(val weaponsList: MutableList<Weapon>,
                       val weaponAdder:(Weapon) -> Unit
    ): RecyclerView.Adapter<CarWeaponAdapter.ViewHolder>() {

    class ViewHolder(view: View, val weaponAdder:(Weapon) -> Unit): RecyclerView.ViewHolder(view) {
        fun bind(weapon: Weapon){
            itemView.weaponName.text = weapon.name
            itemView.weaponCost.text = weapon.cost.toString()
            itemView.setOnClickListener{
                weaponAdder(weapon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_weapons_row, parent, false)
        return ViewHolder(view, weaponAdder)
    }

    override fun getItemCount(): Int {
        return weaponsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(weaponsList[position])
    }

}

