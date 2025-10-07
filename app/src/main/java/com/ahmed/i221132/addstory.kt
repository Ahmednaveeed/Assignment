package com.ahmed.i221132

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class addstory : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addstory)

        val storyImage = findViewById<ImageView>(R.id.storyImage)
        val imageUri = intent.getStringExtra("storyImage")
        val closeBtn = findViewById<ImageView>(R.id.closeBtn)

        if (imageUri != null) {
            storyImage.setImageURI(Uri.parse(imageUri))
        }

        closeBtn.setOnClickListener {
            finish()
        }

    }
}