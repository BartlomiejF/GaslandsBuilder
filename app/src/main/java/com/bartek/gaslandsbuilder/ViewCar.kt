package com.bartek.gaslandsbuilder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bartek.gaslandsbuilder.data.SavedCar
import com.bartek.gaslandsbuilder.data.getSingleCar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ViewCar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_car)
        val id = intent.extras?.getInt("id")
        val car: SavedCar = getSingleCar(this, id)
        val name: TextView = findViewById(R.id.viewCarName)
        name.text = car.name

        val cost: TextView = findViewById(R.id.viewCarCost)
        cost.text = car.cost.toString()

        val weapons: TextView = findViewById(R.id.viewCarWeapons)
        val weaponsAndUpgrades: List<List<String>> = listOf(
            car.weapons.split(";"),
            car.upgrades.split(";")
        )
        val weaponsAndUpgradesList: List<String> = weaponsAndUpgrades.flatten()
        weapons.text = weaponsAndUpgradesList.joinToString(separator="\n"){ it.split(":")[0] }

        val btn: Button = findViewById(R.id.button)
        btn.setOnClickListener { toBitmapFromView(findViewById(R.id.carFrame), car.name) }

        val maxGearValue: TextView = findViewById(R.id.maxGearValue)
        maxGearValue.text = car.maxGear.toString()

        val handlingValue: TextView = findViewById(R.id.handlingValue)
        handlingValue.text = car.handling.toString()

        val crewValue: TextView = findViewById(R.id.crewValue)
        crewValue.text = car.crew.toString()

        val hullTableRowLayout1: LinearLayout = findViewById(R.id.hullTableRowLayout1)
        val hullTableRowLayout2: LinearLayout = findViewById(R.id.hullTableRowLayout2)
        repeat(car.hull){
            val checkBox = CheckBox(this)
            checkBox.text = ""
            checkBox.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            if (it>17){
              val hullTableRowLayout3: LinearLayout = findViewById(R.id.hullTableRowLayout3)
                hullTableRowLayout3.addView(checkBox)
            } else if (it.rem(other = 2)==0) {
                hullTableRowLayout1.addView(checkBox)
            } else { hullTableRowLayout2.addView(checkBox) }
        }
//            val gearUpDownLayout: LinearLayout = findViewById(R.id.gearUpDownLayout)
//            gearUpDownLayout.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
//        val btn: Button = findViewById(R.id.button)
//        btn.setOnClickListener { toBitmapFromView(findViewById(R.id.carFrame)) }
    }

    fun toBitmapFromView(view: View, name: String){
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val background = view.background
        view.layout(view.left, view.top, view.right, view.bottom)
        if (background != null){
            canvas.drawColor(Color.WHITE)
            background.draw(canvas)
        }
        view.draw(canvas)
        // Create a file to save the image
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${name}.jpg")
            val output = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
            output.flush()
            output.close()
            Toast.makeText(this, "saved at ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e:IOException){
            e.printStackTrace()
        }
    }
}