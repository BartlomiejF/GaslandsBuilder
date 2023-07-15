package com.bartek.gaslandsbuilder

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.SavedCar
import com.bartek.gaslandsbuilder.data.deleteSavedCar
import com.bartek.gaslandsbuilder.data.getAllSavedCars
import com.bartek.gaslandsbuilder.data.getMultipleCarsOnId
import com.bartek.gaslandsbuilder.databinding.SavedCarRowBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.io.File


class MainActivity : AppCompatActivity() {
    lateinit var savedCars: MutableList<SavedCar>
    lateinit var savedCarsAdapter: SavedCarsAdapter
    var carsToPlay = mutableListOf<Int>()
    var teamCost = 0
    var ads = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val createVehButton: Button = findViewById(R.id.createVehicle)
        createVehButton.setOnClickListener {
            startActivity(Intent(this, CarCreator::class.java))
        }
        ads = getSharedPreferences("ads_preferences", MODE_PRIVATE).getBoolean("ads", true)
        if (ads){
            MobileAds.initialize(this) {}
            val adRequest = AdRequest.Builder().build()
            findViewById<AdView>(R.id.adViewMain).loadAd(adRequest)
        } else {
            val params = createVehButton.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(0,8,24,0)
        }
        val nightMode = getSharedPreferences("night_mode", MODE_PRIVATE).getBoolean("night_mode", false)
        if (nightMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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
                val intent = Intent(this, GameTracker::class.java)
                intent.putExtra("ids", carsToPlay.joinToString(", "))
                intent.putExtra("ads", ads)
                startActivity(intent)
            }
            R.id.menuItemExport -> export(carsToPlay.joinToString(", "))
            R.id.menuItemCarOfTheMonth -> startActivity(Intent(this, image_of_the_month::class.java))
            R.id.menuDarkMode -> {
                val nightModePrefs = getSharedPreferences("night_mode", MODE_PRIVATE)
                if (nightModePrefs.getBoolean("night_mode", false)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    nightModePrefs.edit().putBoolean("night_mode", false).commit()
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    nightModePrefs.edit().putBoolean("night_mode", true).commit()
                }
            }
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

        if ("ads destroyer" in savedCars.map { it.name.lowercase() } && ads){
            ads = false
            val sharedPreferences = getSharedPreferences("ads_preferences", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("ads", false)
            editor.commit()
        }

        val savedCarsRecyclerView: RecyclerView = findViewById(R.id.savedCarsRecyclerView)
        savedCarsRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = savedCarsAdapter
            }
    }

    @SuppressLint("SuspiciousIndentation")
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
        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("${car.name} will be deleted!")
                setTitle("WARNING!")
                setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, id ->
                        savedCars.remove(car)
                        savedCarsAdapter.notifyDataSetChanged()
                        deleteSavedCar(car.id!!, context)
                        if (car.name.lowercase() == "ads destroyer") {
                            val sharedPreferences =
                                getSharedPreferences("ads_preferences", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("ads", true)
                            editor.commit()
                        }
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

    class ViewHolder(view: SavedCarRowBinding): RecyclerView.ViewHolder(view.root) {
        val binding = view

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
            binding.carName.text = car.name
            binding.cost.text = "Cans: ${car.cost}"
            binding.savedCarType.text = car.type
            binding.savedCarSponsor.text = car.sponsor
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

            binding.savedCarWeapons.text = weaponsAndUpgradesList
            binding.deleteButton.setOnClickListener {
                carRemover(car)
            }
            binding.viewCarButton.setOnClickListener{
                viewCar(car.id, context)
            }
            binding.editButton.setOnClickListener {
                editCar(car.id, context)
            }

            binding.markToPlay.setOnCheckedChangeListener(null)
            when (car.chosenToTracker){
                0 -> binding.markToPlay.isChecked = false
                1 -> binding.markToPlay.isChecked = true
            }

            binding.markToPlay.setOnCheckedChangeListener { _, isChecked ->
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

            val hamburgerButton = binding.hamburgerButton
            hamburgerButton.setOnClickListener{
                showMenu(it, R.menu.ovewrflow_menu)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.saved_car_row, parent, false)
        val inflater = LayoutInflater.from(parent?.context)
        return ViewHolder(SavedCarRowBinding.inflate(inflater, parent, false))
    }

    override fun getItemCount(): Int {
        return savedCars.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(savedCars[position], carRemover, context)
    }
}