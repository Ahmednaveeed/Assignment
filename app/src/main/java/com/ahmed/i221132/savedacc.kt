package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class savedacc : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savedacc)
        auth = FirebaseAuth.getInstance()

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val switchAccounts = findViewById<TextView>(R.id.switchAccounts)
        val signUp = findViewById<TextView>(R.id.signUp)

        loginBtn.setOnClickListener {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // If currentUser is not null, it means Firebase already holds a session token.
                // We assume this is the 'previously saved account' and log them in immediately.

                Toast.makeText(this, "Welcome back, ${currentUser.email}!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Should theoretically not happen if the splash screen worked,
                // but if the token expired/was manually logged out, prompt full login.
                Toast.makeText(this, "No saved account found. Please log in.", Toast.LENGTH_LONG).show()
                // Redirects to the switch accounts flow, which is log in.
                val intent = Intent(this, login::class.java)
                startActivity(intent)
            }
        }

        switchAccounts.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }

        signUp.setOnClickListener {
            val intent = Intent(this, register::class.java)
            startActivity(intent)
        }


    }
}