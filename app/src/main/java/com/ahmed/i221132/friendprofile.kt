package com.ahmed.i221132

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class friendprofile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friendprofile)

        val home_image = findViewById<ImageView>(R.id.home_image)
        val search_image = findViewById<ImageView>(R.id.search_image)
        val heart_image = findViewById<ImageView>(R.id.heart_image)
        val profile_image = findViewById<ImageView>(R.id.profile_image)
        val back_button = findViewById<ImageView>(R.id.back_button)
        val messageBtn = findViewById<Button>(R.id.messageBtn)

        messageBtn.setOnClickListener {
            val intent = Intent(this, DMhammad::class.java)
            startActivity(intent)

        }
        back_button.setOnClickListener {
            val intent = Intent(this, DMhammad::class.java)
            finish()
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

        val followBtn = findViewById<Button>(R.id.followBtn)

        followBtn.setOnClickListener {
            if (followBtn.text == "Follow") {
                // Switch to Following
                followBtn.text = "following"
                followBtn.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                followBtn.setTextColor(Color.BLACK)
            } else {
                // Switch back to Follow
                followBtn.text = "Follow"
                followBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#8B4513"))
                followBtn.setTextColor(Color.WHITE)
            }
        }

    }
}