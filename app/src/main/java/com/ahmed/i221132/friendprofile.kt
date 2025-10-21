package com.ahmed.i221132

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
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

// Assuming these data classes exist in separate files:
// data class ProfilePost(val postId: String = "", val userId: String = "", val imageBase64: String = "")
// data class ProfileHighlight(val highlightId: String = "", val title: String = "", val coverImageBase64: String = "", val isNewButton: Boolean = false)

class friendprofile : AppCompatActivity() {

    // --- Firebase & User IDs ---
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var targetUserId: String
    private lateinit var currentUserId: String

    // --- UI Elements ---
    private lateinit var followBtn: Button
    private lateinit var profilePicture: CircleImageView
    private lateinit var usernameText: TextView
    private lateinit var bioNameText: TextView
    private lateinit var bioQuoteText: TextView
    private lateinit var postsCountText: TextView
    private lateinit var followersCountText: TextView
    private lateinit var followingCountText: TextView
    private lateinit var messageBtn: Button
    private lateinit var emailBtn: Button
    private lateinit var contentRestrictionMessage: TextView
    private lateinit var navProfileImage: CircleImageView
    private lateinit var highlightsRecyclerView: RecyclerView
    private lateinit var postsRecyclerView: RecyclerView

    // --- Adapters & Data ---
    private lateinit var profilePostAdapter: ProfilePostAdapter
    private lateinit var highlightAdapter: ProfileHighlightAdapter
    private val postsList = mutableListOf<Post>()
    private val highlightsList = mutableListOf<ProfileHighlight>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friendprofile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        currentUserId = auth.currentUser?.uid ?: return finishAffinity() // Exit if no user

        // Get the target user ID from the intent
        targetUserId = intent.getStringExtra("TARGET_USER_UID") ?: run {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Prevent viewing own profile in this activity
        if (targetUserId == currentUserId) {
            startActivity(Intent(this, Profile::class.java))
            finish()
            return
        }

        // Initialize all UI elements
        initViews()

        // Setup RecyclerViews with adapters
        setupRecyclerViews()

        // Setup click listeners for buttons and navigation
        setupListeners()

        // Load all dynamic data from Firebase
        loadTargetUserProfile()
        loadHighlights()
        loadPosts()
        loadFollowStatus()
        loadCurrentUserNavProfilePic()
    }

    // --- Initialization and Setup Functions ---

    private fun initViews() {
        followBtn = findViewById(R.id.followBtn)
        profilePicture = findViewById(R.id.profile_picture)
        usernameText = findViewById(R.id.username)
        bioNameText = findViewById(R.id.bio_name)
        bioQuoteText = findViewById(R.id.bio_quote) // Using bio_quote for bio display
        postsCountText = findViewById(R.id.posts_count)
        followersCountText = findViewById(R.id.followers_count)
        followingCountText = findViewById(R.id.following_count)
        messageBtn = findViewById(R.id.messageBtn)
        emailBtn = findViewById(R.id.emailBtn)
        highlightsRecyclerView = findViewById(R.id.highlights_recycler_view)
        postsRecyclerView = findViewById(R.id.postsGrid)
        contentRestrictionMessage = findViewById(R.id.content_restriction_message)
        navProfileImage = findViewById(R.id.profile_image) // Bottom nav profile image
    }

    private fun setupRecyclerViews() {
        // Highlights RecyclerView
        highlightAdapter = ProfileHighlightAdapter(highlightsList, this, targetUserId)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        highlightsRecyclerView.adapter = highlightAdapter

        // Posts Grid RecyclerView
        profilePostAdapter = ProfilePostAdapter(postsList, this)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        postsRecyclerView.adapter = profilePostAdapter
    }

    private fun setupListeners() {
        followBtn.setOnClickListener { handleFollowButtonClick() }
        followersCountText.setOnClickListener { launchFollowListActivity("Followers") }
        followingCountText.setOnClickListener { launchFollowListActivity("Following") }
        findViewById<ImageView>(R.id.back_button).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.home_image).setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)) }
        findViewById<ImageView>(R.id.search_image).setOnClickListener { startActivity(Intent(this, search::class.java)) }
        findViewById<ImageView>(R.id.heart_image).setOnClickListener { startActivity(Intent(this, heart_following::class.java)) }
        // Bottom nav profile image always goes to the current user's profile
        navProfileImage.setOnClickListener { startActivity(Intent(this, Profile::class.java)) }

        messageBtn.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("USER_ID", targetUserId)
            intent.putExtra("USER_NAME", usernameText.text.toString())
            startActivity(intent)
        }
        // Add listener for email button if desired
        // emailBtn.setOnClickListener { /* ... */ }
    }

    // --- Data Loading Functions ---

    private fun loadCurrentUserNavProfilePic() {
        database.getReference("users").child(currentUserId).get().addOnSuccessListener { dataSnapshot ->
            val profileImageBase64 = dataSnapshot.child("profileImageBase64").getValue(String::class.java)
            if (!profileImageBase64.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                    navProfileImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                } catch (e: Exception) { navProfileImage.setImageResource(R.drawable.user) }
            } else { navProfileImage.setImageResource(R.drawable.user) }
        }.addOnFailureListener { navProfileImage.setImageResource(R.drawable.user) }
    }

    private fun loadTargetUserProfile() {
        database.getReference("users").child(targetUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@friendprofile, "User not found.", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
                val username = snapshot.child("username").getValue(String::class.java) ?: "N/A"
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val bio = snapshot.child("bio").getValue(String::class.java) ?: ""
                val profileImageBase64 = snapshot.child("profileImageBase64").getValue(String::class.java) ?: ""
                val followersCountNum = snapshot.child("followers").getValue(Long::class.java) ?: 0L // Read count directly
                val followingCountNum = snapshot.child("following").getValue(Long::class.java) ?: 0L // Read count directly

                usernameText.text = username
                bioNameText.text = name
                bioQuoteText.text = bio
                followersCountText.text = followersCountNum.toString()
                followingCountText.text = followingCountNum.toString()

                if (profileImageBase64.isNotEmpty()) {
                    try {
                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                        profilePicture.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                    } catch (e: Exception) { profilePicture.setImageResource(R.drawable.user) }
                } else { profilePicture.setImageResource(R.drawable.user) }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@friendprofile, "Failed to load user: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadHighlights() {
        val highlightsRef = database.getReference("highlights").child(targetUserId)
        highlightsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                highlightsList.clear()
                for (highlightSnapshot in snapshot.children) {
                    val highlight = highlightSnapshot.getValue(ProfileHighlight::class.java)
                    if (highlight != null) {
                        highlightsList.add(highlight)
                    }
                }
                highlightAdapter.notifyDataSetChanged()
                // Visibility handled by updateFollowButton
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@friendprofile, "Failed to load highlights.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPosts() {
        database.getReference("posts")
            .orderByChild("userId")
            .equalTo(targetUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postsList.clear()
                    for (postSnapshot in snapshot.children) {
                        // 🔑 FIXED: Get the data as 'Post'
                        val post = postSnapshot.getValue(Post::class.java)
                        if (post != null) {
                            postsList.add(post)
                        }
                    }
                    postsList.reverse() // Show newest first
                    profilePostAdapter.notifyDataSetChanged()
                    postsCountText.text = postsList.size.toString()
                    // Visibility handled by updateFollowButton
                }
                override fun onCancelled(error: DatabaseError) { /* ... */ }
            })
    }

    // --- Follow System Logic ---

    private fun loadFollowStatus() {
        val followingRef = database.getReference("following").child(currentUserId).child(targetUserId)
        val requestsRef = database.getReference("followRequests").child(targetUserId).child(currentUserId)

        requestsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(requestSnapshot: DataSnapshot) {
                if (requestSnapshot.exists()) {
                    updateFollowButton("Requested")
                } else {
                    followingRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(followingSnapshot: DataSnapshot) {
                            if (followingSnapshot.exists()) {
                                updateFollowButton("Following")
                            } else {
                                updateFollowButton("Follow")
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateFollowButton(status: String) {
        followBtn.text = status
        val isFollowing = status == "Following"
        val contentVisibility = if (isFollowing) View.VISIBLE else View.GONE
        val restrictionVisibility = if (isFollowing) View.GONE else View.VISIBLE

        postsRecyclerView.visibility = contentVisibility
        highlightsRecyclerView.visibility = contentVisibility
        contentRestrictionMessage.visibility = restrictionVisibility

        when (status) {
            "Following" -> {
                followBtn.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                followBtn.setTextColor(Color.BLACK)
                messageBtn.visibility = View.VISIBLE
                emailBtn.visibility = View.VISIBLE // Or GONE if not implemented
            }
            "Requested" -> {
                followBtn.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                followBtn.setTextColor(Color.BLACK)
                messageBtn.visibility = View.GONE
                emailBtn.visibility = View.GONE
            }
            "Follow" -> {
                followBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#8B4513")) // Brown
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
        database.getReference("followRequests").child(targetUserId).child(currentUserId).setValue(true)
            .addOnSuccessListener {
                updateFollowButton("Requested")
                Toast.makeText(this, "Follow request sent.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { Toast.makeText(this, "Failed to send request.", Toast.LENGTH_SHORT).show() }
    }

    private fun unfollowUser() {
        val updates = mapOf(
            "/following/$currentUserId/$targetUserId" to null,
            "/followers/$targetUserId/$currentUserId" to null
        )
        database.reference.updateChildren(updates).addOnSuccessListener {
            // Decrement counters
            database.getReference("users/$currentUserId/following").setValue(ServerValue.increment(-1))
            database.getReference("users/$targetUserId/followers").setValue(ServerValue.increment(-1))
                .addOnSuccessListener {
                    updateFollowButton("Follow")
                    Toast.makeText(this, "Unfollowed.", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { Toast.makeText(this, "Failed to unfollow.", Toast.LENGTH_SHORT).show() }
    }

    private fun cancelFollowRequest() {
        database.getReference("followRequests").child(targetUserId).child(currentUserId).removeValue()
            .addOnSuccessListener {
                updateFollowButton("Follow")
                Toast.makeText(this, "Request cancelled.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { Toast.makeText(this, "Failed to cancel request.", Toast.LENGTH_SHORT).show() }
    }

    private fun launchFollowListActivity(listType: String) {
        Toast.makeText(this, "$listType list not implemented yet", Toast.LENGTH_SHORT).show()
    }
}