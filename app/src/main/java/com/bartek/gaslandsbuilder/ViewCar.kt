package com.bartek.gaslandsbuilder

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bartek.gaslandsbuilder.data.*
import kotlinx.android.synthetic.main.view_car_perks_row.view.*
import kotlinx.android.synthetic.main.view_car_upgrades_row.view.*
import kotlinx.android.synthetic.main.view_car_weapons_row.view.*
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

        val btn: Button = findViewById(R.id.button)
        btn.visibility = View.GONE
//        btn.setOnClickListener { saveBitmap(toBitmapFromView(findViewById(R.id.carFrame)), car.name) }

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

        val viewCarWeapons: LinearLayout = findViewById<LinearLayout>(R.id.viewCarWeapons)
        if (weaponsList.isNotEmpty()) {
            for (item in weaponsList) {
                val weaponsRow =
                    LayoutInflater.from(this).inflate(R.layout.view_car_weapons_row, null)
                weaponsRow.apply {
                    viewCarWeaponName.text = item.name
                    val ammo = item.ammo
                    if (ammo != 0) {
                        repeat(ammo) {
                            val ammoLinearLayout: LinearLayout = findViewById(R.id.ammoLayout)
                            val checkBox = CheckBox(context)
                            checkBox.text = ""
                            checkBox.layoutParams =
                                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT)
                            ammoLinearLayout.addView(checkBox)
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
                }
                viewCarWeapons.addView(weaponsRow)
            }
        }
        if (upgradesList.isNotEmpty()) {
            upgradesList.forEach {
                val upgradesRow =
                    LayoutInflater.from(this).inflate(R.layout.view_car_upgrades_row, null)
                upgradesRow.apply {
                    viewCarUpgradeName.text = it.name
                    upgradeSpecialRules.text = it.specRules
                    if (it.ammo == 0) {
                        upgradeAmmoCheckBox.visibility = View.GONE
                        viewCarAmmoText.visibility = View.GONE
                    }
                }
                viewCarWeapons.addView(upgradesRow)
            }
        }

        if (perksList.isNotEmpty()) {
            perksList.forEach {
                val perksRow = LayoutInflater.from(this).inflate(R.layout.view_car_perks_row, null)
                perksRow.apply {
                    viewCarPerkName.text = it.name
                    viewCarPerkClass.text = it.perkClass
                }
                viewCarWeapons.addView(perksRow)
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
