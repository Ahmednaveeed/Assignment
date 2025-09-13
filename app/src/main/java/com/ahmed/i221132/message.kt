package com.ahmed.i221132

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class message : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val dm1 = findViewById<LinearLayout>(R.id.dm1)
        val dm2 = findViewById<LinearLayout>(R.id.dm2)
        val dm3 = findViewById<LinearLayout>(R.id.dm3)
        val dm4 = findViewById<LinearLayout>(R.id.dm4)
        val dm5 = findViewById<LinearLayout>(R.id.dm5)
        val dm6 = findViewById<LinearLayout>(R.id.dm6)
        val backBtn = findViewById<ImageView>(R.id.backBtn)

        backBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        dm1.setOnClickListener {
            val intent = Intent(this, DMhammad::class.java)
            startActivity(intent)
        }

        dm2.setOnClickListener {
            val intent = Intent(this, DMabdullah::class.java)
            startActivity(intent)
        }

        dm3.setOnClickListener {
            val intent = Intent(this, DMzohaib::class.java)
            startActivity(intent)
        }

        dm4.setOnClickListener {
            val intent = Intent(this, DMsauleh::class.java)
            startActivity(intent)
        }

        dm5.setOnClickListener {
            val intent = Intent(this, DMfaizan::class.java)
            startActivity(intent)
        }

        dm6.setOnClickListener {
            val intent = Intent(this, DMumair::class.java)
            startActivity(intent)
        }

    }
}