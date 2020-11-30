package com.bartek.gaslandsbuilder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class about : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val supportButton: Button = findViewById(R.id.supportButton)
        supportButton.setOnClickListener{
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://github.com/BartlomiejF/GaslandsBuilder")
            startActivity(openURL)
        }
    }
}