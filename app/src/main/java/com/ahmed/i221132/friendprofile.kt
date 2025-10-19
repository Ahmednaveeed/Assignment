package com.ahmed.i221132

import Highlight
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class friendprofile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var targetUserId: String
    private lateinit var currentUserId: String

    // UI Elements
    private lateinit var followBtn: Button
    private lateinit var profilePicture: CircleImageView
    private lateinit var usernameText: TextView
    private lateinit var bioNameText: TextView
    private lateinit var bioQuoteText: TextView
    private lateinit var websiteText: TextView
    private lateinit var postsCountText: TextView
    private lateinit var followersCountText: TextView
    private lateinit var followingCountText: TextView
    private lateinit var highlightsRecyclerView: RecyclerView
    private lateinit var postsRecyclerView: RecyclerView // Maps to R.id.postsGrid
    private lateinit var messageBtn: Button
    private lateinit var emailBtn: Button

    // Adapters & Data
    private lateinit var profilePostAdapter: ProfilePostAdapter
    private lateinit var highlightAdapter: HighlightAdapter // 🚀 Declared HighlightAdapter
    private val postsList = mutableListOf<Post>()
    private val highlightsList = mutableListOf<Highlight>() // 🚀 Highlight data list


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friendprofile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        currentUserId = auth.currentUser?.uid ?: return finish()

        // 1. Retrieve Target User ID (This is the key step from the search activity)
        targetUserId = intent.getStringExtra("TARGET_USER_UID") ?: run {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show()
            return finish()
        }

        // IMPORTANT: Prevent viewing own profile in this "friend" activity
        if (targetUserId == currentUserId) {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
            return
        }

        // 2. Initialize UI Components
        initViews()

        // 3. Setup RecyclerViews
        setupRecyclerViews()

        // 4. Load Data and Follow Status
        loadTargetUserProfile()
        loadHighlights() // 🚀 Load highlights dynamically
        loadPosts()
        loadFollowStatus()

        // 5. Setup Listeners
        setupListeners()
    }

    // --- Initialization and Setup ---

    private fun initViews() {
        // Find all UI elements based on the IDs in friendprofile.xml
        followBtn = findViewById(R.id.followBtn)
        profilePicture = findViewById(R.id.profile_picture)
        usernameText = findViewById(R.id.username)
        bioNameText = findViewById(R.id.bio_name)
        bioQuoteText = findViewById(R.id.bio_quote)
        websiteText = findViewById(R.id.website_text)

        postsCountText = findViewById(R.id.posts_count)
        followersCountText = findViewById(R.id.followers_count)
        followingCountText = findViewById(R.id.following_count)

        messageBtn = findViewById(R.id.messageBtn)
        emailBtn = findViewById(R.id.emailBtn)

        highlightsRecyclerView = findViewById(R.id.highlights_recycler_view)
        postsRecyclerView = findViewById(R.id.postsGrid)
    }

    private fun setupRecyclerViews() {
        // Highlights (Horizontal Scroll)
        highlightAdapter = HighlightAdapter(highlightsList, this) // 🚀 Use the dynamic list
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        highlightsRecyclerView.adapter = highlightAdapter

        // Posts Grid (3-column layout manager)
        profilePostAdapter = ProfilePostAdapter(postsList, this)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        postsRecyclerView.adapter = profilePostAdapter
    }

    private fun setupListeners() {
        // Follow Button Logic
        followBtn.setOnClickListener { handleFollowButtonClick() }

        // Navigation (Your existing logic)
        findViewById<ImageView>(R.id.back_button).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.home_image).setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)) }
        findViewById<ImageView>(R.id.search_image).setOnClickListener { startActivity(Intent(this, search::class.java)) }
        findViewById<ImageView>(R.id.heart_image).setOnClickListener { startActivity(Intent(this, heart_following::class.java)) }
        findViewById<CircleImageView>(R.id.profile_image).setOnClickListener { startActivity(Intent(this, Profile::class.java)) }

        // Message Button (Opens Chat)
        messageBtn.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("TARGET_USER_UID", targetUserId)
            startActivity(intent)
        }
    }

    // --- Data Loading Functions (Fetches all necessary data from Firebase) ---

    private fun loadTargetUserProfile() {
        database.getReference("users").child(targetUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)

                if (user != null) {
                    // Update UI with Firebase data (Name, Bio, Website, Username)
                    usernameText.text = user.username
                    bioNameText.text = user.name
                    bioQuoteText.text = user.bio
                    websiteText.text = user.website

                    // Display Counts (Based on the size of the 'followers' and 'following' maps)
                    postsCountText.text = snapshot.child("posts").childrenCount.toString()
                    followersCountText.text = snapshot.child("followers").childrenCount.toString()
                    followingCountText.text = snapshot.child("following").childrenCount.toString()

                    // Load Profile Picture from Base64
                    user.profileImageBase64?.let { base64 ->
                        try {
                            val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            profilePicture.setImageBitmap(decodedImage)
                        } catch (e: Exception) {
                            profilePicture.setImageResource(R.drawable.user)
                        }
                    } ?: profilePicture.setImageResource(R.drawable.user)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@friendprofile, "Failed to load user: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 🚀 NEW FUNCTION: Load Highlights dynamically
    private fun loadHighlights() {
        // ASSUMPTION: Highlights are stored under a 'highlights' node keyed by user ID.
        // Example: /highlights/{targetUserId}/[list of Highlight objects]

        database.getReference("highlights").child(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    highlightsList.clear()

                    for (highlightSnapshot in snapshot.children) {
                        // NOTE: You must define a Highlight data class matching your Firebase structure
                        val highlight = highlightSnapshot.getValue(Highlight::class.java)
                        if (highlight != null) {
                            highlightsList.add(highlight)
                        }
                    }

                    highlightAdapter.notifyDataSetChanged()

                    // Hide the RecyclerView if no highlights are found
                    highlightsRecyclerView.visibility = if (highlightsList.isEmpty()) View.GONE else View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {
                    // Log error or show Toast
                }
            })
    }


    private fun loadPosts() {
        // Fetch posts belonging to the target user
        database.getReference("posts")
            .orderByChild("userId")
            .equalTo(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postsList.clear()
                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)?.copy(postId = postSnapshot.key ?: "")
                        if (post != null) {
                            postsList.add(post)
                        }
                    }
                    profilePostAdapter.notifyDataSetChanged()
                    postsCountText.text = postsList.size.toString()

                    // Hide the RecyclerView if no posts are found
                    postsRecyclerView.visibility = if (postsList.isEmpty()) View.GONE else View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@friendprofile, "Failed to load posts.", Toast.LENGTH_SHORT).show()
                    postsRecyclerView.visibility = View.GONE
                }
            })
    }

    // --- Follow System Logic (Sends Request / Unfollows) ---

    private fun loadFollowStatus() {
        val followingRef = database.getReference("following").child(currentUserId).child(targetUserId)
        val requestsRef = database.getReference("followRequests").child(targetUserId).child(currentUserId)

        requestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    updateFollowButton("Requested")
                    return
                }

                followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            updateFollowButton("Following")
                        } else {
                            updateFollowButton("Follow")
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateFollowButton(text: String) {
        followBtn.text = text

        when (text) {
            "Following" -> {
                followBtn.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                followBtn.setTextColor(Color.BLACK)
                messageBtn.visibility = View.VISIBLE
                emailBtn.visibility = View.VISIBLE
            }
            "Requested" -> {
                followBtn.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                followBtn.setTextColor(Color.BLACK)
                messageBtn.visibility = View.GONE
                emailBtn.visibility = View.GONE
            }
            "Follow" -> {
                followBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#8B4513"))
                followBtn.setTextColor(Color.WHITE)
                messageBtn.visibility = View.GONE
                emailBtn.visibility = View.GONE
            }
        }
    }

    private fun handleFollowButtonClick() {
        when (followBtn.text.toString()) {
            "Follow" -> sendFollowRequest()
            "Requested" -> cancelFollowRequest()
            "Following" -> unfollowUser()
        }
    }

    private fun sendFollowRequest() {
        database.getReference("followRequests")
            .child(targetUserId)
            .child(currentUserId)
            .setValue(true)
            .addOnSuccessListener {
                updateFollowButton("Requested")
                Toast.makeText(this, "Follow request sent.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send request.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun unfollowUser() {
        // 1. Remove from current user's 'following'
        database.getReference("following")
            .child(currentUserId)
            .child(targetUserId)
            .removeValue()

        // 2. Remove from target user's 'followers'
        database.getReference("followers")
            .child(targetUserId)
            .child(currentUserId)
            .removeValue()
            .addOnSuccessListener {
                updateFollowButton("Follow")
                Toast.makeText(this, "Unfollowed.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to unfollow.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cancelFollowRequest() {
        database.getReference("followRequests")
            .child(targetUserId)
            .child(currentUserId)
            .removeValue()
            .addOnSuccessListener {
                updateFollowButton("Follow")
                Toast.makeText(this, "Request cancelled.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to cancel request.", Toast.LENGTH_SHORT).show()
            }
    }
}
