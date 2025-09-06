package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Callmartni : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_callmartini)

        val hangup_button = findViewById<ImageView>(R.id.hangup_button)

        hangup_button.setOnClickListener {
            val intent = Intent(this, DMmartini::class.java)
            startActivity(intent)
        }

    }
}