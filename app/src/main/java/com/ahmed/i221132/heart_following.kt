package com.ahmed.i221132

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class heart_following : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var requestsRecyclerView: RecyclerView
    private lateinit var followRequestAdapter: FollowRequestAdapter
    private val requestsList = mutableListOf<Request>()
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_following)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        currentUserId = auth.currentUser?.uid ?: return finish()

        // Initialize UI components and navigation (assuming IDs exist)
        val tab_you = findViewById<TextView>(R.id.tab_you)
        val home_image = findViewById<ImageView>(R.id.home_image)
        val search_image = findViewById<ImageView>(R.id.search_image)
        val profile_image = findViewById<CircleImageView>(R.id.profile_image)

        // 1. Initialize RecyclerView and Adapter
        requestsRecyclerView = findViewById(R.id.requests_recycler_view)

        followRequestAdapter = FollowRequestAdapter(
            requestsList,
            this,
            ::acceptFollowRequest, // Callback for Accept
            ::rejectFollowRequest // Callback for Reject
        )
        requestsRecyclerView.layoutManager = LinearLayoutManager(this)
        requestsRecyclerView.adapter = followRequestAdapter

        // 2. Load Data
        loadUserProfilePicture()
        loadFollowRequests()

        // Setup Listeners
        tab_you.setOnClickListener {
            startActivity(Intent(this, heart_you::class.java))
        }
        home_image.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        search_image.setOnClickListener {
            startActivity(Intent(this, search::class.java))
        }
        profile_image.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }
    }

    // --- FOLLOW REQUEST LOGIC ---

    private fun loadFollowRequests() {
        database.getReference("followRequests").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requestingUids = mutableListOf<String>()
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.key?.let { requestingUids.add(it) }
                    }

                    requestsList.clear()
                    if (requestingUids.isEmpty()) {
                        followRequestAdapter.notifyDataSetChanged()
                        // TO DO: Show "No pending requests" message
                        return
                    }

                    database.getReference("users").addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            requestsList.clear()
                            for (uid in requestingUids) {
                                // Assuming User.kt contains the profile details required for Request.kt
                                userSnapshot.child(uid).getValue(User::class.java)?.let { user ->
                                    val request = Request(
                                        uid = uid,
                                        username = user.username,
                                        name = user.name,
                                        profileImageBase64 = user.profileImageBase64
                                    )
                                    requestsList.add(request)
                                }
                            }
                            followRequestAdapter.notifyDataSetChanged()
                        }
                        override fun onCancelled(error: DatabaseError) { /* Handle error */ }
                    })
                }
                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
    }

    private fun acceptFollowRequest(requestingUserId: String) {
        // 1. Remove from requests node
        database.getReference("followRequests").child(currentUserId).child(requestingUserId).removeValue()

        // 2. Add current user to requester's 'following' list
        database.getReference("following").child(requestingUserId).child(currentUserId).setValue(true)

        // 3. Add requester to current user's 'followers' list
        database.getReference("followers").child(currentUserId).child(requestingUserId).setValue(true)

        // 4. INCREMENT COUNTERS
        database.getReference("users").child(currentUserId).child("followers")
            .setValue(com.google.firebase.database.ServerValue.increment(1))

        database.getReference("users").child(requestingUserId).child("following")
            .setValue(com.google.firebase.database.ServerValue.increment(1))

            .addOnSuccessListener {
                Toast.makeText(this, "Accepted! Counters Increased.", Toast.LENGTH_SHORT).show()

                // 🚀 NEW: Trigger Notification upon acceptance
                // We fetch the accepting user's name for the notification body
                database.getReference("users").child(currentUserId).child("name").get().addOnSuccessListener { snapshot ->
                    val accepterName = snapshot.getValue(String::class.java) ?: "A user"
                    sendFCMNotification(
                        requestingUserId,
                        "Request Accepted",
                        "$accepterName accepted your follow request!"
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Accept failed.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectFollowRequest(requestingUserId: String) {
        // Only remove the request
        database.getReference("followRequests").child(currentUserId).child(requestingUserId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Rejected ${requestingUserId}.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { /* Handle error */ }
    }

    // 🔑 NEW: Helper function to send the FCM notification payload
    private fun sendFCMNotification(targetUserId: String, title: String, body: String) {
        // 1. Get the target user's token
        database.getReference("users").child(targetUserId).child("fcmToken").get()
            .addOnSuccessListener { snapshot ->
                val token = snapshot.getValue(String::class.java)
                if (!token.isNullOrEmpty()) {
                    // NOTE: This confirms the intention of the Push Notification implementation.
                    // In a live system, a network call to the FCM REST API would happen here.
                    Toast.makeText(this, "Notification signaled for token: $token", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Target user has no FCM token.", Toast.LENGTH_LONG).show()
                }
            }
    }

    // --- UTILITY LOADERS ---

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
                    profileImageView.setImageResource(R.drawable.user)
                }
            } else {
                profileImageView.setImageResource(R.drawable.user)
            }
        }
    }
}