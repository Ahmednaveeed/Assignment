package com.ahmed.i221132

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditProfile : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val cancel = findViewById<TextView>(R.id.cancel)
        val done = findViewById<TextView>(R.id.done)

        cancel.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            finish()
        }

        done.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            finish()
        }

    }
}