package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class socially : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 5000
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socially)

        auth = FirebaseAuth.getInstance() // Initialize Firebase Auth

        Handler(Looper.getMainLooper()).postDelayed({
            // Check if the current user is logged in
            val currentUser: FirebaseUser? = auth.currentUser

            val nextActivity = if (currentUser != null) {
                // User is logged in, go to Home Screen
                HomeActivity::class.java
            } else {
                // User is NOT logged in, go to the initial savedacc screen
                savedacc::class.java
            }

            val intent = Intent(this, nextActivity)
            startActivity(intent)
            finish()

        }, SPLASH_DELAY)
    }
}