package com.bartek.gaslandsbuilder

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.compose.ui.res.stringResource

class image_of_the_month : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_of_the_month)
        val inputStream = assets.open("images/${getString(R.string.car_of_the_month_image_name)}")
        val d = Drawable.createFromStream(inputStream, null)
        val imageOfTheMonth = findViewById<ImageView>(R.id.imageOfTheMonth)
        imageOfTheMonth.setImageDrawable(d)
    }
}