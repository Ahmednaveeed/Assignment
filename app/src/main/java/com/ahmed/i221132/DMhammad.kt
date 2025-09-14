package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class DMhammad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dmhammad)

        val video_call_button = findViewById<ImageView>(R.id.video_call_button)
        val options_button = findViewById<ImageView>(R.id.options_button)
        val back_button = findViewById<ImageView>(R.id.back_button)

        video_call_button.setOnClickListener {
            val intent = Intent(this, Callhammad::class.java)
            startActivity(intent)
        }

        options_button.setOnClickListener {
            val intent = Intent(this, friendprofile::class.java)
            startActivity(intent)
        }

        back_button.setOnClickListener {
            val intent = Intent(this, message::class.java)
            finish()
        }
    }
}