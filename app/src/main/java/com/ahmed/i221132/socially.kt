package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class socially : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socially)

        val socially = findViewById<ImageView>(R.id.socially)

        socially.setOnClickListener {
            val intent = Intent(this, savedacc::class.java)
            startActivity(intent)
        }
    }
}