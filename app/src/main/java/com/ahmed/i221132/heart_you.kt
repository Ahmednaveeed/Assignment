package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class heart_you : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_you)

        val tab_you = findViewById<TextView>(R.id.tab_you)
        val tab_following = findViewById<TextView>(R.id.tab_following)
        val home_image = findViewById<ImageView>(R.id.home_image)
        val search_image = findViewById<ImageView>(R.id.search_image)
        val add_post = findViewById<ImageView>(R.id.add_post)
        val heart_image = findViewById<ImageView>(R.id.heart_image)
        val profile_image = findViewById<ImageView>(R.id.profile_image)


        tab_you.setOnClickListener {
            val intent = Intent(this, heart_you::class.java)
            startActivity(intent)
        }
        tab_following.setOnClickListener {
            val intent = Intent(this, heart_following::class.java)
            startActivity(intent)
        }
        home_image.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        search_image.setOnClickListener {
            val intent = Intent(this, search::class.java)
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