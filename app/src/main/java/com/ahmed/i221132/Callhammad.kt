package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Callhammad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_callhammad)

        val hangup_button = findViewById<ImageView>(R.id.hangup_button)

        hangup_button.setOnClickListener {
            val intent = Intent(this, DMhammad::class.java)
            startActivity(intent)
        }
    }
}