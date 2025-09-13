package com.ahmed.i221132

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Profile : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val home_image = findViewById<ImageView>(R.id.home_image)
        val search_image = findViewById<ImageView>(R.id.search_image)
        val add_post = findViewById<ImageView>(R.id.add_post)
        val heart_image = findViewById<ImageView>(R.id.heart_image)
        val profile_image = findViewById<ImageView>(R.id.profile_image)
        val highlight1 = findViewById<ImageView>(R.id.highlight1)
        val highlight2 = findViewById<ImageView>(R.id.highlight2)
        val highlight3 = findViewById<ImageView>(R.id.highlight3)
        val editProfileBtn = findViewById<Button>(R.id.editProfileBtn)

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

        highlight1.setOnClickListener {
            val intent = Intent(this, highlight1::class.java)
            startActivity(intent)
        }
        highlight2.setOnClickListener {
            val intent = Intent(this, highlight2::class.java)
            startActivity(intent)
        }
        highlight3.setOnClickListener {
            val intent = Intent(this, highlight3::class.java)
            startActivity(intent)
        }

        editProfileBtn.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

    }
}