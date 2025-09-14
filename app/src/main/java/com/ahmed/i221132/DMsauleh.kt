package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class DMsauleh : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dmsauleh)

        val video_call_button = findViewById<ImageView>(R.id.video_call_button)

        video_call_button.setOnClickListener {
            val intent = Intent(this, Callsauleh::class.java)
            startActivity(intent)
        }

        val back_button = findViewById<ImageView>(R.id.back_button)
        back_button.setOnClickListener {
            val intent = Intent(this, message::class.java)
            finish()
        }
    }
}