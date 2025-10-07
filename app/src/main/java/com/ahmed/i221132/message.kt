package com.ahmed.i221132

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class message : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        // NEW: RecyclerView Setup for DMs
        val recyclerView = findViewById<RecyclerView>(R.id.dms_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val dms = listOf(
            DMMessage("hammad_yasin", "Have a nice day, bro!", ".now", R.drawable.hammad),
            DMMessage("abdullah_malik309", "I head this is a good movie, s...", ".now", R.drawable.abdullah),
            DMMessage("zohaib_shafqat", "See you on the next meeting!", ".15m", R.drawable.zohaib),
            DMMessage("saulehnaveed", "Sounds good😂😂😂", ".20m", R.drawable.sauleh),
            DMMessage("faizan_naveed", "The new design looks cool, b...", ".1m", R.drawable.faizan),
            DMMessage("umair.asghar", "Thank you, bro!", ".2h", R.drawable.umair)
            // more can be added
        )

        val adapter = DMAdapter(dms, this)
        recyclerView.adapter = adapter
        // END NEW

        val backBtn = findViewById<ImageView>(R.id.backBtn)

        backBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        val bottomCamera = findViewById<ImageView>(R.id.bottomCamera)

        bottomCamera.setOnClickListener {
            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(packageManager) != null) {
                startActivity(cameraIntent)
            }
        }
    }
}