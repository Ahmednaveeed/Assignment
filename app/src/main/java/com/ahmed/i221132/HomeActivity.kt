package com.ahmed.i221132

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.messaging.FirebaseMessaging
import coil.load
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.Dispatchers // For thread control
import kotlinx.coroutines.GlobalScope // For starting coroutine
import kotlinx.coroutines.launch // For starting coroutine
import kotlinx.coroutines.withContext // For switching back to main thread
// NOTE: Ensure PostAdapter and StoryAdapter are available in your project.
// NOTE: Ensure Story data class is available (similar structure to Post, but for stories).

private lateinit var pickImageLauncher: ActivityResultLauncher<String>

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var addPostLauncher: ActivityResultLauncher<Intent>

    // UIDs of users the current user is FOLLOWING (plus the current user itself)
    private val followingUids = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupLaunchers()
        setupNavigation()
        setupUserPresence()
        saveFCMToken()
        // Load dynamic data from Firebase
        loadUserData()
        // 🚀 CHANGE: Load following list first, then load the feed
        loadFollowingAndFeed()
        listenForIncomingCalls()
    }

    /**
     * 🚀 FIX FOR ANR: Moves Base64 decoding off the main (UI) thread using Coroutines.
     */
    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        val profileImageView = findViewById<CircleImageView>(R.id.profile_image)

        database.getReference("users").child(uid).get()
            .addOnSuccessListener { dataSnapshot ->
                val profileImageBase64 = dataSnapshot.child("profileImageBase64").value as? String

                if (!profileImageBase64.isNullOrEmpty()) {
                    // Start Coroutine on the I/O thread for heavy decoding work
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                            // Switch back to the Main thread to update the UI
                            withContext(Dispatchers.Main) {
                                profileImageView.setImageBitmap(decodedImage)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                profileImageView.setImageResource(R.drawable.user)
                            }
                        }
                    }
                } else {
                    profileImageView.setImageResource(R.drawable.user)
                }
            }
    }





    // 🚀 NEW FUNCTION: Fetches the list of followed users, then calls loadFeed()
    private fun loadFollowingAndFeed() {
        val currentUserUid = auth.currentUser?.uid ?: return
        followingUids.add(currentUserUid) // Always include self

        // 1. Fetch all UIDs the current user is following
        database.getReference("following").child(currentUserUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        // The key of the child node is the UID of the followed user
                        childSnapshot.key?.let { followedUid ->
                            followingUids.add(followedUid)
                        }
                    }
                    // 2. Once following list is ready, load the feed content
                    loadFeedContent()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HomeActivity, "Failed to load following list: ${error.message}", Toast.LENGTH_SHORT).show()
                    loadFeedContent() // Load content anyway, but feed will be empty/only self posts
                }
            })
    }

    private fun listenForIncomingCalls() {
        val currentUserUid = auth.currentUser?.uid ?: return
        // Listen for data written to the current user's call node: /calls/{currentUid}
        database.getReference("calls").child(currentUserUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val callerId = snapshot.child("callerId").getValue(String::class.java)
                        val callType = snapshot.child("callType").getValue(String::class.java)
                        val channelName = snapshot.child("channelName").getValue(String::class.java)
                        val status = snapshot.child("status").getValue(String::class.java)

                        if (status == "ringing" && callerId != null && callType != null && channelName != null) {
                            // Launch the Call Activity to answer the call
                            val intent = Intent(this@HomeActivity, CallActivity::class.java)
                            intent.putExtra("CHANNEL_NAME", channelName)
                            intent.putExtra("CALL_TYPE", callType)
                            intent.putExtra("IS_CALLER", false) // Recipient is NOT the caller
                            intent.putExtra("CALLER_ID", callerId) // Pass caller's ID for potential UI/cleanup
                            startActivity(intent)

                            // IMPORTANT: Change the status to "answered" to prevent immediate re-triggering
                            snapshot.ref.child("status").setValue("answered")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun saveFCMToken() {
        val uid = auth.currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = task.result
            // Save token to /users/{uid}/fcmToken
            database.getReference("users").child(uid)
                .child("fcmToken").setValue(token)
        }
    }

    private fun loadFeedContent() {
        // --- Story Loading (Filtered) ---
        loadFilteredStories()

        // --- Post Loading (Filtered) ---
        loadFilteredPosts()
    }




    private fun setupUserPresence() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(uid)
        val lastOnlineRef = userRef.child("lastSeen")
        val isOnlineRef = userRef.child("isOnline")

        // 1. Set status to Offline/Timestamp when the user disconnects
        database.getReference(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false

                if (connected) {
                    // Remove the 'lastSeen' timestamp if they are online
                    lastOnlineRef.removeValue()

                    // Set status to TRUE when connected
                    isOnlineRef.setValue(true)

                    // IMPORTANT: When connection is lost, set isOnline to false and record timestamp
                    isOnlineRef.onDisconnect().setValue(false)
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP)
                } else {
                    // If the app gets here (e.g., manually called), they are offline
                    // The onDisconnect will handle this automatically if the connection drops.
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }





    private fun loadFilteredStories() {
        val storiesRecyclerView = findViewById<RecyclerView>(R.id.stories_recycler_view)
        storiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val storyList = mutableListOf<Story>()
        val storyAdapter = StoryAdapter(storyList, this) { userId ->
            val intent = Intent(this, StoryViewActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        storiesRecyclerView.adapter = storyAdapter

        val storiesRef = database.getReference("stories")
        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

        storiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                storyList.clear()

                // Temporary list to hold stories, we only fetch user data for UIDs in this list
                val usersWithFilteredStories = mutableSetOf<String>()

                for (userStorySnapshot in snapshot.children) {
                    val userId = userStorySnapshot.key

                    // 🚀 CRITICAL FILTER 1: Only check stories from users we follow (or self)
                    if (userId != null && followingUids.contains(userId)) {
                        var hasActiveStory = false

                        userStorySnapshot.children.forEach { story ->
                            val timestamp = story.child("timestamp").getValue(Long::class.java) ?: 0L
                            if (timestamp > twentyFourHoursAgo) {
                                hasActiveStory = true
                                return@forEach
                            }
                        }
                        if (hasActiveStory) {
                            usersWithFilteredStories.add(userId)
                        }
                    }
                }

                // Now fetch user data for the filtered list of UIDs
                usersWithFilteredStories.forEach { userId ->
                    database.getReference("users").child(userId).get().addOnSuccessListener { userSnapshot ->
                        val storyUser = userSnapshot.getValue(Story::class.java)
                        if (storyUser != null) {
                            // 🚀 Insert self-story at the beginning
                            if (userId == followingUids.first()) { // Assuming first added UID is current user
                                storyList.add(0, storyUser)
                            } else {
                                storyList.add(storyUser)
                            }
                            storyAdapter.notifyDataSetChanged()
                        }
                    }
                }
                // Handle case where only current user has a story
                if (storyList.isEmpty() && followingUids.contains(auth.currentUser?.uid)) {
                    // Optionally fetch current user's story again if none loaded
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load stories.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadFilteredPosts() {
        val postsRecyclerView = findViewById<RecyclerView>(R.id.posts_recycler_view)
        postsRecyclerView.layoutManager = LinearLayoutManager(this)

        val postList = mutableListOf<Post>()
        val postAdapter = PostAdapter(postList, this)
        postsRecyclerView.adapter = postAdapter

        val postsRef = database.getReference("posts")

        // Use a single ValueEventListener on the 'posts' root node and filter in-memory.
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()

                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)

                    if (post != null) {
                        // 🚀 CRITICAL FILTER 2: Check if the post author is in the following list
                        if (followingUids.contains(post.userId)) {
                            postList.add(post)
                        }
                    }
                }

                // Display posts newest first
                postList.sortByDescending { it.timestamp }
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load feed.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupLaunchers() {
        // ... (This function remains the same, no changes needed)
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val intent = Intent(this, addstory::class.java)
                intent.putExtra("storyImage", uri.toString())
                startActivity(intent)
            }
        }

        addPostLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    val intent = Intent(this, add_post::class.java)
                    intent.putExtra("postImage", uri.toString())
                    startActivity(intent)
                }
            }
        }
    }

    private fun setupNavigation() {
        // ... (This function remains the same, no changes needed)
        val search_image = findViewById<ImageView>(R.id.search_image)
        val message_button = findViewById<ImageView>(R.id.message_button)
        val add_post_button = findViewById<ImageView>(R.id.add_post)
        val camera_button = findViewById<ImageView>(R.id.camera_button)
        val profile_image = findViewById<CircleImageView>(R.id.profile_image)
        val heart_image = findViewById<ImageView>(R.id.heart_image)

        search_image.setOnClickListener { startActivity(Intent(this, search::class.java)) }
        message_button.setOnClickListener { startActivity(Intent(this, message::class.java)) }
        profile_image.setOnClickListener { startActivity(Intent(this, Profile::class.java)) }
        heart_image.setOnClickListener { startActivity(Intent(this, heart_following::class.java)) }
        camera_button.setOnClickListener { pickImageLauncher.launch("image/*") }

        add_post_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            addPostLauncher.launch(intent)
        }
    }
}



//package com.ahmed.i221132
//
//import android.content.Intent
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.os.Bundle
//import android.util.Base64
//import android.view.View
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//import de.hdodenhof.circleimageview.CircleImageView
//import coil.load
//import kotlinx.coroutines.Dispatchers // 🚀 NEW: For thread control
//import kotlinx.coroutines.GlobalScope // 🚀 NEW: For starting coroutine
//import kotlinx.coroutines.launch // 🚀 NEW: For starting coroutine
//import kotlinx.coroutines.withContext // 🚀 NEW: For switching back to main thread
//// NOTE: Ensure PostAdapter and StoryAdapter are available in your project.
//// NOTE: Ensure Story data class is available (similar structure to Post, but for stories).
//
//private lateinit var pickImageLauncher: ActivityResultLauncher<String>
//
//class HomeActivity : AppCompatActivity() {
//
//    private lateinit var auth: FirebaseAuth
//    private lateinit var database: FirebaseDatabase
//    private lateinit var addPostLauncher: ActivityResultLauncher<Intent>
//
//    // UIDs of users the current user is FOLLOWING (plus the current user itself)
//    private val followingUids = mutableSetOf<String>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_home)
//
//        auth = FirebaseAuth.getInstance()
//        database = FirebaseDatabase.getInstance()
//
//        setupLaunchers()
//        setupNavigation()
//
//        loadUserData()
//        // 🚀 CHANGE: Load following list first, then load the feed
//        loadFollowingAndFeed()
//    }
//
//    private fun loadUserData() {
//        val uid = auth.currentUser?.uid ?: return
//        val profileImageView = findViewById<CircleImageView>(R.id.profile_image)
//
//        database.getReference("users").child(uid).get()
//            .addOnSuccessListener { dataSnapshot ->
//                val profileImageBase64 = dataSnapshot.child("profileImageBase64").value as? String
//                if (!profileImageBase64.isNullOrEmpty()) {
//                    try {
//                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
//                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//                        profileImageView.setImageBitmap(decodedImage)
//                    } catch (e: Exception) {
//                        profileImageView.setImageResource(R.drawable.user)
//                    }
//                } else {
//                    profileImageView.setImageResource(R.drawable.user)
//                }
//            }
//    }
//
//    // 🚀 NEW FUNCTION: Fetches the list of followed users, then calls loadFeed()
//    private fun loadFollowingAndFeed() {
//        val currentUserUid = auth.currentUser?.uid ?: return
//        followingUids.add(currentUserUid) // Always include self
//
//        // 1. Fetch all UIDs the current user is following
//        database.getReference("following").child(currentUserUid)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    for (childSnapshot in snapshot.children) {
//                        // The key of the child node is the UID of the followed user
//                        childSnapshot.key?.let { followedUid ->
//                            followingUids.add(followedUid)
//                        }
//                    }
//                    // 2. Once following list is ready, load the feed content
//                    loadFeedContent()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(this@HomeActivity, "Failed to load following list: ${error.message}", Toast.LENGTH_SHORT).show()
//                    loadFeedContent() // Load content anyway, but feed will be empty/only self posts
//                }
//            })
//    }
//
//
//    private fun loadFeedContent() {
//        // --- Story Loading (Filtered) ---
//        loadFilteredStories()
//
//        // --- Post Loading (Filtered) ---
//        loadFilteredPosts()
//    }
//
//    private fun loadFilteredStories() {
//        val storiesRecyclerView = findViewById<RecyclerView>(R.id.stories_recycler_view)
//        storiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//
//        val storyList = mutableListOf<Story>()
//        val storyAdapter = StoryAdapter(storyList, this) { userId ->
//            val intent = Intent(this, StoryViewActivity::class.java)
//            intent.putExtra("USER_ID", userId)
//            startActivity(intent)
//        }
//        storiesRecyclerView.adapter = storyAdapter
//
//        val storiesRef = database.getReference("stories")
//        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
//
//        storiesRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                storyList.clear()
//
//                // Temporary list to hold stories, we only fetch user data for UIDs in this list
//                val usersWithFilteredStories = mutableSetOf<String>()
//
//                for (userStorySnapshot in snapshot.children) {
//                    val userId = userStorySnapshot.key
//
//                    // 🚀 CRITICAL FILTER 1: Only check stories from users we follow (or self)
//                    if (userId != null && followingUids.contains(userId)) {
//                        var hasActiveStory = false
//
//                        userStorySnapshot.children.forEach { story ->
//                            val timestamp = story.child("timestamp").getValue(Long::class.java) ?: 0L
//                            if (timestamp > twentyFourHoursAgo) {
//                                hasActiveStory = true
//                                return@forEach
//                            }
//                        }
//                        if (hasActiveStory) {
//                            usersWithFilteredStories.add(userId)
//                        }
//                    }
//                }
//
//                // Now fetch user data for the filtered list of UIDs
//                usersWithFilteredStories.forEach { userId ->
//                    database.getReference("users").child(userId).get().addOnSuccessListener { userSnapshot ->
//                        val storyUser = userSnapshot.getValue(Story::class.java)
//                        if (storyUser != null) {
//                            // 🚀 Insert self-story at the beginning
//                            if (userId == followingUids.first()) { // Assuming first added UID is current user
//                                storyList.add(0, storyUser)
//                            } else {
//                                storyList.add(storyUser)
//                            }
//                            storyAdapter.notifyDataSetChanged()
//                        }
//                    }
//                }
//                // Handle case where only current user has a story
//                if (storyList.isEmpty() && followingUids.contains(auth.currentUser?.uid)) {
//                    // Optionally fetch current user's story again if none loaded
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@HomeActivity, "Failed to load stories.", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun loadFilteredPosts() {
//        val postsRecyclerView = findViewById<RecyclerView>(R.id.posts_recycler_view)
//        postsRecyclerView.layoutManager = LinearLayoutManager(this)
//
//        val postList = mutableListOf<Post>()
//        val postAdapter = PostAdapter(postList, this)
//        postsRecyclerView.adapter = postAdapter
//
//        val postsRef = database.getReference("posts")
//
//        // Use a single ValueEventListener on the 'posts' root node and filter in-memory.
//        // Direct Firebase filtering using 'where' is not practical here as it only supports one 'equalTo' clause.
//        postsRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                postList.clear()
//
//                for (postSnapshot in snapshot.children) {
//                    val post = postSnapshot.getValue(Post::class.java)
//
//                    if (post != null) {
//                        // 🚀 CRITICAL FILTER 2: Check if the post author is in the following list
//                        if (followingUids.contains(post.userId)) {
//                            postList.add(post)
//                        }
//                    }
//                }
//
//                // Display posts newest first
//                postList.sortByDescending { it.timestamp }
//                postAdapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@HomeActivity, "Failed to load feed.", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun setupLaunchers() {
//        // ... (This function remains the same, no changes needed)
//        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//            if (uri != null) {
//                val intent = Intent(this, addstory::class.java)
//                intent.putExtra("storyImage", uri.toString())
//                startActivity(intent)
//            }
//        }
//
//        addPostLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_OK) {
//                val uri = result.data?.data
//                if (uri != null) {
//                    val intent = Intent(this, add_post::class.java)
//                    intent.putExtra("postImage", uri.toString())
//                    startActivity(intent)
//                }
//            }
//        }
//    }
//
//    private fun setupNavigation() {
//        // ... (This function remains the same, no changes needed)
//        val search_image = findViewById<ImageView>(R.id.search_image)
//        val message_button = findViewById<ImageView>(R.id.message_button)
//        val add_post_button = findViewById<ImageView>(R.id.add_post)
//        val camera_button = findViewById<ImageView>(R.id.camera_button)
//        val profile_image = findViewById<CircleImageView>(R.id.profile_image)
//        val heart_image = findViewById<ImageView>(R.id.heart_image)
//
//        search_image.setOnClickListener { startActivity(Intent(this, search::class.java)) }
//        message_button.setOnClickListener { startActivity(Intent(this, message::class.java)) }
//        profile_image.setOnClickListener { startActivity(Intent(this, Profile::class.java)) }
//        heart_image.setOnClickListener { startActivity(Intent(this, heart_following::class.java)) }
//        camera_button.setOnClickListener { pickImageLauncher.launch("image/*") }
//
//        add_post_button.setOnClickListener {
//            val intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "image/*"
//            addPostLauncher.launch(intent)
//        }
//    }
//}