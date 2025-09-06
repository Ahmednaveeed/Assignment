package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class savedacc : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savedacc)

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val switchAccounts = findViewById<TextView>(R.id.switchAccounts)
        val signUp = findViewById<TextView>(R.id.signUp)

        loginBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
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