package com.bartek.gaslandsbuilder

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.SavedCar
import com.bartek.gaslandsbuilder.data.deleteSavedCar
import com.bartek.gaslandsbuilder.data.getAllSavedCars
import kotlinx.android.synthetic.main.saved_car_row.view.*

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()){
            R.id.menuItemAbout -> startActivity(Intent(this, about::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        savedCars = getAllSavedCars(this)
        savedCarsAdapter = SavedCarsAdapter(savedCars,
            ::removeSavedCar,
            this
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
        val alertDialog: AlertDialog? = this?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("${car.name} will be deleted!")
                setTitle("WARNING!")
                setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, id ->
                        savedCars.remove(car)
                        savedCarsAdapter.notifyDataSetChanged()
                        deleteSavedCar(car.id!!, context)
                        dialog.cancel()
                    })
                setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            }

            builder.create()
        }
        alertDialog?.show()

//        savedCars.remove(car)
//        savedCarsAdapter.notifyDataSetChanged()
    }

}

class SavedCarsAdapter(val savedCars: MutableList<SavedCar>,
val carRemover: (SavedCar) -> Unit, val context: Context): RecyclerView.Adapter<SavedCarsAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        fun viewCar(id: Int?, context: Context){
            val intent = Intent(context, ViewCar::class.java)
            intent.putExtra("id", id)
            context.startActivity(intent)
        }

        fun bind(car: SavedCar, carRemover: (SavedCar) -> Unit, context: Context){
            itemView.carName.text = car.name
            itemView.cost.text = "Cans: ${car.cost.toString()}"
            itemView.savedCarType.text = car.type
//            var weaponsAndUpgradesList: MutableList<String> = mutableListOf()
//            weaponsAndUpgradesList.addAll(car.weapons.split(";"))
//            if (weaponsAndUpgradesList[0] == ""){
//                weaponsAndUpgradesList = car.upgrades.split(";") as MutableList<String>
//            } else {
//                weaponsAndUpgradesList.addAll(car.upgrades.split(";"))
//            }
//            itemView.savedCarWeapons.text = weaponsAndUpgradesList.joinToString(separator="\n")
            val weaponsAndUpgrades: List<List<String>> = listOf(
                car.weapons.split(";"),
                car.upgrades.split(";")
            )
            val weaponsAndUpgradesList: List<String> = weaponsAndUpgrades.flatten()
            itemView.savedCarWeapons.text = weaponsAndUpgradesList.joinToString(separator="\n"){ it.split(":")[0] }
            itemView.deleteButton.setOnClickListener {
                carRemover(car)
            }
            itemView.setOnClickListener{
                viewCar(car.id, context)
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
        holder.bind(savedCars[position], carRemover, context)
    }
}