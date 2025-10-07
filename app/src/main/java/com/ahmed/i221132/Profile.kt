package com.ahmed.i221132

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.hdodenhof.circleimageview.CircleImageView

class Profile : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val home_image = findViewById<ImageView>(R.id.home_image)
        val search_image = findViewById<ImageView>(R.id.search_image)
        val heart_image = findViewById<ImageView>(R.id.heart_image)
        val profile_image = findViewById<CircleImageView>(R.id.profile_image)
        val highlight1 = findViewById<CircleImageView>(R.id.highlight1)
        val highlight2 = findViewById<CircleImageView>(R.id.highlight2)
        val highlight3 = findViewById<CircleImageView>(R.id.highlight3)
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
            val intent = Intent(this, Highlight1::class.java)
            startActivity(intent)
        }
        highlight2.setOnClickListener{
            val intent = Intent(this, Highlight2::class.java)
            startActivity(intent)
        }
        highlight3.setOnClickListener {
            val intent = Intent(this, Highlight3::class.java)
            startActivity(intent)
        }

        editProfileBtn.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

    }
}