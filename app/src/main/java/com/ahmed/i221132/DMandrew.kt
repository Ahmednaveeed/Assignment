package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DMandrew : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dmandrew)

        val video_call_button = findViewById<ImageView>(R.id.video_call_button)

        video_call_button.setOnClickListener {
            val intent = Intent(this, Callandrew::class.java)
            startActivity(intent)
        }

        val back_button = findViewById<ImageView>(R.id.back_button)
        back_button.setOnClickListener {
            val intent = Intent(this, message::class.java)
            startActivity(intent)
        }
    }
}