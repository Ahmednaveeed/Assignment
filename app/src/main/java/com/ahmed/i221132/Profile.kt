package com.ahmed.i221132

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class Profile : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // NEW: Highlights RecyclerView Setup
        val highlightsRecyclerView = findViewById<RecyclerView>(R.id.profile_highlights_recycler_view)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val profileHighlights = listOf(
            ProfileHighlight("New", 0, isNewButton = true),  // "New" button
            ProfileHighlight("Cars", R.drawable.cars, isNewButton = false),
            ProfileHighlight("Sky", R.drawable.sky, isNewButton = false),
            ProfileHighlight("Trips", R.drawable.trips, isNewButton = false)
        )

        val highlightAdapter = ProfileHighlightAdapter(profileHighlights, this) { position ->
            // Handle highlight clicks based on position
            when (position) {
                0 -> {
                    // "New" button clicked - you can add functionality here
                    Toast.makeText(this, "Add new highlight", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    val intent = Intent(this, Highlight1::class.java)
                    startActivity(intent)
                }
                2 -> {
                    val intent = Intent(this, Highlight2::class.java)
                    startActivity(intent)
                }
                3 -> {
                    val intent = Intent(this, Highlight3::class.java)
                    startActivity(intent)
                }
            }
        }
        highlightsRecyclerView.adapter = highlightAdapter
        // END NEW

        // Posts RecyclerView Setup
        val recyclerView = findViewById<RecyclerView>(R.id.profile_posts_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 3)  // 3 columns for grid

        val profilePosts = listOf(
            ProfilePost(R.drawable.post1),
            ProfilePost(R.drawable.post4),
            ProfilePost(R.drawable.post6),
            ProfilePost(R.drawable.post3),
            ProfilePost(R.drawable.post7),
            ProfilePost(R.drawable.post9),
            ProfilePost(R.drawable.post2),
            ProfilePost(R.drawable.post5),
            ProfilePost(R.drawable.post8)
        )

        val adapter = ProfilePostAdapter(profilePosts)
        recyclerView.adapter = adapter

        val home_image = findViewById<ImageView>(R.id.home_image)
        val search_image = findViewById<ImageView>(R.id.search_image)
        val heart_image = findViewById<ImageView>(R.id.heart_image)
        val profile_image = findViewById<CircleImageView>(R.id.profile_image)
        val editProfileBtn = findViewById<Button>(R.id.editProfileBtn)

        home_image.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        search_image.setOnClickListener {
            val intent = Intent(this, search::class.java)
            startActivity(intent)
        }
        heart_image.setOnClickListener {
            val intent = Intent(this, heart_following::class.java)
            startActivity(intent)
        }
        profile_image.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        editProfileBtn.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }
    }
}