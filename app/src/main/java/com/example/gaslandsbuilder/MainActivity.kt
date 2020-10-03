package com.example.gaslandsbuilder

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gaslandsbuilder.data.SavedCar
import com.example.gaslandsbuilder.data.getAllSavedCars
import kotlinx.android.synthetic.main.saved_car_row.view.*
import kotlinx.android.synthetic.main.saved_cars_chosen_weapons_listview_row.view.*

class MainActivity : AppCompatActivity() {

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

        val preferences: SharedPreferences = this.getSharedPreferences(
            "singleCar",
            Context.MODE_PRIVATE
        )

        preferences.edit().apply {
            putInt("sumCarVal", 0)
            putInt("vehicleTypeCost", 0)
            putInt("freeBuildSlots", 0)
            putInt("sumWeaponsValue", 0)
            apply()
        }

        val savedCarsRecyclerView: RecyclerView = findViewById(R.id.savedCarsRecyclerView)
        savedCarsRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = SavedCarsAdapter(getAllSavedCars(this@MainActivity))
            }

    }
}

class SavedCarsAdapter(val savedCars: MutableList<SavedCar>): RecyclerView.Adapter<SavedCarsAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(car: SavedCar){
            itemView.carName.text = car.name
            itemView.cost.text = "Cans: ${car.cost.toString()}"
            itemView.savedCarType.text = car.type
            val adapter = SavedCarsWeaponsAdapter(car.weapons.split(";"))
            itemView.savedCarWeaponsListView.adapter = adapter
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
        holder.bind(savedCars[position])
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