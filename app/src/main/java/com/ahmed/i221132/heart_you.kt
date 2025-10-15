package com.ahmed.i221132

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class heart_you : AppCompatActivity() {

    // 🔑 NEW: Added Firebase properties
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_you)

        // 🔑 NEW: Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val tab_you = findViewById<TextView>(R.id.tab_you)
        val home_image = findViewById<ImageView>(R.id.home_image)
        val search_image = findViewById<ImageView>(R.id.search_image)
        val profile_image = findViewById<CircleImageView>(R.id.profile_image)

        // 🔑 NEW: Call the function to load the profile picture
        loadUserProfilePicture()

        tab_you.setOnClickListener {
            startActivity(Intent(this,heart_you::class.java))
        }
        home_image.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        search_image.setOnClickListener {
            startActivity(Intent(this, search::class.java))
        }
        profile_image.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }
    }

    // 🔑 NEW: Function to fetch and display the user's profile picture
    private fun loadUserProfilePicture() {
        val uid = auth.currentUser?.uid ?: return
        val profileImageView = findViewById<CircleImageView>(R.id.profile_image)

        database.getReference("users").child(uid).get().addOnSuccessListener { dataSnapshot ->
            val profileImageBase64 = dataSnapshot.child("profileImageBase64").value as? String
            if (!profileImageBase64.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    profileImageView.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    profileImageView.setImageResource(R.drawable.user)
                }
            } else {
                profileImageView.setImageResource(R.drawable.user)
            }
        }
    }
}