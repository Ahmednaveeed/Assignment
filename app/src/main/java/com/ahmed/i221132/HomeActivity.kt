package com.ahmed.i221132

import coil.load
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

private lateinit var pickImageLauncher: ActivityResultLauncher<String>

class HomeActivity : AppCompatActivity() {

    // ---  FIREBASE INITIALIZATION ---
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize the ActivityResultLauncher only once
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                val intent = Intent(this, addstory::class.java)
                intent.putExtra("storyImage", uri.toString())
                startActivity(intent)
            }
        }

        // --- DYNAMIC CONTENT LOADING ---
        // 1. Load the user's profile data (e.g., set the bottom navigation profile image)
        loadUserData()

        // 2. Load the feed content (will be empty for new users)
        loadFeedContent()

        // --- VIEW SETUP ---
        val search_image = findViewById<ImageView>(R.id.search_image)
        val message_button = findViewById<ImageView>(R.id.message_button)
        val addPostButton = findViewById<ImageView>(R.id.add_post)
        val camera_button = findViewById<ImageView>(R.id.camera_button)
        val profile_image = findViewById<CircleImageView>(R.id.profile_image)
        val heart_image = findViewById<ImageView>(R.id.heart_image)

        // Posts RecyclerView Setup (Layout Manager only)
        val postsRecyclerView = findViewById<RecyclerView>(R.id.posts_recycler_view)
        postsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Stories RecyclerView Setup (Layout Manager only)
        val storiesRecyclerView = findViewById<RecyclerView>(R.id.stories_recycler_view)
        storiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        // --- NAVIGATION LISTENERS ---
        search_image.setOnClickListener {
            startActivity(Intent(this, search::class.java))
        }
        message_button.setOnClickListener {
            startActivity(Intent(this, message::class.java))
        }
        profile_image.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }
        heart_image.setOnClickListener {
            startActivity(Intent(this, heart_following::class.java))
        }
        camera_button.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        var launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            if(it.resultCode == RESULT_OK){
                val uri = it.data?.data

                // It creates an intent for your new screen, passes the image URI,
                // and starts the activity.
                if (uri != null) {
                    val intent = Intent(this, add_post::class.java)
                    intent.putExtra("postImage", uri.toString())
                    startActivity(intent)
                }
            }
        }


        addPostButton.setOnClickListener {
            // This part correctly opens the gallery to select an image
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            launcher.launch(intent)
        }
    }


    // --- LOAD USER PROFILE DATA ---
    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        val profileImageView = findViewById<CircleImageView>(R.id.profile_image)

        // Fetch the user's data record from the Realtime Database
        database.getReference("users").child(uid).get()
            .addOnSuccessListener { dataSnapshot ->
                // Fetch the Profile Image URL saved during registration
                val profileImageUrl = dataSnapshot.child("profileImageUrl").value as? String

                // If a valid URL is found, load it into the bottom navigation profile picture
                if (!profileImageUrl.isNullOrEmpty() && profileImageUrl != "https://default-image-url.com/profile.png") {
                    // 🔑 If using Coil or Glide:
                    profileImageView.load(profileImageUrl)

                    // If not using a library, the image will remain the static default
                    // (R.drawable.ahmed) until you implement image loading.
                } else {
                    // Optionally set a local default image if the URL is the placeholder
                    profileImageView.setImageResource(R.drawable.user)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile data.", Toast.LENGTH_SHORT).show()
            }
    }


    // --- LOAD STORIES AND FEED CONTENT ---
    private fun loadFeedContent() {
        // --- 1. Stories Logic (Initial Empty State) ---

        // For a new user, the story section should only show 'Your Story'.
        val initialStories = listOf(
            // Position 0 is always the current user's story
            Story("Your story", R.drawable.ahmed)
        )

        val storiesRecyclerView = findViewById<RecyclerView>(R.id.stories_recycler_view)
        val storyAdapter = StoryAdapter(initialStories, this) { position ->

            val intent = when (position) {
                0 -> Intent(this, yourstory::class.java)
                1 -> Intent(this, story1::class.java)
                2 -> Intent(this, story2::class.java)
                3 -> Intent(this, story3::class.java)
                4 -> Intent(this, story4::class.java)
                else -> return@StoryAdapter
            }
            startActivity(intent)
        }
        storiesRecyclerView.adapter = storyAdapter


        // --- 2. Posts Feed Logic (Initial Empty State) ---

        // For a new user who hasn't followed anyone, the posts list MUST be empty.
        val emptyPosts = emptyList<Post>()

        val postsRecyclerView = findViewById<RecyclerView>(R.id.posts_recycler_view)
        postsRecyclerView.adapter = PostAdapter(emptyPosts, this)

        // Display a message if the feed is empty
        if (emptyPosts.isEmpty()) {
            Toast.makeText(this, "Welcome! Follow users or add your first post to see activity.", Toast.LENGTH_LONG).show()
        }

        // NOTE: Later, this function will contain complex Firebase logic to query
        // posts from users listed in the current user's '/following/' node.
    }
}