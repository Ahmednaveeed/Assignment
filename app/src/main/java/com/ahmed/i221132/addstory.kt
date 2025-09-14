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

        // Buttons (you can add Toasts for demo)
        findViewById<Button>(R.id.closeFriendsBtn)?.setOnClickListener {
            Toast.makeText(this, "Shared with close friends (demo)", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.yourStoryBtn)?.setOnClickListener {
            Toast.makeText(this, "Added to your story (demo)", Toast.LENGTH_SHORT).show()
        }

        closeBtn.setOnClickListener {
            finish()
        }

    }
}