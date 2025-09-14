package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class search : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val home_image = findViewById<ImageView>(R.id.home_image)
        val heart_image = findViewById<ImageView>(R.id.heart_image)
        val profile_image = findViewById<ImageView>(R.id.profile_image)

        home_image.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        heart_image.setOnClickListener {
            val intent = Intent(this, heart_following::class.java)
            startActivity(intent)
        }
        profile_image.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

    }
}