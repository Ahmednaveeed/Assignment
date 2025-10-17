package com.ahmed.i221132

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View // Import for View.GONE and View.VISIBLE
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView // Import for the imageScroll
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class search : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // UI elements for search
    private lateinit var searchEditText: EditText
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var noResultsText: TextView
    private lateinit var imageScroll: ScrollView // Reference to the static Explore content
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // 1. Initialize Search UI components

        searchEditText = findViewById(R.id.search_edit_text)
        searchRecyclerView = findViewById(R.id.search_results_recycler_view)
        imageScroll = findViewById(R.id.imageScroll)
        noResultsText = findViewById(R.id.no_results_text)


        // 2. Setup RecyclerView and Adapter
        searchAdapter = SearchAdapter(this, userList) { user ->
            // Handle item click: Navigate to the user's Profile
            val intent = Intent(this, Profile::class.java)
            // Pass the target user's UID to the Profile activity
            intent.putExtra("TARGET_USER_UID", user.uid)
            startActivity(intent)
        }
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = searchAdapter

        // 3. Setup Search Listener
        setupSearchListener()


        // Existing navigation and profile picture code
        val home_image = findViewById<ImageView>(R.id.home_image)
        val heart_image = findViewById<ImageView>(R.id.heart_image)
        val profile_image = findViewById<CircleImageView>(R.id.profile_image)

        loadUserProfilePicture()

        home_image.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        heart_image.setOnClickListener {
            // NOTE: heart_following will eventually show Follow Requests and activity
            val intent = Intent(this, heart_following::class.java)
            startActivity(intent)
        }
        profile_image.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            // This is navigating to the current user's profile
            startActivity(intent)
        }
    }

    // Function containing the Firebase search listener setup
    private fun setupSearchListener() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Trigger search every time text changes
                searchUsers(s.toString())
            }
            // Required overridden functions from TextWatcher
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun searchUsers(query: String) {
        val searchQuery = query.trim().toLowerCase()

        if (searchQuery.isEmpty()) {
            // If query is empty, clear results and show the static 'Explore' content
            userList.clear()
            searchAdapter.notifyDataSetChanged()

            // UI visibility toggle
            searchRecyclerView.visibility = View.GONE
            noResultsText.visibility = View.GONE
            imageScroll.visibility = View.VISIBLE
            return
        }

        // If query is NOT empty, hide static content and show the RecyclerView for results
//        searchRecyclerView.visibility = View.VISIBLE

        imageScroll.visibility = View.GONE
        noResultsText.visibility = View.GONE
        // --- CORE FIREBASE SEARCH QUERY ---
        // Searches the 'users' node, ordering by 'username'
        database.getReference("users")
            .orderByChild("username")
            .startAt(searchQuery) // Start searching at the query prefix
            .endAt(searchQuery + "\uf8ff") // End searching at the end of the prefix range
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    val currentUserId = auth.currentUser?.uid

                    for (userSnapshot in snapshot.children) {
                        // Map the user JSON data to the User data class
                        val user = userSnapshot.getValue(User::class.java)?.copy(uid = userSnapshot.key ?: "")

                        // Check if the user is valid and not the current user
                        if (user != null && user.uid != currentUserId) {
                            // Secondary check to ensure strict prefix match (Firebase range queries can be broad)
                            if (user.username.toLowerCase().startsWith(searchQuery)) {
                                userList.add(user)
                            }
                        }
                    }
                    // --- STATE 3: Handle Results Found vs. Not Found ---
                    if (userList.isEmpty()) {
                        // 🚀 Substate A: NO RESULTS FOUND
                        searchRecyclerView.visibility = View.GONE
                        noResultsText.visibility = View.VISIBLE // SHOW "No user found"
                    } else {
                        // 🚀 Substate B: RESULTS FOUND
                        searchRecyclerView.visibility = View.VISIBLE // Show the search results list
                        noResultsText.visibility = View.GONE
                    }
                    searchAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                    // Example: Log.e("Search", "Database error: ${error.message}")
                }
            })
    }

    // Function to fetch and display the current user's profile picture
    private fun loadUserProfilePicture() {
        val uid = auth.currentUser?.uid ?: return
        val profileImageView = findViewById<CircleImageView>(R.id.profile_image)

        database.getReference("users").child(uid).get().addOnSuccessListener { dataSnapshot ->
            val profileImageBase64 = dataSnapshot.child("profileImageBase64").value as? String
            if (!profileImageBase64.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    profileImageView.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    // Fallback to a default drawable if decoding fails
                    profileImageView.setImageResource(R.drawable.user)
                }
            } else {
                profileImageView.setImageResource(R.drawable.user)
            }
        }
    }
}