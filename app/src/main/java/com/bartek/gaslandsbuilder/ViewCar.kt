package com.bartek.gaslandsbuilder

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bartek.gaslandsbuilder.data.*
import com.bartek.gaslandsbuilder.databinding.ViewCarPerksRowBinding
import com.bartek.gaslandsbuilder.databinding.ViewCarUpgradesRowBinding
import com.bartek.gaslandsbuilder.databinding.ViewCarWeaponsRowBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ViewCar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_car2)
        val id = intent.extras?.getInt("id")
        val car: SavedCar = getSingleCar(this, id)
        val name: TextView = findViewById(R.id.viewCarName)
        name.text = car.name

        val onFireButton: ImageButton = findViewById(R.id.fireButton)
        onFireButton.visibility = View.GONE

        val btn: Button = findViewById(R.id.button)
        btn.visibility = View.GONE
//        btn.setOnClickListener { saveBitmap(toBitmapFromView(findViewById(R.id.carFrame)), car.name) }

        val resetHazardButton: Button = findViewById(R.id.resetHazard)
        resetHazardButton.visibility = View.GONE

        val hazardText: TextView = findViewById(R.id.hazardText)
        hazardText.visibility = View.GONE

        val hazardValue: TextView = findViewById(R.id.hazardValue)
        hazardValue.visibility = View.GONE

        val addHazard: ImageButton = findViewById(R.id.addHazard)
        addHazard.visibility = View.GONE

        val removeHazard: ImageButton = findViewById(R.id.removeHazard)
        removeHazard.visibility = View.GONE

        val carSponsor:TextView = findViewById(R.id.carSponsor)
        carSponsor.text = car.sponsor

        val cost: TextView = findViewById(R.id.viewCarCost)
        cost.text = car.cost.toString()

        val weaponsList: MutableList<Weapon> = car.getWeaponsList()
        val upgradesList: MutableList<Upgrade> = car.getUpgradesList()
        val perksList: List<Perk> = car.getPerksList()
        findViewById<TextView>(R.id.vehicleTypeText).text = car.type
        findViewById<TextView>(R.id.vehicleWeightText).text = car.weight
        if (weaponsList.isNotEmpty() or upgradesList.isNotEmpty() or perksList.isNotEmpty()) {
            findViewById<TextView>(R.id.viewCarWeaponsNoWeaponsText).visibility = View.GONE
        }

        val inflater = LayoutInflater.from(this)
        val viewCarWeapons: LinearLayout = findViewById<LinearLayout>(R.id.viewCarWeapons)
        if (weaponsList.isNotEmpty()) {
            for (item in weaponsList) {
                val weaponsBind = ViewCarWeaponsRowBinding.inflate(inflater, null, false)
                weaponsBind.apply {
                    viewCarWeaponName.text = item.name
                    val ammo = item.ammo
                    if (ammo != 0) {
                        repeat(ammo) {
                            val checkBox = CheckBox(this@ViewCar)
                            checkBox.text = ""
                            checkBox.layoutParams =
                                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT)
                            ammoLayout.addView(checkBox)
                        }
                    } else {
                        ammoText.visibility = View.GONE
                    }
                    rangeText.text = item.range
                    if (item.damage != "null") {
                        weaponDamage.text = item.damage
                    } else {
                        weaponDamage.visibility = View.GONE
                    }
                    if (item.specialRules != "null") {
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
        if (upgradesList.isNotEmpty()) {
            upgradesList.forEach {
                val upgradesBind = ViewCarUpgradesRowBinding.inflate(inflater, null, false)
//                val upgradesRow =
//                    LayoutInflater.from(this).inflate(R.layout.view_car_upgrades_row, null)
                upgradesBind.apply {
                    viewCarUpgradeName.text = it.name
                    upgradeSpecialRules.text = it.specRules
                    if (it.ammo == 0) {
                        upgradeAmmoCheckBox.visibility = View.GONE
                        viewCarAmmoText.visibility = View.GONE
                    }
                }
                viewCarWeapons.addView(upgradesBind.root)
            }
        }

        if (perksList.isNotEmpty()) {
            perksList.forEach {
                val perksBind = ViewCarPerksRowBinding.inflate(inflater, null, false)
//                val perksRow = LayoutInflater.from(this).inflate(R.layout.view_car_perks_row, null)
                perksBind.apply {
                    viewCarPerkName.text = it.name
                    viewCarPerkClass.text = it.perkClass
                }
                viewCarWeapons.addView(perksBind.root)
            }
        }

        findViewById<TextView>(R.id.maxGearValue).text = car.maxGear.toString()

        findViewById<TextView>(R.id.handlingValue).text = car.handling.toString()

        findViewById<TextView>(R.id.crewValue).text = car.crew.toString()

        repeat(car.hull) {
            val checkBox = CheckBox(this)
            checkBox.text = ""
            checkBox.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            if (it > 17) {
                findViewById<LinearLayout>(R.id.hullTableRowLayout3).addView(checkBox)
            } else if (it.rem(other = 2) == 0) {
                findViewById<LinearLayout>(R.id.hullTableRowLayout1).addView(checkBox)
            } else {
                findViewById<LinearLayout>(R.id.hullTableRowLayout2).addView(checkBox)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.dark_mode_menu, menu)
        menu!!.findItem(R.id.darkMode)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.darkMode -> {
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

    }

    fun toBitmapFromView(view: View): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val background = view.background
        view.layout(view.left, view.top, view.right, view.bottom)
        if (background != null) {
            canvas.drawColor(Color.WHITE)
            background.draw(canvas)
        }
        view.draw(canvas)
        return bitmap
    }


    fun saveBitmap(bitmap: Bitmap?, name: String) {
        val name = "$name.jpg"
        var fos: OutputStream? = null
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            //getting the contentResolver
            applicationContext?.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, name)
            fos = FileOutputStream(image)
        }

        fos.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        }
        fos?.flush()
        fos?.close()
        Toast.makeText(this, "Picture saved", Toast.LENGTH_SHORT).show()
    }
}
