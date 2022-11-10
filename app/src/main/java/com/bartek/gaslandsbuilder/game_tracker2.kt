package com.bartek.gaslandsbuilder

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat
import com.bartek.gaslandsbuilder.data.*
import com.bartek.gaslandsbuilder.databinding.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


class GameTracker : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tracker2)
        val ids = intent.extras?.getString("ids")
        val cars = getMultipleCarsOnId(this, ids!!)
        for (car in cars){
            car.mutableWeaponsList = car.getWeaponsList()
            car.mutableUpgradesList = car.getUpgradesList()
        }
        val gameTrackerAdapter = CarsAdapter(this, cars as ArrayList<SavedCar>)
        val gameTrackerListView: ListView = findViewById(R.id.gameTrackerListView)
        gameTrackerListView.apply {
            adapter = gameTrackerAdapter
        }

        var audiencePoints = 0
        val audiencePointsTextView = findViewById<TextView>(R.id.audiencePoints)
        audiencePointsTextView.text = audiencePoints.toString()

        findViewById<Button>(R.id.audienceVotePlus).setOnClickListener {
            audiencePoints +=1
            audiencePointsTextView.text = audiencePoints.toString()
        }
        findViewById<Button>(R.id.audienceVoteMinus).setOnClickListener {
            if (audiencePoints > 0) {
                audiencePoints -= 1
                audiencePointsTextView.text = audiencePoints.toString()
            }
        }

        MobileAds.initialize(this) {}
        if (intent.extras?.getBoolean("ads") == true){
            val adRequest = AdRequest.Builder().build()
            findViewById<AdView>(R.id.adView).loadAd(adRequest)
        }
    }


    override fun onBackPressed() {
        val alertDialog: AlertDialog? = this.let {
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

class CarsAdapter(private val context: Context,
                    private val dataSource: ArrayList<SavedCar>) : BaseAdapter() {

//    private val inflater: LayoutInflater
//            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    //1
    override fun getCount(): Int {
        return dataSource.size
    }

    //2
    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val inflater = LayoutInflater.from(parent?.context)
        val binding = GameTrackerCarRowBinding.inflate(inflater, parent, false)
        val car = dataSource[position]

        binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.viewCarName.text = car.name
        binding.viewCarCost.text = "Cans: ${car.cost}"
        binding.maxGearText.text = "Gear"
        binding.carSponsor.text = car.sponsor

        val perksList: List<Perk> = car.getPerksList()
        binding.vehicleTypeText.text = car.type
        binding.vehicleWeightText.text = car.weight
        if (car.mutableWeaponsList.isNotEmpty() or car.mutableUpgradesList.isNotEmpty() or perksList.isNotEmpty()){
            binding.viewCarWeaponsNoWeaponsText.visibility = View.GONE
        }

        val viewCarWeapons: LinearLayout = binding.viewCarWeapons
        if (car.mutableWeaponsList.isNotEmpty()){
            for (item in car.mutableWeaponsList){
                val weaponsBind = GameTrackerWeaponsRowBinding.inflate(inflater, null, false)
                weaponsBind.apply {
                    viewCarWeaponName.text = item.name
                    val ammo = item.ammo
                    if (ammo != 0){
                        ammoValue.text = (item.ammo - item.usedAmmo).toString()
                    } else {
                        ammoText.visibility = View.GONE
                        ammoValue.visibility = View.GONE
                        removeAmmo.visibility = View.GONE
                        addAmmo.visibility = View.GONE
                    }
                    removeAmmo.setOnClickListener {
                        item.usedAmmo += 1
                        ammoValue.text = (item.ammo - item.usedAmmo).toString()
                    }

                    addAmmo.setOnClickListener {
                        item.ammo += 1
                        ammoValue.text = (item.ammo - item.usedAmmo).toString()

                    }
                    rangeText.text = "Range: " + item.range.toString()
                    if (item.damage != "null"){
                        weaponDamage.text = "Damage: " + item.damage.toString()
                    } else {
                        weaponDamage.visibility = View.GONE
                    }
                    if (item.specialRules != "null"){
                        weaponSpecialRules.text = item.specialRules
                    } else {
                        weaponSpecialRules.visibility = View.GONE
                    }
                    if (item.mount != "null") {
                        weaponMount.text = item.mount.toString() + " mounted"
                    } else {
                        weaponMount.visibility = View.GONE
                    }
                }
                viewCarWeapons.addView(weaponsBind.root)
            }
        }
        if (car.mutableUpgradesList.isNotEmpty()){
            car.mutableUpgradesList.forEach{
                val upgradesBind = GameTrackerUpgradesRowBinding.inflate(inflater, null, false)
                upgradesBind.apply {
                    viewCarUpgradeName.text = it.name
                    upgradeSpecialRules.text = it.specRules
                    removeAmmo.setOnClickListener { _ ->
                        it.usedAmmo +=1
                        ammoValue.text = (it.ammo - it.usedAmmo).toString()
                    }
                    if (it.ammo == 0) {
                        viewCarAmmoText.visibility = View.GONE
                        ammoValue.visibility = View.GONE
                        removeAmmo.visibility = View.GONE
                    } else {
                        ammoValue.text = (it.ammo - it.usedAmmo).toString()
                    }
                }
                viewCarWeapons.addView(upgradesBind.root)
            }
        }

        if (perksList.isNotEmpty()){
            perksList.forEach {
                val perksBind = ViewCarPerksRowBinding.inflate(inflater, null, false)
//                val perksRow = LayoutInflater.from(context).inflate(R.layout.view_car_perks_row, null)
                perksBind.apply {
                    viewCarPerkName.text = it.name
                    viewCarPerkClass.text = it.perkClass
                }
                viewCarWeapons.addView(perksBind.root)
            }
        }

        binding.maxGearValue.text = car.currentGear.toString()

        binding.handlingValue.text = car.handling.toString()

        binding.crewValue.text = car.crew.toString()

        binding.hullValue.text = (car.hull-car.damageTaken).toString()
        binding.hullTextMax.text = "/${car.hull}"
        binding.addHull.setOnClickListener{
            car.damageTaken -= 1
            binding.hullValue.text = (car.hull-car.damageTaken).toString()
        }

        binding.removeHull.setOnClickListener{
            car.damageTaken += 1
            binding.hullValue.text = (car.hull-car.damageTaken).toString()
        }

        binding.gearUpDownLayout.visibility = View.VISIBLE
        binding.gearUp.setOnClickListener {
            if (car.currentGear<car.maxGear){
                car.currentGear+=1
                binding.maxGearValue.text = car.currentGear.toString()
            }
        }
        binding.gearDown.setOnClickListener {
            if (car.currentGear>1) {
                car.currentGear -= 1
                binding.maxGearValue.text = car.currentGear.toString()
            }
        }

        when (car.onFire){
            false -> {
                DrawableCompat.setTint(
                    DrawableCompat.wrap(binding.fireButton.drawable),
                    binding.viewCarName.currentTextColor
                )
            }
            true -> DrawableCompat.setTint(
                DrawableCompat.wrap(binding.fireButton.drawable),
                Color.RED
            )
        }

        binding.fireButton.setOnClickListener {
            car.onFire = if (car.onFire) {
                DrawableCompat.setTint(
                    DrawableCompat.wrap(binding.fireButton.drawable),
                    binding.viewCarName.currentTextColor
                )
                false
            } else {
                DrawableCompat.setTint(
                    DrawableCompat.wrap(binding.fireButton.drawable),
                    Color.RED
                )
                true
            }
        }

        binding.hazardValue.text = car.hazard.toString()
        binding.resetHazard.setOnClickListener {
            if (car.hazard>0){
                car.hazard = 0
                binding.hazardValue.text = car.hazard.toString()
            }
            if (car.onFire) binding.fireButton.performClick()
        }

        binding.addHazard.setOnClickListener{
            car.hazard += 1
            binding.hazardValue.text = car.hazard.toString()
        }

        binding.removeHazard.setOnClickListener {
            if (car.hazard>0){
                car.hazard -= 1
                binding.hazardValue.text = car.hazard.toString()
            }
            if (car.hazard - 1 < 0){
                if (car.onFire) binding.fireButton.performClick()
            }
        }
        return binding.root
    }

}
