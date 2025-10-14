package com.ahmed.i221132

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
// ❌ Coil import is no longer needed for this specific function, but keep it for other adapters
import coil.load

private lateinit var pickImageLauncher: ActivityResultLauncher<String>

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var addPostLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupLaunchers()
        setupNavigation()

        // Load dynamic data from Firebase
        loadUserData()
        loadFeedContent()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        val profileImageView = findViewById<CircleImageView>(R.id.profile_image)

        database.getReference("users").child(uid).get()
            .addOnSuccessListener { dataSnapshot ->
                // 🔑 CHANGE: Fetched 'profileImageBase64' instead of 'profileImageUrl'
                val profileImageBase64 = dataSnapshot.child("profileImageBase64").value as? String

                // 🔑 CHANGE: Replaced Coil with Base64 decoding logic
                if (!profileImageBase64.isNullOrEmpty()) {
                    try {
                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        profileImageView.setImageBitmap(decodedImage)
                    } catch (e: Exception) {
                        profileImageView.setImageResource(R.drawable.user)
                    }
                } else {
                    profileImageView.setImageResource(R.drawable.user)
                }
            }
    }

    private fun loadFeedContent() {
        // ... (This function remains the same as before, no changes needed here)
        val storiesRecyclerView = findViewById<RecyclerView>(R.id.stories_recycler_view)
        storiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val stories = listOf(Story("Your story", R.drawable.ahmed))
        storiesRecyclerView.adapter = StoryAdapter(stories, this) { /* click logic */ }

        val postsRecyclerView = findViewById<RecyclerView>(R.id.posts_recycler_view)
        postsRecyclerView.layoutManager = LinearLayoutManager(this)

        val postList = mutableListOf<Post>()
        val postAdapter = PostAdapter(postList, this)
        postsRecyclerView.adapter = postAdapter

        val postsRef = database.getReference("posts")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        postList.add(post)
                    }
                }
                postList.reverse()
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