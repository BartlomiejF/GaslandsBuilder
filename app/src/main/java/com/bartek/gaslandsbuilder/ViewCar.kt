package com.bartek.gaslandsbuilder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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