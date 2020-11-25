package com.example.gaslandsbuilder

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gaslandsbuilder.data.*
import kotlinx.android.synthetic.main.saved_car_row.view.*
import kotlinx.android.synthetic.main.saved_cars_chosen_weapons_listview_row.view.*

class MainActivity : AppCompatActivity() {
    lateinit var savedCars: MutableList<SavedCar>
    lateinit var savedCarsAdapter: SavedCarsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val createVehButton: Button = findViewById(R.id.createVehicle)
        createVehButton.setOnClickListener {
            startActivity(Intent(this, CarCreator::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        savedCars = getAllSavedCars(this)
        savedCarsAdapter = SavedCarsAdapter(savedCars,
            DbHelper(
                this@MainActivity,
                "savedCarsDB",
                SAVED_CARS_DB_VERSION
            ).writableDatabase,
            ::removeSavedCar
        )

        val preferences: SharedPreferences = this.getSharedPreferences(
            "singleCar",
            Context.MODE_PRIVATE
        )

        preferences.edit().apply {
            putInt("sumCarVal", 0)
            putInt("vehicleTypeCost", 0)
            putInt("freeBuildSlots", 0)
            putInt("sumWeaponsValue", 0)
            putInt("takenSlots", 0)
            apply()
        }



        val savedCarsRecyclerView: RecyclerView = findViewById(R.id.savedCarsRecyclerView)
        savedCarsRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = savedCarsAdapter
            }
    }

    fun removeSavedCar(car: SavedCar){
        savedCars.remove(car)
        savedCarsAdapter.notifyDataSetChanged()
    }

}

class SavedCarsAdapter(val savedCars: MutableList<SavedCar>, val db: SQLiteDatabase,
val carRemover: (SavedCar) -> Unit): RecyclerView.Adapter<SavedCarsAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(car: SavedCar, db: SQLiteDatabase, carRemover: (SavedCar) -> Unit){
            itemView.carName.text = car.name
            itemView.cost.text = "Cans: ${car.cost.toString()}"
            itemView.savedCarType.text = car.type
            val weaponsAndUpgradesList: MutableList<String> = mutableListOf<String>()
            weaponsAndUpgradesList.addAll(car.weapons.split(";"))
            weaponsAndUpgradesList.addAll(car.upgrades.split(";"))
            val adapter = SavedCarsWeaponsAdapter(weaponsAndUpgradesList)
            itemView.savedCarWeaponsListView.adapter = adapter
            itemView.deleteButton.setOnClickListener {
                deleteSavedCar(car.id!!, db)
                carRemover(car)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.saved_car_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return savedCars.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(savedCars[position], db, carRemover)
    }
}

class SavedCarsWeaponsAdapter(val weaponsList: List<String>): BaseAdapter() {

    override fun getItem(position: Int): Any {
        return weaponsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return weaponsList.size
    }

    override fun getView(position: Int, convertview: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.saved_cars_chosen_weapons_listview_row, parent, false)
        view.chosenWeapon.text = weaponsList[position]
        return view
    }
}