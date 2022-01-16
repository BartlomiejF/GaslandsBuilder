package com.bartek.gaslandsbuilder

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.SavedCar
import com.bartek.gaslandsbuilder.data.deleteSavedCar
import com.bartek.gaslandsbuilder.data.getAllSavedCars
import com.bartek.gaslandsbuilder.data.getMultipleCarsOnId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.saved_car_row.view.*
import java.io.File


class MainActivity : AppCompatActivity() {
    lateinit var savedCars: MutableList<SavedCar>
    lateinit var savedCarsAdapter: SavedCarsAdapter
    var carsToPlay = mutableListOf<Int>()
    var teamCost = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val createVehButton: Button = findViewById(R.id.createVehicle)
        createVehButton.setOnClickListener {
            startActivity(Intent(this, CarCreator::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this@MainActivity)
        inflater.inflate(R.menu.menu_main, menu)
        menu!!.findItem(R.id.menuItemExport)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu!!.findItem(R.id.menuItemPlay)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()){
            R.id.menuItemAbout -> startActivity(Intent(this, about::class.java))
            R.id.menuItemPlay -> {
                val intent = Intent(this, gameTracker::class.java)
                intent.putExtra("ids", carsToPlay.joinToString(", "))
                startActivity(intent)
            }
            R.id.menuItemExport -> export(carsToPlay.joinToString(", "))
            R.id.menuItemCarOfTheMonth -> startActivity(Intent(this, image_of_the_month::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        teamCost = 0
        val teamCostValue: TextView = findViewById(R.id.teamCostValue)
        val teamCostText: TextView = findViewById(R.id.teamCostText)
        teamCostText.visibility = View.GONE
        teamCostValue.visibility = View.GONE
        carsToPlay.clear()
        savedCars = getAllSavedCars(this)
        savedCarsAdapter = SavedCarsAdapter(savedCars,
            ::removeSavedCar,
            this
        )

        val savedCarsRecyclerView: RecyclerView = findViewById(R.id.savedCarsRecyclerView)
        savedCarsRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = savedCarsAdapter
            }
    }

    fun export(ids: String){
        val cars = getMultipleCarsOnId(this, ids)
        val file = File(applicationContext.filesDir, "Gaslands Builder roster.txt")
            file.writeText(cars.joinToString("\r\n\n\n",
                prefix = "Team cost: ${teamCost}\n\n")
            { car ->
                car.getExportCarText()
            })
        val uri = FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID + "." + localClassName + ".provider",
            file)
        val share = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_VIEW
            data = uri
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        }, null)
        startActivity(share)
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

    fun teamCostCalculator(car: SavedCar, add:Boolean){
        val teamCostValue: TextView = findViewById(R.id.teamCostValue)
        val teamCostText: TextView = findViewById(R.id.teamCostText)
        if (add) {
            carsToPlay.add(car.id!!)
            teamCost += car.cost
            car.chosenToTracker = 1
        } else {
            carsToPlay.remove(car.id!!)
            teamCost -= car.cost
            car.chosenToTracker = 0
        }

        teamCostValue.text = teamCost.toString()
        if ((teamCostValue.visibility == View.VISIBLE) and ( teamCost == 0)) {
            teamCostText.visibility = View.GONE
            teamCostValue.visibility = View.GONE
        } else {
            teamCostText.visibility = View.VISIBLE
            teamCostValue.visibility = View.VISIBLE
        }
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

        fun editCar(id: Int?, context: Context){
            val intent = Intent(context, SavedCarEditor::class.java)
            intent.putExtra("id", id)
            context.startActivity(intent)
        }

        fun bind(car: SavedCar, carRemover: (SavedCar) -> Unit, context: Context){
            itemView.carName.text = car.name
            itemView.cost.text = "Cans: ${car.cost}"
            itemView.savedCarType.text = car.type
            itemView.savedCarSponsor.text = car.sponsor
            val weaponsAndUpgrades: MutableList<String> = mutableListOf(
                car.getWeaponsList().joinToString("\n") {
                    var text = "${it.name}"
                    if (it.mount != "null") {
                        text = "${it.mount} mounted ${it.name}"
                    }
                    return@joinToString text
                },
                car.getUpgradesList().joinToString("\n") { it.name },
                car.getPerksList().joinToString("\n") { it.name }
            )
           weaponsAndUpgrades.removeAll(listOf("", "\n"))
            val weaponsAndUpgradesList: String = if (weaponsAndUpgrades.isNotEmpty()){
                weaponsAndUpgrades.joinToString("\n")
            } else {
                ""
            }

            itemView.savedCarWeapons.text = weaponsAndUpgradesList
            itemView.deleteButton.setOnClickListener {
                carRemover(car)
            }
            itemView.viewCarButton.setOnClickListener{
                viewCar(car.id, context)
            }
            itemView.editButton.setOnClickListener {
                editCar(car.id, context)
            }

            itemView.markToPlay.setOnCheckedChangeListener(null)
            when (car.chosenToTracker){
                0 -> itemView.markToPlay.isChecked = false
                1 -> itemView.markToPlay.isChecked = true
            }

            itemView.markToPlay.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    (context as MainActivity).teamCostCalculator(car, true)

                } else {
                    (context as MainActivity).teamCostCalculator(car, false)
                }
            }
            fun showMenu(v: View, @MenuRes menuRes: Int) {
                val popup = PopupMenu(context!!, v)
                popup.menuInflater.inflate(menuRes, popup.menu)

                popup.setOnMenuItemClickListener {
                    when (it.itemId){
                        R.id.menuDelete -> carRemover(car)
                        R.id.menuEdit -> editCar(car.id, context)
                        R.id.menuView -> viewCar(car.id, context)
                    }
                    true

                }
                popup.setOnDismissListener {
                    // Respond to popup being dismissed.
                }
                // Show the popup menu.
                popup.show()
            }

            val hamburgerButton = itemView.hamburgerButton
            hamburgerButton.setOnClickListener{
                showMenu(it, R.menu.ovewrflow_menu)
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