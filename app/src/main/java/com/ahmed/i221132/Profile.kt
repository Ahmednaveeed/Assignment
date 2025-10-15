package com.ahmed.i221132

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var postAdapter: ProfilePostAdapter
    private val postList = mutableListOf<ProfilePost>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // --- Initialization ---
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // --- Setup RecyclerViews ---
        setupHighlights()
        val postsRecyclerView = findViewById<RecyclerView>(R.id.profile_posts_recycler_view)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        postAdapter = ProfilePostAdapter(postList, this)
        postsRecyclerView.adapter = postAdapter

        // --- Load Data and Setup Navigation ---
        loadProfileData()
        setupNavigation()
    }

    private fun loadProfileData() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            startActivity(Intent(this, login::class.java))
            finish()
            return
        }

        val userRef = database.getReference("users").child(uid)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                // Find all UI elements
                val mainProfilePicture = findViewById<CircleImageView>(R.id.profile_picture)
                val navProfilePicture = findViewById<CircleImageView>(R.id.profile_image)
                val username = findViewById<TextView>(R.id.username)
                val followersCount = findViewById<TextView>(R.id.followers_count)
                val followingCount = findViewById<TextView>(R.id.following_count)
                val bioName = findViewById<TextView>(R.id.bio_name)
                val bioText = findViewById<TextView>(R.id.bio_quote) // Your new single bio TextView

                // Extract data from Firebase
                val profileImageBase64 = dataSnapshot.child("profileImageBase64").getValue(String::class.java)
                val usernameStr = dataSnapshot.child("username").getValue(String::class.java)
                val nameStr = dataSnapshot.child("name").getValue(String::class.java)
                val bio = dataSnapshot.child("bio").getValue(String::class.java)
                val followers = dataSnapshot.child("followers").getValue(Long::class.java) ?: 0L
                val following = dataSnapshot.child("following").getValue(Long::class.java) ?: 0L

                // Update UI with fetched data
                username.text = usernameStr
                bioName.text = nameStr
                bioText.text = bio // Set the bio
                followersCount.text = followers.toString()
                followingCount.text = following.toString()

                // Decode and set the profile picture on both views
                if (!profileImageBase64.isNullOrEmpty()) {
                    try {
                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        mainProfilePicture.setImageBitmap(decodedImage)
                        navProfilePicture.setImageBitmap(decodedImage)
                    } catch (e: Exception) {
                        mainProfilePicture.setImageResource(R.drawable.user)
                        navProfilePicture.setImageResource(R.drawable.user)
                    }
                } else {
                    mainProfilePicture.setImageResource(R.drawable.user)
                    navProfilePicture.setImageResource(R.drawable.user)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show()
        }

        // Fetch this user's posts separately
        loadUserPosts(uid)
    }

    private fun loadUserPosts(uid: String) {
        val postsCountTextView = findViewById<TextView>(R.id.posts_count)
        val postsRef = database.getReference("posts")

        postsRef.orderByChild("userId").equalTo(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(ProfilePost::class.java)
                    if (post != null) {
                        postList.add(post)
                    }
                }
                postList.reverse() // Show newest first
                postAdapter.notifyDataSetChanged()

                // Update the post count with the actual number of posts found
                postsCountTextView.text = postList.size.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Profile, "Failed to load posts.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupHighlights() {
        val uid = auth.currentUser?.uid ?: return
        val highlightsRecyclerView = findViewById<RecyclerView>(R.id.profile_highlights_recycler_view)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val highlightList = mutableListOf<ProfileHighlight>()
        val highlightAdapter = ProfileHighlightAdapter(highlightList, this, uid)
        highlightsRecyclerView.adapter = highlightAdapter

        val highlightsRef = database.getReference("highlights").child(uid)
        highlightsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                highlightList.clear()
                highlightList.add(ProfileHighlight(title = "New", isNewButton = true))
                for (highlightSnapshot in snapshot.children) {
                    val highlight = highlightSnapshot.getValue(ProfileHighlight::class.java)
                    if (highlight != null) {
                        highlightList.add(highlight)
                    }
                }
                highlightAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupNavigation() {
        val home_image = findViewById<ImageView>(R.id.home_image)
        val search_image = findViewById<ImageView>(R.id.search_image)
        val add_post = findViewById<ImageView>(R.id.add_post)
        val heart_image = findViewById<ImageView>(R.id.heart_image)
        val editProfileBtn = findViewById<Button>(R.id.editProfileBtn)

        home_image.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)) }
        search_image.setOnClickListener { startActivity(Intent(this, search::class.java)) }
        // The add_post button should launch the gallery picker, similar to HomeActivity
        // add_post.setOnClickListener { ... }
        heart_image.setOnClickListener { startActivity(Intent(this, heart_following::class.java)) }
        editProfileBtn.setOnClickListener { startActivity(Intent(this, EditProfile::class.java)) }
    }
}