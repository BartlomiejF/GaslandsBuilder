package com.example.gaslandsbuilder

import android.content.Context
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
import kotlinx.android.synthetic.main.chosen_weapons_row.view.*
import kotlinx.android.synthetic.main.single_weapons_row.view.*

class WeaponCreator : AppCompatActivity() {
    lateinit var mount: String
    val mountType = arrayOf("Front", "Back", "Side", "Turret(cost x3)")
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

        val chosenWeaponsRecyclerView: RecyclerView = findViewById(R.id.chosenWeaponsList)
        chosenWeaponsRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = ChosenWeaponAdapter(chosenWeapons, ::removeWeapon)
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

    fun addWeapon(cost: Int, name: String, buildSlots: Int){
        var cost = cost
        val preferences = getPrefs()
        val mount = this@WeaponCreator.mount
        when (mount){
            "Turret(cost x3)"-> cost *= 3
            else -> cost = cost
        }
        preferences.edit().apply {
            putInt(
                "sumWeaponsValue",
                preferences.getInt("sumWeaponsValue", 0) + cost
            )
            apply()
        }
        chosenWeapons.add(ChosenWeapon(name, cost, buildSlots, mount))
        updateSumCost()
    }

    fun removeWeapon(position: Int): Unit{
        val preferences = getPrefs()
        preferences.edit().apply {
            putInt(
                "sumWeaponsValue",
                preferences.getInt("sumWeaponsValue", 0) - chosenWeapons[position].cost
            )
            apply()
        }
        updateSumCost()
        chosenWeapons.removeAt(position)
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
                       val weaponAdder:(Int, String, Int) -> Unit
    ): RecyclerView.Adapter<CarWeaponAdapter.ViewHolder>() {

    class ViewHolder(view: View, val weaponAdder:(Int, String, Int) -> Unit): RecyclerView.ViewHolder(view) {
        fun bind(weapon: Weapon){
            itemView.weaponName.text = weapon.name
            itemView.weaponCost.text = weapon.cost.toString()
            itemView.setOnClickListener{
                weaponAdder(weapon.addWeapon(), weapon.name, weapon.buildSlots)
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

class ChosenWeaponAdapter(val chosenWeapons: MutableList<ChosenWeapon>, val weaponRemover:(Int) -> Unit): RecyclerView.Adapter<ChosenWeaponAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(weapon: ChosenWeapon, weaponRemover:(Int) -> Unit, position: Int, notifier:(Int) -> Unit){
            itemView.chosenWeaponName.text = "${weapon.mount} mounted ${weapon.name}"
            itemView.removeChosenWeaponButton.setOnClickListener{
                weaponRemover(position)
                notifier(position)
            }
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
        holder.bind(chosenWeapons[position], weaponRemover, position, ::notifyItemRemoved)
    }

}