package com.ahmed.i221132

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Highlight1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_highlight1)

        val close_button = findViewById<ImageView>(R.id.close_button)

        close_button.setOnClickListener {
            finish()
        }

    }
}